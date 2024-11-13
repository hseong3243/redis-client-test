package org.example.redisclient;

import static org.assertj.core.api.Assertions.assertThat;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisTheadSafeTest {

    private final int poolSize = 50;
    private final ExecutorService executorService = Executors.newFixedThreadPool(poolSize);
    private CountDownLatch latch;

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
    }

    @Test
    void 제디스풀() throws InterruptedException {
        // given
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(poolSize);
        config.setMaxIdle(poolSize);
        config.setMinIdle(poolSize);
        JedisPool jedisPool = new JedisPool(config, "127.0.0.1", 6379);

        // when
        for (int i = 0; i < poolSize; i++) {
            executorService.submit(() -> {
                try (Jedis jedis = jedisPool.getResource()) {
                    jedis.incr("jedis");
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then
        Jedis jedis = jedisPool.getResource();
        String result = jedis.get("jedis");
        assertThat(result).isEqualTo(String.valueOf(poolSize));
        jedis.close();
        jedisPool.close();
    }

    @Test
    void 레투스_스레드_안전() throws InterruptedException {
        // given
        RedisClient redisClient = RedisClient.create("redis://127.0.0.1:6379");
        StatefulRedisConnection<String, String> con = redisClient.connect();
        RedisCommands<String, String> sync = con.sync();
        sync.set("lettuce", "0");

        // when
        for (int i = 0; i < poolSize; i++) {
            executorService.submit(() -> {
                try {
                    sync.incr("lettuce");
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then
        assertThat(sync.get("lettuce")).isEqualTo(String.valueOf(poolSize));
        con.close();
        redisClient.shutdown();
    }
}
