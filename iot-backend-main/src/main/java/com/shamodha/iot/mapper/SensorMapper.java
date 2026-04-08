package com.shamodha.iot.mapper;

import com.shamodha.iot.dto.SensorDataDTO;
import com.shamodha.iot.dto.Unit;
import com.shamodha.iot.dto.ValueDTO;
import com.shamodha.iot.model.SensorData;
import com.shamodha.iot.model.SensorLogEntry;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * --------------------------------------------
 * Author: Shamodha Sahan
 * GitHub: https://github.com/shamodhas
 * Website: https://shamodha.com
 * --------------------------------------------
 * Created: 2/22/2026 11:43 AM
 * Project: iot-service
 * --------------------------------------------
 **/

@Component
public class SensorMapper {
    public SensorDataDTO mapToDTO(SensorData model) {
        return SensorDataDTO.builder()
                .deviceId(model.getDeviceId())
                .zoneId(model.getZoneId())
                .value(new ValueDTO(
                        model.getTemperature(),
                        model.getTempUnit(),
                        model.getHumidity(),
                        model.getHumidityUnit()
                ))
                .capturedAt(model.getCapturedAt().toString())
                .build();
    }

    public SensorLogEntry mapToCache(SensorData sensorData) {
        if (sensorData == null) return null;

        return SensorLogEntry.builder()
                .deviceId(sensorData.getDeviceId())
                .zoneId(sensorData.getZoneId())
                .temperature(sensorData.getTemperature())
                .tempUnit(sensorData.getTempUnit().toString())
                .humidity(sensorData.getHumidity())
                .humidityUnit(sensorData.getHumidityUnit().toString())
                .capturedAt(sensorData.getCapturedAt().toString())
                .build();
    }

    public SensorData mapFromCache(SensorLogEntry sensorLogEntry) {
        if (sensorLogEntry == null) return null;

        return SensorData.builder()
                .deviceId(sensorLogEntry.getDeviceId())
                .zoneId(sensorLogEntry.getZoneId())
                .temperature(sensorLogEntry.getTemperature())
                .tempUnit(Unit.valueOf(sensorLogEntry.getTempUnit()))
                .humidity(sensorLogEntry.getHumidity())
                .humidityUnit(Unit.valueOf(sensorLogEntry.getHumidityUnit()))
                .capturedAt(Instant.parse(sensorLogEntry.getCapturedAt()))
                .build();
    }
}
