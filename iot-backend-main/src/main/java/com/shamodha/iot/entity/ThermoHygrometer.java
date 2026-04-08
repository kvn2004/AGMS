package com.shamodha.iot.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * --------------------------------------------
 * Author: Shamodha Sahan
 * GitHub: https://github.com/shamodhas
 * Website: https://shamodha.com
 * --------------------------------------------
 * Created: 2/22/2026 8:53 AM
 * Project: iot-service
 * --------------------------------------------
 **/

@Table("devices")
@Data
@EqualsAndHashCode(callSuper = true)
public class ThermoHygrometer extends BaseEntity {
    @Column("device_id")
    private String id;

    private String userId;
    private String name;
    private String zoneId;
}