package com.shamodha.iot.config;

import com.shamodha.iot.model.SensorData;
import com.shamodha.iot.model.SensorLogEntry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * --------------------------------------------
 * Author: Shamodha Sahan
 * GitHub: https://github.com/shamodhas
 * Website: https://shamodha.com
 * --------------------------------------------
 * Created: 2/22/2026 11:25 AM
 * Project: iot-service
 * --------------------------------------------
 **/

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveStringRedisTemplate reactiveStringRedisTemplate(ReactiveRedisConnectionFactory factory) {
        return new ReactiveStringRedisTemplate(factory);
    }

    @Bean
    public ReactiveRedisOperations<String, SensorLogEntry> redisOperations(ReactiveRedisConnectionFactory factory) {
        Jackson2JsonRedisSerializer<SensorLogEntry> serializer = new Jackson2JsonRedisSerializer<>(SensorLogEntry.class);

        RedisSerializationContext.RedisSerializationContextBuilder<String, SensorLogEntry> builder =
                RedisSerializationContext.newSerializationContext(new StringRedisSerializer());

        RedisSerializationContext<String, SensorLogEntry> context = builder.value(serializer).build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}