package com.vihanganimsara.zone_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZoneRequestDTO {
    private String name;
    private double minTemp;
    private double maxTemp;
    private String deviceId;
    private String accessToken;
}

