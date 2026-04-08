package com.shamodha.iot.mapper;

import com.shamodha.iot.dto.DeviceDTO;
import com.shamodha.iot.entity.ThermoHygrometer;
import org.springframework.stereotype.Component;

/**
 * --------------------------------------------
 * Author: Shamodha Sahan
 * GitHub: https://github.com/shamodhas
 * Website: https://shamodha.com
 * --------------------------------------------
 * Created: 2/22/2026 11:52 AM
 * Project: iot-service
 * --------------------------------------------
 **/

@Component
public class DeviceMapper {
    public DeviceDTO mapToDTO(ThermoHygrometer entity) {
        return new DeviceDTO(
                entity.getId(),
                entity.getName(),
                entity.getZoneId(),
                entity.getUserId(),
                entity.getCreatedAt().toString()
        );
    }
}