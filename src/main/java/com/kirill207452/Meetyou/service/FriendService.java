package com.kirill207452.Meetyou.service;

import com.example.demo.model.User;
import com.example.demo.repository.FriendRepository;
import com.example.demo.repository.Repository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendService {
    private final Repository userRepository;
    private final FriendRepository friendRepository;

    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new IllegalArgumentException("User cannot be friend with himself");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + userId
                        + " not found"));
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + friendId
                        + " not found"));

        if (friendRepository.existsFriendship(userId, friendId)) {
            throw new IllegalStateException("Users are already friends");
        }

        user.addFriend(friend);
        userRepository.save(user);
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + userId
                        + " not found"));
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + friendId
                        + " not found"));

        if (!friendRepository.existsFriendship(userId, friendId)) {
            throw new IllegalStateException("Users are not friends");
        }

        user.removeFriend(friend);
        userRepository.save(user);
    }

    public List<User> getAllFriends(Long userId) {
        return friendRepository.findFriendsOfUser(userId);
    }

    public boolean checkFriendship(Long userId, Long friendId) {
        return friendRepository.existsFriendship(userId, friendId);
    }
}