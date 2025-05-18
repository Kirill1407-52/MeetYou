package com.kirill.meetyou.service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LogService {
    private final Map<String, String> taskStatusMap = new ConcurrentHashMap<>();
    private final Map<String, Path> taskFileMap = new ConcurrentHashMap<>();

    @Value("${logging.file.path:./logs}")
    private String logDirectory;

    // Self-injection
    private LogService self;

    @Autowired
    public void setSelf(LogService self) {
        this.self = self;
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(getGeneratedLogPath());
            Files.createDirectories(getArchivedLogPath());
            log.info("Log directories initialized at: {}", logDirectory);
        } catch (IOException e) {
            log.error("Failed to create log directories", e);
        }
    }

    private Path getGeneratedLogPath() {
        return Paths.get(logDirectory, "generated");
    }

    private Path getArchivedLogPath() {
        return Paths.get(logDirectory, "archived");
    }

    @Async
    public void processLogCreation(String taskId) {
        try {
            taskStatusMap.put(taskId, "IN_PROGRESS");
            Path filePath = getGeneratedLogPath().resolve("meetyou-log-" + taskId + ".log");

            String logContent = String.format(
                    "Log created at: %s%nTask ID: %s%n",
                    LocalDateTime.now(),
                    taskId
            );

            Files.write(filePath, logContent.getBytes(), StandardOpenOption.CREATE);
            taskFileMap.put(taskId, filePath);
            taskStatusMap.put(taskId, "COMPLETED");
            log.info("Log file created: {}", filePath);

        } catch (Exception e) {
            log.error("Failed to create log file for task: {}", taskId, e);
            taskStatusMap.put(taskId, "FAILED");
        }
    }

    public String createLogTask() {
        String taskId = UUID.randomUUID().toString();
        taskStatusMap.put(taskId, "PENDING");
        // Call through the self-injected proxy
        self.processLogCreation(taskId);
        return taskId;
    }

    public String getTaskStatus(String taskId) {
        return taskStatusMap.getOrDefault(taskId, "NOT_FOUND");
    }

    public Resource getLogFileByTaskId(String taskId) throws IOException {
        String status = taskStatusMap.get(taskId);
        if (!"COMPLETED".equals(status)) {
            throw new IOException("Log file not ready. Current status: "
                    + (status != null ? status : "NOT_FOUND"));
        }

        Path filePath = taskFileMap.get(taskId);
        if (filePath == null || !Files.exists(filePath)) {
            throw new IOException("Log file not found for task: " + taskId);
        }

        return new UrlResource(filePath.toUri());
    }

    public Resource getLogFileByDate(LocalDate date) throws IOException {
        Path filePath = getArchivedLogPath().resolve(
                String.format("meetyou-app-%s.log", date));

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }

            filePath = Paths.get(logDirectory, "meetyou-app.log");
            resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new IOException("No log file available for date: " + date);
            }

            return resource;

        } catch (MalformedURLException e) {
            throw new IOException("Invalid log file path format", e);
        }
    }
}