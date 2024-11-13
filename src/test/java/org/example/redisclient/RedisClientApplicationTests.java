package org.example.redisclient;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
class RedisClientApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void contextLoads() {
        for (String bean : applicationContext.getBeanDefinitionNames()) {
            System.out.println("bean: " + bean);
        }
        RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();
        assertThat(connectionFactory).isInstanceOf(LettuceConnectionFactory.class);
        assertThat(connectionFactory).isNotInstanceOf(JedisConnectionFactory.class);
    }
}
