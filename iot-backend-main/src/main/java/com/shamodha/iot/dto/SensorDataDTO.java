package com.shamodha.iot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * --------------------------------------------
 * Author: Shamodha Sahan
 * GitHub: https://github.com/shamodhas
 * Website: https://shamodha.com
 * --------------------------------------------
 * Created: 2/22/2026 11:11 AM
 * Project: iot-service
 * --------------------------------------------
 **/

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SensorDataDTO {
    private String deviceId;
    private String zoneId;
    private ValueDTO value;
    private String capturedAt;
}