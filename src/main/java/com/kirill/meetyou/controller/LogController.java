package com.kirill.meetyou.controller;

import com.kirill.meetyou.dto.LogTask;
import com.kirill.meetyou.enums.LogTaskStatus;
import com.kirill.meetyou.service.LogGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
@Tag(name = "Логирование", description = "Эндпоинты для получения лог-файлов")
@Slf4j
public class LogController {
    private final LogGenerationService service;
    private static final String LOGS_DIR = "logs/"; // Добавьте это в константы

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Operation(summary = "Получить лог-файл по дате",
            description = "Возвращает .log файл, сформированный в указанный день (формат: yyyy-MM-dd)")
    @GetMapping("/{date}")
    public ResponseEntity<InputStreamResource> getLogsByDate(
            @PathVariable String date) {
        try {
            LocalDate localDate = LocalDate.parse(date, DATE_FORMATTER);
            Path path = Paths.get(LOGS_DIR + "app-" + DATE_FORMATTER.format(localDate) + ".log")
                    .toAbsolutePath()
                    .normalize();

            // Проверка, что путь находится в разрешённой директории
            if (!path.startsWith(Paths.get(LOGS_DIR).toAbsolutePath())) {
                return ResponseEntity.badRequest().build();
            }

            if (!Files.exists(path)) {
                return ResponseEntity.notFound().build();
            }

            InputStreamResource resource = new InputStreamResource(new FileInputStream(path.toFile()));

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + path.getFileName())
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(resource);
        } catch (Exception e) {
            log.error("Ошибка при получении логов: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get("logs"));
            log.info("Директория для логов создана");
        } catch (IOException e) {
            log.error("Ошибка при создании директории логов: {}", e.getMessage());
        }
    }

    @Operation(summary = "Запустить асинхронную генерацию логов")
    @PostMapping("/{from}/{to}")
    public ResponseEntity<String> startGeneration(
            @PathVariable String from,
            @PathVariable String to) {
        try {
            String id = service.startTask(from, to);
            return ResponseEntity.ok(id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Ошибка: " + e.getMessage());
        }
    }

    @Operation(summary = "Получить статус задачи генерации логов")
    @GetMapping("/status/{id}")
    public ResponseEntity<LogTask> getStatus(@PathVariable String id) {
        LogTask task = service.getStatus(id);
        if (task == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(task);
    }

    @Operation(summary = "Скачать сгенерированный лог")
    @GetMapping("/result/{id}")
    public ResponseEntity<InputStreamResource> getFile(@PathVariable String id) {
        LogTask task = service.getStatus(id);

        if (task == null || task.getStatus() != LogTaskStatus.SUCCESS) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path path = Paths.get(task.getFilePath()).toAbsolutePath().normalize();

            // Проверка существования файла
            if (!Files.exists(path)) {
                log.error("Файл не найден: {}", path);
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + path.getFileName())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(new FileInputStream(path.toFile())));
        } catch (Exception e) {
            log.error("Ошибка при скачивании файла: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}