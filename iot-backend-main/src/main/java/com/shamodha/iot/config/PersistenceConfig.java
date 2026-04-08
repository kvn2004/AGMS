package com.shamodha.iot.config;

import com.shamodha.iot.entity.BaseEntity;
import com.shamodha.iot.entity.ThermoHygrometer;
import com.shamodha.iot.entity.User;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.r2dbc.mapping.event.BeforeConvertCallback;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * --------------------------------------------
 * Author: Shamodha Sahan
 * GitHub: https://github.com/shamodhas
 * Website: https://shamodha.com
 * --------------------------------------------
 * Created: 2/22/2026 10:03 AM
 * Project: iot-service
 * --------------------------------------------
 **/

@Configuration
public class PersistenceConfig {

    @Bean
    public BeforeConvertCallback<BaseEntity> baseEntityCallback() {
        return (entity, sqlIdentifier) -> {
            if (entity.getId() == null) {
                entity.setId(UUID.randomUUID().toString());
            }
            if (entity.getCreatedAt() == null) {
                entity.setCreatedAt(Instant.now());
            }
            return Mono.just(entity);
        };
    }

    @Bean
    public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);

        String query = """
                CREATE TABLE IF NOT EXISTS users (
                    id VARCHAR(36) PRIMARY KEY,
                    username VARCHAR(255) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL,
                    role VARCHAR(50),
                    created_at DATETIME(6)
                );
                
                CREATE TABLE IF NOT EXISTS devices (
                    device_id VARCHAR(36) PRIMARY KEY,
                    user_id VARCHAR(50),
                    name VARCHAR(100),
                    zone_id VARCHAR(50),
                    created_at DATETIME(6)
                );
                """;

        initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ByteArrayResource(query.getBytes())));
        return initializer;
    }
}