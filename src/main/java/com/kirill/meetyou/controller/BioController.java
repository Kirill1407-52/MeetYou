package com.kirill.meetyou.controller;

import com.kirill.meetyou.dto.BioDto.CreateRequest;
import com.kirill.meetyou.dto.BioDto.Response;
import com.kirill.meetyou.dto.BioDto.UpdateBioRequest;
import com.kirill.meetyou.dto.BioDto.UpdateInterestFactRequest;
import com.kirill.meetyou.service.BioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}")
@RequiredArgsConstructor
@Tag(name = "Управление биографией",
        description = "API для управления биографией и интересными фактами пользователей")
public class BioController {
    private final BioService bioService;

    @PostMapping("/bio")
    @Operation(summary = "Создать биографию пользователя",
            description = "Создает новую биографию для указанного пользователя")
    @ApiResponse(responseCode = "201", description = "Биография успешно создана")
    public ResponseEntity<Response> createUserBio(
            @PathVariable Long userId,
            @Valid @RequestBody CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bioService.createUserBio(userId, request));
    }

    @GetMapping("/bio")
    @Operation(summary = "Получить биографию пользователя",
            description = "Возвращает текст биографии указанного пользователя")
    @ApiResponse(responseCode = "200", description = "Биография успешно получена")
    public ResponseEntity<String> getBio(@PathVariable Long userId) {
        return ResponseEntity.ok(bioService.getBioByUserId(userId));
    }

    @GetMapping("/interest_fact")
    @Operation(summary = "Получить интересный факт",
            description = "Возвращает интересный факт указанного пользователя")
    @ApiResponse(responseCode = "200", description = "Интересный факт успешно получен")
    public ResponseEntity<String> getInterestFact(@PathVariable Long userId) {
        return ResponseEntity.ok(bioService.getInterestFactByUserId(userId));
    }

    @GetMapping("/bioall")
    @Operation(summary = "Получить полную биографию",
            description = "Возвращает полную информацию о биографии, включая интересный факт")
    @ApiResponse(responseCode = "200", description = "Полная биография успешно получена")
    public ResponseEntity<Response> getFullBio(@PathVariable Long userId) {
        return ResponseEntity.ok(bioService.getFullBioByUserId(userId));
    }

    @PutMapping("/bio")
    @Operation(summary = "Обновить биографию",
            description = "Обновляет биографию указанного пользователя")
    @ApiResponse(responseCode = "200", description = "Биография успешно обновлена")
    public ResponseEntity<Response> updateBio(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateBioRequest request) {
        return ResponseEntity.ok(bioService.updateBio(userId, request));
    }

    @PutMapping("/interest_fact")
    @Operation(summary = "Обновить интересный факт",
            description = "Обновляет интересный факт указанного пользователя")
    @ApiResponse(responseCode = "200", description = "Интересный факт успешно обновлен")
    public ResponseEntity<Response> updateInterestFact(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateInterestFactRequest request) {
        return ResponseEntity.ok(bioService.updateInterestFact(userId, request));
    }

    @DeleteMapping("/bio")
    @Operation(summary = "Удалить биографию",
            description = "Удаляет биографию указанного пользователя")
    @ApiResponse(responseCode = "204", description = "Биография успешно удалена")
    public ResponseEntity<Void> deleteUserBio(@PathVariable Long userId) {
        bioService.deleteUserBio(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/interest_fact")
    @Operation(summary = "Удалить интересный факт",
            description = "Удаляет интересный факт указанного пользователя")
    @ApiResponse(responseCode = "204", description = "Интересный факт успешно удален")
    public ResponseEntity<Void> deleteInterestFact(@PathVariable Long userId) {
        bioService.deleteInterestFact(userId);
        return ResponseEntity.noContent().build();
    }
}