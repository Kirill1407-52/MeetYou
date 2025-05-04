package com.kirill.meetyou.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LogService {

    @Value("${logging.file.path:./logs}")
    private String logDirectory;

    public Resource getLogFileByDate(LocalDate date) throws IOException {
        String filename = String.format("meetyou-app-%s.log", date);
        Path filePath = Paths.get(logDirectory, "archived", filename);

        try {
            // Сначала проверяем архивную версию
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                // Если в архиве нет, проверяем текущий файл
                filePath = Paths.get(logDirectory, "meetyou-app.log");
                resource = new UrlResource(filePath.toUri());

                if (!resource.exists() || !resource.isReadable()) {
                    throw new IOException("Log file not found or not readable");
                }
            }

            return resource;

        } catch (MalformedURLException e) {
            log.error("Ошибка формирования URL для файла логов", e);
            throw new IOException("Failed to create URL for log file", e);
        }
    }
}