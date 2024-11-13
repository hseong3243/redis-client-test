package org.example.redisclient;

import static org.assertj.core.api.Assertions.assertThat;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public class LettuceRedissonTest {

    private final int poolSize = 50;
    private final ExecutorService executorService = Executors.newFixedThreadPool(poolSize);
    private CountDownLatch latch;
    private Counter counter;

    private static class Counter {

        private int count = 0;

        private void increment() {
            count++;
        }
    }

    @BeforeAll
    static void beforeAll() {
        try(RedisClient redisClient = RedisClient.create("redis://localhost:6379")) {
            RedisCommands<String, String> sync = redisClient.connect().sync();
            sync.flushall();
        }
    }

    @BeforeEach
    void setUp() {
        latch = new CountDownLatch(poolSize);
        counter = new Counter();
    }

    @Test
    public void 레투스() throws InterruptedException {
        // given
        RedisClient redisClient = RedisClient.create("redis://localhost:6379");
        StatefulRedisConnection<String, String> con = redisClient.connect();
        RedisCommands<String, String> sync = con.sync();

        // when
        for (int i = 0; i < poolSize; i++) {
            executorService.submit(() -> {
                try {
                    while (!sync.setnx("lettuce", "lock")) {
                        Thread.sleep(10);
                    }
                    counter.increment();
                    sync.del("lettuce");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        //then
        assertThat(counter.count).isEqualTo(poolSize);
        con.close();
        redisClient.shutdown();
    }

    @Test
    public void 레디슨() throws InterruptedException {
        // given
        Config config = new Config();
        config.useSingleServer().setAddress("redis://localhost:6379");
        RedissonClient redissonClient = Redisson.create(config);

        // when
        RLock lock = redissonClient.getLock("redisson");
        for (int i = 0; i < poolSize; i++) {
            executorService.submit(() -> {
                try {
                    if (lock.tryLock(5, 1, TimeUnit.SECONDS)) {
                        counter.increment();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    lock.unlock();
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then
        assertThat(counter.count).isEqualTo(poolSize);
        redissonClient.shutdown();
    }
}
