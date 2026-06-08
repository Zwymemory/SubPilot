package com.subpilot.module.health.controller;

import com.subpilot.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@Tag(name = "Health")
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Operation(summary = "Application health check")
    @GetMapping
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.success(Map.of(
                "status", "UP",
                "service", "SubPilot",
                "time", LocalDateTime.now()
        ));
    }
}
