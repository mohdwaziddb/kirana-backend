package com.kiranastore.kirana.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping({"/", "/health"})
    public Map<String, Object> health() {
        return Map.of(
                "success", true,
                "message", "Kirana backend is running successfully",
                "status", "UP",
                "timestamp", Instant.now().toString()
        );
    }
}
