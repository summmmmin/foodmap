package com.example.foodmap.config;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

public class MySqlContainerConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    static final MySQLContainer<?> MYSQL =
            new MySQLContainer<>(DockerImageName.parse("mysql:8.4"))
                    .withDatabaseName("foodmap")
                    .withUsername("user")
                    .withPassword("pass");

    @BeforeAll
    static void startContainer() {
        if (!MYSQL.isRunning()) {
            MYSQL.start();
        }
    }

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        startContainer();
        TestPropertyValues.of(
                "spring.datasource.url=" + MYSQL.getJdbcUrl(),
                "spring.datasource.username=" + MYSQL.getUsername(),
                "spring.datasource.password=" + MYSQL.getPassword(),
                "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
                // 테스트에서는 Redis 오토 설정 배제(캐시 사용 안함)
                "spring.autoconfigure.exclude=" +
                        "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration",
                // schema.sql 적용
                "spring.sql.init.mode=always",
                "spring.sql.init.encoding=UTF-8",
                "server.port=0"
        ).applyTo(context.getEnvironment());
    }
}
