package com.shamodha.iot.service;

import com.shamodha.iot.dto.SensorDataDTO;
import com.shamodha.iot.dto.Unit;
import com.shamodha.iot.entity.ThermoHygrometer;
import com.shamodha.iot.mapper.SensorMapper;
import com.shamodha.iot.model.SensorData;
import com.shamodha.iot.model.SensorLogEntry;
import com.shamodha.iot.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Random;

/**
 * --------------------------------------------
 * Author: Shamodha Sahan
 * GitHub: https://github.com/shamodhas
 * Website: https://shamodha.com
 * --------------------------------------------
 * Created: 2/22/2026 11:17 AM
 * Project: iot-service
 * --------------------------------------------
 **/

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorService {

    private final ReactiveRedisOperations<String, SensorLogEntry> redisOperations;
    private final ReactiveStringRedisTemplate stringRedisTemplate;
    private final Random random = new Random();
    private final SensorMapper sensorMapper;

    public Mono<Boolean> initDeviceData(ThermoHygrometer device) {
        SensorData initialData = SensorData.builder()
                .deviceId(device.getId())
                .zoneId(device.getZoneId())
                .temperature(24.0)
                .tempUnit(Unit.CELSIUS)
                .humidity(55.0)
                .humidityUnit(Unit.PERCENTAGE)
                .capturedAt(Instant.now())
                .build();

        SensorLogEntry sensorLogEntry = sensorMapper.mapToCache(initialData);

        log.info("Initializing Redis cache for device: {}", device.getId());

        return redisOperations.opsForValue().set("live:" + device.getId(), sensorLogEntry)
                .then(stringRedisTemplate.opsForSet().add("active_devices", device.getId()))
                .doOnNext(count -> log.info("Device {} added to active set. Total active: {}", device.getId(), count))
                .map(count -> count != null && count >= 0);
    }

    public Mono<SensorDataDTO> refreshAndGetTelemetry(String deviceId) {
        return redisOperations.opsForValue().get("live:" + deviceId)
                .flatMap(lastData -> {
                    SensorData sensorData = sensorMapper.mapFromCache(lastData);

                    SensorData newData = generateNextStep(sensorData);
                    SensorLogEntry sensorLogEntry = sensorMapper.mapToCache(newData);

                    return redisOperations.opsForValue().set("live:" + deviceId, sensorLogEntry)
                            .map(success -> sensorMapper.mapToDTO(newData));
                })
                .doOnError(e -> log.error("Error refreshing telemetry for device {}: {}", deviceId, e.getMessage()));
    }

    private SensorData generateNextStep(SensorData lastData) {
        double newTemp = lastData.getTemperature() + (random.nextDouble() - 0.5) * 0.4;
        double newHumid = lastData.getHumidity() + (random.nextDouble() - 0.5) * 0.4;

        return SensorData.builder()
                .deviceId(lastData.getDeviceId())
                .zoneId(lastData.getZoneId())
                .temperature(Math.round(newTemp * 100.0) / 100.0)
                .tempUnit(Unit.CELSIUS)
                .humidity(Math.round(newHumid * 100.0) / 100.0)
                .humidityUnit(Unit.PERCENTAGE)
                .capturedAt(Instant.now())
                .build();
    }
}