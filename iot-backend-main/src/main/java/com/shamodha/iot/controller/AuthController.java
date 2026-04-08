package com.shamodha.iot.controller;

import com.shamodha.iot.dto.UserDTO;
import com.shamodha.iot.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

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
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Mono<ResponseEntity<UserDTO>> register(@RequestBody UserDTO dto) {
        return authService.register(dto)
                .map(res -> ResponseEntity.status(201).body(res));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<UserDTO>> login(@RequestBody UserDTO dto) {
        return authService.login(dto)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.status(401).build());
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<UserDTO>> refresh(@RequestBody UserDTO dto) {
        return authService.refreshToken(dto.getRefreshToken())
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(401).build()));
    }

    @GetMapping("/health")
    public String test() {
        return "Iot Service health is Good..!";
    }
}