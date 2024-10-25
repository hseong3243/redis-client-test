package org.example.redisclient;

import static org.assertj.core.api.Assertions.assertThat;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import redis.clients.jedis.Jedis;

@SpringBootTest
class RedisConfigTest {

    @Autowired
    private Jedis jedis;

    @Autowired
    private RedisClient redisClient;

    @Autowired
    private RedissonClient redissonClient;

    @Test
    void 제디스_연결확인() {
        jedis.set("jedis", "value");
        String value = jedis.get("jedis");
        assertThat(value).isEqualTo("value");
    }

    @Test
    void 레투스_연결확인() {
        StatefulRedisConnection<String, String> con = redisClient.connect();
        RedisCommands<String, String> sync = con.sync();
        sync.set("lettuce", "value");
        String value = sync.get("lettuce");
        assertThat(value).isEqualTo("value");
    }

    @Test
    void 레디슨_연결확인() {
        RBucket<String> bucket = redissonClient.getBucket("redisson");
        bucket.set("value");
        String value = bucket.get();
        assertThat(value).isEqualTo("value");
    }
}
