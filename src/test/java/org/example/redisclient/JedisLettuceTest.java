package org.example.redisclient;

import static org.assertj.core.api.Assertions.assertThat;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

public class JedisLettuceTest {

    @Test
    void 제디스() {
        Jedis jedis = new Jedis("127.0.0.1", 6379);

        jedis.set("jedis", "value");
        String value = jedis.get("jedis");

        assertThat(value).isEqualTo("value");
        jedis.close();
    }

    @Test
    void 레투스() {
        RedisClient redisClient = RedisClient.create("redis://127.0.0.1:6379");
        StatefulRedisConnection<String, String> con = redisClient.connect();
        RedisCommands<String, String> sync = con.sync();

        sync.set("lettuce", "value");
        String value = sync.get("lettuce");

        assertThat(value).isEqualTo("value");
        con.close();
        redisClient.shutdown();
    }
}
