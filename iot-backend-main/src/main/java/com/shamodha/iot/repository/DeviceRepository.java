package com.shamodha.iot.repository;

import com.shamodha.iot.entity.ThermoHygrometer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * --------------------------------------------
 * Author: Shamodha Sahan
 * GitHub: https://github.com/shamodhas
 * Website: https://shamodha.com
 * --------------------------------------------
 * Created: 2/22/2026 8:54 AM
 * Project: iot-service
 * --------------------------------------------
 **/

@Repository
public interface DeviceRepository extends ReactiveCrudRepository<ThermoHygrometer, String> {
    Flux<ThermoHygrometer> findByUserId(String userId, Pageable pageable);
}