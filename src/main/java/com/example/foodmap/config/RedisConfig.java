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
public class RedisConfig {

    @Bean
    public RedisCacheManager redisCacheManager(
            RedisConnectionFactory cf,
            @Value("${cache.ttl.search.seconds:3600}") long searchTtl,
            @Value("${cache.ttl.geocode.seconds:2592000}") long geocodeTtl
    ) {
        var keySerializer   = new StringRedisSerializer();

        var polymorphicTypeValidator = com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("java.util")
                .allowIfSubType("com.example.foodmap")
                .build();

        var omJsonMapper = com.fasterxml.jackson.databind.json.JsonMapper.builder()
                .activateDefaultTyping(polymorphicTypeValidator, com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.EVERYTHING,
                        com.fasterxml.jackson.annotation.JsonTypeInfo.As.WRAPPER_ARRAY)
                .build();

        var valueSerializer = new GenericJackson2JsonRedisSerializer(omJsonMapper);

        var geocodeValueSerializer = new org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer<>(double[].class);



        RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
                .prefixCacheNameWith("foodmap:")
                .disableCachingNullValues()
                .entryTtl(Duration.ofSeconds(searchTtl));

        var geocodeConfiguration = base
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(geocodeValueSerializer))
                .entryTtl(Duration.ofSeconds(geocodeTtl));

        var nearbyConfiguration = base.entryTtl(Duration.ofSeconds(searchTtl));

        Map<String, RedisCacheConfiguration> configs = Map.of(
                "geocode", geocodeConfiguration,
                "placeNearby", nearbyConfiguration
        );

        return RedisCacheManager.builder(cf)
                .cacheDefaults(base)
                .withInitialCacheConfigurations(configs)
                .build();
    }
}
