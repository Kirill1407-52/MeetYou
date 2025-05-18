package com.kirill.meetyou.controller;

import com.kirill.meetyou.dto.MessageDto;
import com.kirill.meetyou.service.MessageService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<MessageDto> sendMessage(
            @RequestParam Long senderId,
            @RequestParam Long receiverId,
            @RequestParam String content) {
        return ResponseEntity.ok(messageService.sendMessage(senderId, receiverId, content));
    }

    @GetMapping("/conversation")
    public ResponseEntity<List<MessageDto>> getConversation(
            @RequestParam Long user1Id,
            @RequestParam Long user2Id) {
        return ResponseEntity.ok(messageService.getConversation(user1Id, user2Id));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadMessagesCount(@RequestParam Long userId) {
        return ResponseEntity.ok(messageService.getUnreadMessagesCount(userId));
    }

    @PostMapping("/mark-as-read")
    public ResponseEntity<Void> markMessagesAsRead(
            @RequestParam Long userId,
            @RequestParam Long interlocutorId) {
        messageService.markMessagesAsRead(userId, interlocutorId);
        return ResponseEntity.ok().build();
    }
}