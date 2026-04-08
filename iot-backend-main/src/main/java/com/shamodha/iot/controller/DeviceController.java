package com.shamodha.iot.controller;

import com.shamodha.iot.dto.DeviceDTO;
import com.shamodha.iot.dto.SensorDataDTO;
import com.shamodha.iot.dto.UserDTO;
import com.shamodha.iot.service.DeviceService;
import com.shamodha.iot.service.SensorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;

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

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {
    private final DeviceService deviceService;
    private final SensorService sensorService;

    @PostMapping
    public Mono<DeviceDTO> saveDevice(@RequestBody DeviceDTO device, @AuthenticationPrincipal UserDTO user) {
        device.setUserId(user.getUserId());
        return deviceService.saveDevice(device);
    }

    @GetMapping
    public Flux<DeviceDTO> getMyDevices(
            @AuthenticationPrincipal UserDTO user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return deviceService.getMyDevices(user.getUserId(), pageable);
    }

    @GetMapping("/telemetry/{deviceId}")
    public Mono<SensorDataDTO> readData(@PathVariable String deviceId) {
        return sensorService.refreshAndGetTelemetry(deviceId);
    }

//    @GetMapping("/telemetry/{deviceId}/history")
//    public Flux<SensorData> getDeviceHistory(@PathVariable String deviceId) {
//        return sensorService.getHistory(deviceId);
//    }
}