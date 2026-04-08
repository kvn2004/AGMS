package com.vihanganimsara.zone_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateThresholdDTO {
    private double minTemp;
    private double maxTemp;
}

