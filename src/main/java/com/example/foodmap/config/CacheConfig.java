package com.example.foodmap.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager redisCacheManager(
            RedisConnectionFactory cf,
            @Value("${cache.ttl.search.seconds:3600}") long searchTtl,
            @Value("${cache.ttl.geocode.seconds:2592000}") long geocodeTtl
    ) {
        var keySer   = new StringRedisSerializer();
        var valueSer = new GenericJackson2JsonRedisSerializer();

        RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSer))
                .entryTtl(Duration.ofSeconds(searchTtl)); // 기본 TTL

        Map<String, RedisCacheConfiguration> configs = Map.of(
                "geocode", base.entryTtl(Duration.ofSeconds(geocodeTtl)),
                "placeNearby", base.entryTtl(Duration.ofSeconds(searchTtl))
        );

        return RedisCacheManager.builder(cf)
                .cacheDefaults(base)
                .withInitialCacheConfigurations(configs)
                .build();
    }
}
