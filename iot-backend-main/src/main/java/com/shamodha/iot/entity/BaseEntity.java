package com.shamodha.iot.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.Instant;

/**
 * --------------------------------------------
 * Author: Shamodha Sahan
 * GitHub: https://github.com/shamodhas
 * Website: https://shamodha.com
 * --------------------------------------------
 * Created: 2/22/2026 10:15 AM
 * Project: iot-service
 * --------------------------------------------
 **/

@Data
public abstract class BaseEntity {
    @Id
    private String id;
    private Instant createdAt;
}