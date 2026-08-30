package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/estado")
public class StatusApiController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> getEstado() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "NovaDrogueria Backend API",
            "version", "1.0.0",
            "environment", "production-ready",
            "database", "MongoDB Replica Set (rs0)",
            "timestamp", LocalDateTime.now().toString()
        ));
    }
}
