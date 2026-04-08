package com.shamodha.iot.dto;

/**
 * --------------------------------------------
 * Author: Shamodha Sahan
 * GitHub: https://github.com/shamodhas
 * Website: https://shamodha.com
 * --------------------------------------------
 * Created: 2/22/2026 9:18 AM
 * Project: iot-service
 * --------------------------------------------
 **/

public enum Unit {
    CELSIUS("°C"),
    FAHRENHEIT("°F"),
    PERCENTAGE("%");

    private final String symbol;

    Unit(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}