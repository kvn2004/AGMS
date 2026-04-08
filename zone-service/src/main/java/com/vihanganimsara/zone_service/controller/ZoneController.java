package com.vihanganimsara.zone_service.controller;

import com.vihanganimsara.zone_service.dto.UpdateThresholdDTO;
import com.vihanganimsara.zone_service.dto.ZoneRequestDTO;
import com.vihanganimsara.zone_service.dto.ZoneResponseDTO;
import com.vihanganimsara.zone_service.service.ZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/zones")
@RequiredArgsConstructor
public class ZoneController {

    private final ZoneService zoneService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ZoneResponseDTO createZone(
            @RequestBody ZoneRequestDTO zoneRequestDTO,
            @RequestHeader(value = "Authorization",required = false) String authHeader
    ) {
        // Remove "Bearer " prefix if present
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        return zoneService.createZone(zoneRequestDTO, token);
    }


    @GetMapping("/{id}")
    public ZoneResponseDTO getZone(@PathVariable Long id) {
        return zoneService.getZoneById(id);
    }


    @PutMapping("/{id}")
    public ZoneResponseDTO updateThresholds(@PathVariable Long id, @RequestBody UpdateThresholdDTO dto) {
        return zoneService.updateThresholds(id, dto);
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteZone(@PathVariable Long id) {
        zoneService.deleteZone(id);
    }
}