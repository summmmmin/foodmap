package com.example.foodmap.config;

import jakarta.annotation.PreDestroy;
import okhttp3.mockwebserver.MockWebServer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class MockServerConfiguration {

    private static final MockWebServer SERVER;

    static {
        try {
            SERVER = new MockWebServer();
            SERVER.start();
        } catch (Exception e) {
            throw new RuntimeException("Failed to start MockWebServer", e);
        }
    }

    @Bean
    public MockWebServer mockWebServer() {
        return SERVER;
    }

    public static MockWebServer server() {
        return SERVER;
    }

    @PreDestroy
    public void shutdown() throws Exception {
        SERVER.shutdown();
    }
}
