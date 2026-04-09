package com.vihanganimsara.api_gateway.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerWebExchange modifiedExchange = null;

            try {
                // Extract Authorization header
                String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

                if (authHeader == null || authHeader.isEmpty()) {
                    log.warn("Missing Authorization header for request: {}", exchange.getRequest().getURI());
                    return getUnauthorizedResponse(exchange, "Missing Authorization header");
                }

                // Validate Bearer token format
                if (!authHeader.startsWith("Bearer ")) {
                    log.warn("Invalid token format for request: {}", exchange.getRequest().getURI());
                    return getUnauthorizedResponse(exchange, "Invalid token format. Expected 'Bearer <token>'");
                }

                // Extract token
                String token = authHeader.substring(7);

                // Validate JWT token
                if (!jwtUtil.isTokenValid(token)) {
                    log.warn("Invalid or expired token for request: {}", exchange.getRequest().getURI());
                    return getUnauthorizedResponse(exchange, "Invalid or expired token");
                }

                // Extract username from token for logging/tracking
                String username = jwtUtil.extractUsername(token);
                log.info("✅ JWT Token validated for user: {} | Path: {}", username, exchange.getRequest().getURI());

            } catch (Exception e) {
                log.error("JWT validation error: {}", e.getMessage());
                return getUnauthorizedResponse(exchange, "JWT validation failed: " + e.getMessage());
            }

            return chain.filter(modifiedExchange != null ? modifiedExchange : exchange);
        };
    }

    /**
     * Return 401 Unauthorized response
     */
    private Mono<Void> getUnauthorizedResponse(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");
        
        String body = "{\"error\": \"Unauthorized\", \"message\": \"" + message + "\"}";
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(body.getBytes()))
        );
    }

    public static class Config {
        // Configuration class for filter properties
    }
}
