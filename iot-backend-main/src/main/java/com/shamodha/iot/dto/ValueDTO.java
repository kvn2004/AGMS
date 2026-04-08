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
 * Created: 2/22/2026 9:16 AM
 * Project: iot-service
 * --------------------------------------------
 **/

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValueDTO {
    private double temperature;
    private Unit tempUnit;

    private double humidity;
    private Unit humidityUnit;
}