package com.kirill.meetyou.controller;

import com.kirill.meetyou.model.User;
import com.kirill.meetyou.service.FriendService;
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
public class FriendController {
    private final FriendService friendService;

    @PostMapping("/{userId}/add/{friendId}")
    public ResponseEntity<Void> addFriend(
            @PathVariable Long userId,
            @PathVariable Long friendId) {
        friendService.addFriend(userId, friendId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/remove/{friendId}")
    public ResponseEntity<Void> removeFriend(
            @PathVariable Long userId,
            @PathVariable Long friendId) {
        friendService.removeFriend(userId, friendId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/all")
    public ResponseEntity<List<User>> getAllFriends(@PathVariable Long userId) {
        return ResponseEntity.ok(friendService.getAllFriends(userId));
    }

    @GetMapping("/{userId}/check/{friendId}")
    public ResponseEntity<Boolean> checkFriendship(
            @PathVariable Long userId,
            @PathVariable Long friendId) {
        return ResponseEntity.ok(friendService.checkFriendship(userId, friendId));
    }
}