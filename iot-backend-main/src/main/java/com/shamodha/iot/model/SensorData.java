package com.shamodha.iot.model;

import com.shamodha.iot.dto.Unit;
import com.shamodha.iot.dto.ValueDTO;
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
 * Created: 2/22/2026 10:21 AM
 * Project: iot-service
 * --------------------------------------------
 **/

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SensorData {
    private String deviceId;
    private String zoneId;
    private double temperature;
    private Unit tempUnit;
    private double humidity;
    private Unit humidityUnit;
    private Instant capturedAt;
}
