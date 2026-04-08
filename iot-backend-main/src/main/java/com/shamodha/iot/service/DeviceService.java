package com.shamodha.iot.service;

import com.shamodha.iot.dto.DeviceDTO;
import com.shamodha.iot.dto.Unit;
import com.shamodha.iot.dto.ValueDTO;
import com.shamodha.iot.entity.ThermoHygrometer;
import com.shamodha.iot.mapper.DeviceMapper;
import com.shamodha.iot.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

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

@Service
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final SensorService sensorService;
    private final DeviceMapper deviceMapper;

    public Mono<DeviceDTO> saveDevice(DeviceDTO dto) {
        ThermoHygrometer entity = new ThermoHygrometer();
        entity.setUserId(dto.getUserId());
        entity.setName(dto.getName());
        entity.setZoneId(dto.getZoneId());

        return deviceRepository.save(entity)
                .flatMap(saved -> sensorService.initDeviceData(saved).thenReturn(saved))
                .map(deviceMapper::mapToDTO);
    }

    public Flux<DeviceDTO> getMyDevices(String userId, Pageable pageable) {
        return deviceRepository.findByUserId(userId, pageable)
                .map(entity -> deviceMapper.mapToDTO(entity));
    }
}