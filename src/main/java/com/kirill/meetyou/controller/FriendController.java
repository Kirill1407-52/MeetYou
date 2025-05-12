package com.kirill.meetyou.controller;

import com.kirill.meetyou.model.User;
import com.kirill.meetyou.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
@Tag(name = "Управление друзьями",
        description = "API для управления дружескими связями пользователей")
public class FriendController {
    private final FriendService friendService;

    @PostMapping("/{userId}/add/{friendId}")
    @Operation(summary = "Добавить друга",
            description = "Устанавливает дружескую связь между двумя пользователями")
    @ApiResponse(responseCode = "200", description = "Друг успешно добавлен")
    public ResponseEntity<Void> addFriend(
            @PathVariable Long userId,
            @PathVariable Long friendId) {
        friendService.addFriend(userId, friendId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/remove/{friendId}")
    @Operation(summary = "Удалить друга",
            description = "Удаляет дружескую связь между двумя пользователями")
    @ApiResponse(responseCode = "200", description = "Друг успешно удален")
    public ResponseEntity<Void> removeFriend(
            @PathVariable Long userId,
            @PathVariable Long friendId) {
        friendService.removeFriend(userId, friendId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/all")
    @Operation(summary = "Получить всех друзей",
            description = "Возвращает список всех друзей указанного пользователя")
    @ApiResponse(responseCode = "200", description = "Список друзей успешно получен")
    public ResponseEntity<List<User>> getAllFriends(@PathVariable Long userId) {
        return ResponseEntity.ok(friendService.getAllFriends(userId));
    }

    @GetMapping("/{userId}/check/{friendId}")
    @Operation(summary = "Проверить дружбу",
            description = "Проверяет наличие дружеской связи между двумя пользователями")
    @ApiResponse(responseCode = "200", description = "Статус дружбы успешно получен")
    public ResponseEntity<Boolean> checkFriendship(
            @PathVariable Long userId,
            @PathVariable Long friendId) {
        return ResponseEntity.ok(friendService.checkFriendship(userId, friendId));
    }
}