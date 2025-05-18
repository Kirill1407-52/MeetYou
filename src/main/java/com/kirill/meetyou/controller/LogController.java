package com.kirill.meetyou.controller;

import com.kirill.meetyou.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logs")
@Tag(name = "Log Management", description = "API для управления логами приложения")
@Slf4j
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @Operation(
            summary = "Загрузить лог по дате",
            description = "Позволяет скачать лог-файл за указанную дату в формате yyyy-MM-dd"
    )
    @ApiResponses({@ApiResponse(
                    responseCode = "200",
                    description = "Лог-файл успешно загружен"
            ), @ApiResponse(
                    responseCode = "404",
                    description = "Лог-файл не найден"
            ), @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера"
            )
    })
    @GetMapping("/{date}")
    public ResponseEntity<Resource> downloadLog(
            @Parameter(description = "Дата лог-файла в формате yyyy-MM-dd", example = "2023-12-01")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        try {
            Resource resource = logService.getLogFileByDate(date);
            String filename = String.format("meetyou-app-%s.log", date);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=" + filename)
                    .body(resource);

        } catch (IOException e) {
            if (e.getMessage().equals("Log file not found or not readable")) {
                log.warn("Log file not found for date: {}", date);
                return ResponseEntity.notFound().build();
            }
            log.error("Error while getting log file", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Создать лог-файл асинхронно")
    @PostMapping
    public ResponseEntity<Map<String, String>> createLog() {
        String taskId = logService.createLogTask();
        return ResponseEntity.accepted().body(Map.of("taskId", taskId));
    }

    @Operation(summary = "Проверить статус задачи")
    @GetMapping("/status/{taskId}")
    public ResponseEntity<Map<String, String>> getTaskStatus(
            @PathVariable String taskId) {
        String status = logService.getTaskStatus(taskId);
        return ResponseEntity.ok(Map.of("status", status));
    }

    @Operation(summary = "Скачать лог-файл по ID задачи")
    @GetMapping("/download/{taskId}")
    public ResponseEntity<Resource> downloadLogByTaskId(
            @PathVariable String taskId) throws IOException {
        Resource resource = logService.getLogFileByTaskId(taskId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=meetyou-log-" + taskId + ".log")
                .body(resource);
    }
}