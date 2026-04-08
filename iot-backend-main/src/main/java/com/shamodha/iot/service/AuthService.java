package com.shamodha.iot.service;

import com.shamodha.iot.config.JwtUtil;
import com.shamodha.iot.dto.UserDTO;
import com.shamodha.iot.entity.User;
import com.shamodha.iot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

/**
 * --------------------------------------------
 * Author: Shamodha Sahan
 * GitHub: https://github.com/shamodhas
 * Website: https://shamodha.com
 * --------------------------------------------
 * Created: 2/22/2026 9:06 AM
 * Project: iot-service
 * --------------------------------------------
 **/

@Service
public class AuthService implements ReactiveUserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found: " + username)))
                .map(user -> org.springframework.security.core.userdetails.User
                        .withUsername(user.getUsername())
                        .password(user.getPassword())
                        .authorities("USER")
                        .accountExpired(false)
                        .accountLocked(false)
                        .credentialsExpired(false)
                        .disabled(false)
                        .build());
    }

    public Mono<UserDTO> register(UserDTO dto) {
        return userRepository.findByUsername(dto.getUsername())
                .flatMap(exists -> Mono.<User>error(new RuntimeException("User already exists")))
                .switchIfEmpty(Mono.defer(() -> {
                    User user = new User();
                    user.setUsername(dto.getUsername());
                    user.setPassword(passwordEncoder.encode(dto.getPassword()));
                    user.setRole("USER");

                    return userRepository.save(user);
                }))
                .map(u -> {
                    UserDTO res = new UserDTO();
                    res.setUsername(u.getUsername());
                    res.setAccessToken(jwtUtil.generateAccessToken(u.getId(), u.getUsername()));
                    res.setRefreshToken(jwtUtil.generateRefreshToken(u.getId(), u.getUsername()));

                    return res;
                });
    }

    public Mono<UserDTO> login(UserDTO dto) {
        return userRepository.findByUsername(dto.getUsername())
                .filter(user -> passwordEncoder.matches(dto.getPassword(), user.getPassword()))
                .map(user -> {
                    UserDTO res = new UserDTO();
                    res.setUsername(user.getUsername());
                    res.setAccessToken(jwtUtil.generateAccessToken(user.getId(), user.getUsername()));
                    res.setRefreshToken(jwtUtil.generateRefreshToken(user.getId(), user.getUsername()));

                    return res;
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid username or password")));
    }

    public Mono<UserDTO> refreshToken(String refreshToken) {
        return Mono.just(refreshToken)
                .filter(jwtUtil::validateToken)
                .flatMap(token -> {
                    String username = jwtUtil.extractUsername(token);
                    String userId = jwtUtil.extractUserId(token);

                    UserDTO res = new UserDTO();
                    res.setUsername(username);

                    res.setAccessToken(jwtUtil.generateAccessToken(userId, username));
                    res.setRefreshToken(jwtUtil.generateRefreshToken(userId, username));

                    return Mono.just(res);
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid or Expired Refresh Token")));
    }
}