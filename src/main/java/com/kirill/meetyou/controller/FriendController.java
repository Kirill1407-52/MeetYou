package com.kirill.meetyou.controller;

import com.kirill.meetyou.model.User;
import com.kirill.meetyou.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
@Tag(name = "Friend Management", description = "APIs for managing user friendships")
public class FriendController {
    private final FriendService friendService;

    @PostMapping("/{userId}/add/{friendId}")
    @Operation(summary = "Add friend", description = "Establishes a friendship between two users")
    @ApiResponse(responseCode = "200", description = "Friend added successfully")
    public ResponseEntity<Void> addFriend(
            @PathVariable Long userId,
            @PathVariable Long friendId) {
        friendService.addFriend(userId, friendId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/remove/{friendId}")
    @Operation(summary = "Remove friend", description = "Removes a friendship between two users")
    @ApiResponse(responseCode = "200", description = "Friend removed successfully")
    public ResponseEntity<Void> removeFriend(
            @PathVariable Long userId,
            @PathVariable Long friendId) {
        friendService.removeFriend(userId, friendId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/all")
    @Operation(summary = "Get all friends", description = "Retrieves all friends of the specified user")
    @ApiResponse(responseCode = "200", description = "Friends list retrieved successfully")
    public ResponseEntity<List<User>> getAllFriends(@PathVariable Long userId) {
        return ResponseEntity.ok(friendService.getAllFriends(userId));
    }

    @GetMapping("/{userId}/check/{friendId}")
    @Operation(summary = "Check friendship", description = "Checks if a friendship exists between two users")
    @ApiResponse(responseCode = "200", description = "Friendship status retrieved successfully")
    public ResponseEntity<Boolean> checkFriendship(
            @PathVariable Long userId,
            @PathVariable Long friendId) {
        return ResponseEntity.ok(friendService.checkFriendship(userId, friendId));
    }
}