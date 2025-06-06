package com.kirill.meetyou.controller;

import com.kirill.meetyou.model.Interest;
import com.kirill.meetyou.service.InterestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/interests")
@RequiredArgsConstructor
@Tag(name = "Управление интересами", description = "API для управления интересами пользователей")
public class InterestController {
    private final InterestService interestService;

    @PostMapping
    @Operation(summary = "Добавить интерес",
            description = "Добавляет новый интерес для указанного пользователя")
    @ApiResponse(responseCode = "200", description = "Интерес успешно добавлен")
    public void addInterest(
            @PathVariable Long userId,
            @RequestParam String interestName) {
        interestService.addInterestToUser(userId, interestName);
    }

    @DeleteMapping
    @Operation(summary = "Удалить интерес",
            description = "Удаляет интерес у указанного пользователя")
    @ApiResponse(responseCode = "200", description = "Интерес успешно удален")
    public void removeInterest(
            @PathVariable Long userId,
            @RequestParam String interestName) {
        interestService.removeInterestFromUser(userId, interestName);
    }

    @GetMapping
    @Operation(summary = "Получить интересы пользователя",
            description = "Возвращает все интересы указанного пользователя")
    @ApiResponse(responseCode = "200", description = "Интересы успешно получены")
    public Set<Interest> getUserInterests(@PathVariable Long userId) {
        return interestService.getUserInterests(userId);
    }
}