package com.vihanganimsara.zone_service.service;

import com.vihanganimsara.zone_service.dto.UpdateThresholdDTO;
import com.vihanganimsara.zone_service.dto.ZoneRequestDTO;
import com.vihanganimsara.zone_service.dto.ZoneResponseDTO;
import com.vihanganimsara.zone_service.entity.Zone;
import com.vihanganimsara.zone_service.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ZoneService {

    private final ZoneRepository zoneRepository;
    private final WebClient webClient;


    public ZoneResponseDTO createZone(ZoneRequestDTO zoneRequestDTO, String accessToken) {
        validateTemperatureThresholds(zoneRequestDTO.getMinTemp(), zoneRequestDTO.getMaxTemp());


        Zone zone = new Zone();
        zone.setName(zoneRequestDTO.getName());
        zone.setMinTemp(zoneRequestDTO.getMinTemp());
        zone.setMaxTemp(zoneRequestDTO.getMaxTemp());
        Zone savedZone = zoneRepository.save(zone);


        Map<String, String> deviceRequest = new HashMap<>();
        deviceRequest.put("name", savedZone.getName() + "-Sensor");
        deviceRequest.put("zoneId", savedZone.getId().toString()); // IMPORTANT: Use DB ID

        try {
            Map<String, Object> response = webClient.post()
                    .uri("/devices")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .bodyValue(deviceRequest)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block(); // blocking here for simplicity

            if (response != null && response.containsKey("deviceId")) {
                String deviceId = (String) response.get("deviceId");
                savedZone.setDeviceId(deviceId);
                zoneRepository.save(savedZone); // update zone with deviceId
            } else {
                throw new RuntimeException("IoT API returned invalid response");
            }

        } catch (WebClientResponseException e) {
            throw new RuntimeException("IoT API device registration failed: "
                    + e.getStatusCode() + " " + e.getResponseBodyAsString());
        }

        return mapToResponseDTO(savedZone);
    }


    public ZoneResponseDTO getZoneById(Long id) {
        Optional<Zone> zone = zoneRepository.findById(id);
        if (zone.isPresent()) {
            return mapToResponseDTO(zone.get());
        }
        throw new RuntimeException("Zone not found with id: " + id);
    }


    public ZoneResponseDTO updateThresholds(Long id, UpdateThresholdDTO updateThresholdDTO) {
        validateTemperatureThresholds(updateThresholdDTO.getMinTemp(), updateThresholdDTO.getMaxTemp());

        Optional<Zone> zoneOpt = zoneRepository.findById(id);
        if (zoneOpt.isPresent()) {
            Zone zone = zoneOpt.get();
            zone.setMinTemp(updateThresholdDTO.getMinTemp());
            zone.setMaxTemp(updateThresholdDTO.getMaxTemp());
            Zone updatedZone = zoneRepository.save(zone);
            return mapToResponseDTO(updatedZone);
        }
        throw new RuntimeException("Zone not found with id: " + id);
    }

    public void deleteZone(Long id) {
        if (!zoneRepository.existsById(id)) {
            throw new RuntimeException("Zone not found with id: " + id);
        }
        zoneRepository.deleteById(id);
    }


    private ZoneResponseDTO mapToResponseDTO(Zone zone) {
        return new ZoneResponseDTO(
                zone.getId(),
                zone.getName(),
                zone.getMinTemp(),
                zone.getMaxTemp(),
                zone.getDeviceId()
        );
    }

 
    private void validateTemperatureThresholds(double minTemp, double maxTemp) {
        if (minTemp >= maxTemp) {
            throw new IllegalArgumentException(
                    String.format("Invalid temperature thresholds: minTemp (%s) must be less than maxTemp (%s)",
                            minTemp, maxTemp)
            );
        }
    }
}