package com.prestek.davivienda.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequestMapping("/api/health")
@CrossOrigin(origins = "*")
@Slf4j
@RestController
public class HealthController {
    @GetMapping
    public Map<String, Object> health() {
        return Map.of("status", "UP");
    }
}
