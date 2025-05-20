package com.kirill.meetyou.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogGenerationServiceTest {

    @InjectMocks
    private LogGenerationService logGenerationService;

    @BeforeEach
    void setUp() {
        logGenerationService.setLogDirectory("/test/logs");
    }

    @Test
    void testGetLogFileFromArchive_Success() throws IOException {
        try (var ignored = mockConstruction(UrlResource.class, (mock, context) -> {
            when(mock.exists()).thenReturn(true);
            when(mock.isReadable()).thenReturn(true);
        })) {
            Resource result = logGenerationService.getLogFileByDate(LocalDate.now());
            assertNotNull(result);
            assertTrue(result.exists());
        }
    }

    @Test
    void testArchiveExistsButNotReadable() {
        try (var ignored = mockConstruction(UrlResource.class, (mock, context) -> {
            when(mock.exists()).thenReturn(true);
            when(mock.isReadable()).thenReturn(false);
        })) {
            IOException ex = assertThrows(IOException.class,
                    () -> logGenerationService.getLogFileByDate(LocalDate.now()));
            assertEquals("Log file not found or not readable", ex.getMessage());
        }
    }

    @Test
    void testFallbackToCurrentLog() throws IOException {
        try (var ignored = mockConstruction(UrlResource.class, (mock, context) -> {
            if (context.arguments().toString().contains("archived")) {
                when(mock.exists()).thenReturn(false);
            } else {
                when(mock.exists()).thenReturn(true);
                when(mock.isReadable()).thenReturn(true);
            }
        })) {
            Resource result = logGenerationService.getLogFileByDate(LocalDate.now());
            assertNotNull(result);
        }
    }

    @Test
    void testCurrentLogExistsButNotReadable() {
        try (var ignored = mockConstruction(UrlResource.class, (mock, context) -> {
            if (context.arguments().toString().contains("archived")) {
                when(mock.exists()).thenReturn(false);
            } else {
                when(mock.exists()).thenReturn(true);
                when(mock.isReadable()).thenReturn(false);
            }
        })) {
            IOException ex = assertThrows(IOException.class,
                    () -> logGenerationService.getLogFileByDate(LocalDate.now()));
            assertEquals("Log file not found or not readable", ex.getMessage());
        }
    }

    @Test
    void testBothFilesMissing() {
        try (var ignored = mockConstruction(UrlResource.class,
                (mock, context) -> when(mock.exists()).thenReturn(false))) {
            IOException ex = assertThrows(IOException.class,
                    () -> logGenerationService.getLogFileByDate(LocalDate.now()));
            assertEquals("Log file not found or not readable", ex.getMessage());
        }
    }

    @Test
    void testEmptyLogDirectory() {
        logGenerationService.setLogDirectory("");
        IOException ex = assertThrows(IOException.class,
                () -> logGenerationService.getLogFileByDate(LocalDate.now()));
        assertEquals("Log file not found or not readable", ex.getMessage());
    }
}