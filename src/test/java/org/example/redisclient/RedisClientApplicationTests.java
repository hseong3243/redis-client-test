package org.example.redisclient;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class RedisClientApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        for (String bean : applicationContext.getBeanDefinitionNames()) {
            System.out.println("bean: " + bean);
        }
    }
}
