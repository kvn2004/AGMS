package com.shamodha.iot.model;

import com.shamodha.iot.dto.Unit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * --------------------------------------------
 * Author: Shamodha Sahan
 * GitHub: https://github.com/shamodhas
 * Website: https://shamodha.com
 * --------------------------------------------
 * Created: 2/22/2026 1:45 PM
 * Project: iot-service
 * --------------------------------------------
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorLogEntry {
    private String deviceId;
    private String zoneId;
    private double temperature;
    private String tempUnit;
    private double humidity;
    private String humidityUnit;
    private String capturedAt;
}