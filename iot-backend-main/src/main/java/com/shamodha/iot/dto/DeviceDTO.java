package com.shamodha.iot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * --------------------------------------------
 * Author: Shamodha Sahan
 * GitHub: https://github.com/shamodhas
 * Website: https://shamodha.com
 * --------------------------------------------
 * Created: 2/22/2026 10:58 AM
 * Project: iot-service
 * --------------------------------------------
 **/

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceDTO {
    private String deviceId;
    private String name;
    private String zoneId;
    private String userId;
    private String createAt;
}