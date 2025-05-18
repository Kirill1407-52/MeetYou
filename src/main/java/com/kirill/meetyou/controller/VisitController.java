package com.kirill.meetyou.controller;

import com.kirill.meetyou.dto.VisitResponse;
import com.kirill.meetyou.dto.VisitStatsResponse;
import com.kirill.meetyou.service.VisitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/visits")
@Tag(name = "Visit Tracker", description = "API для учета посещений")
@RequiredArgsConstructor
public class VisitController {

    private final VisitService visitService;

    @Operation(summary = "Зарегистрировать посещение")
    @GetMapping("/count")
    public ResponseEntity<VisitResponse> registerVisit() {
        return ResponseEntity.ok(visitService.registerVisit());
    }

    @Operation(summary = "Получить статистику посещений")
    @GetMapping("/stats")
    public ResponseEntity<VisitStatsResponse> getVisitStats() {
        return ResponseEntity.ok(visitService.getVisitStats());
    }
}