package com.kirill.meetyou.service;

import com.kirill.meetyou.model.User;
import com.kirill.meetyou.repository.FriendRepository;
import com.kirill.meetyou.repository.Repository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendService {
    private static final String USER_WITH_ID_TEXT = "User with id ";
    private static final String NOT_FOUND_TEXT = " not found";

    private final Repository userRepository;
    private final FriendRepository friendRepository;

    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new IllegalArgumentException("User cannot be friend with himself");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(USER_WITH_ID_TEXT
                        + userId + NOT_FOUND_TEXT));
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new EntityNotFoundException(USER_WITH_ID_TEXT
                        + friendId + NOT_FOUND_TEXT));

        if (friendRepository.existsFriendship(userId, friendId)) {
            throw new IllegalStateException("Users are already friends");
        }

        user.addFriend(friend);
        userRepository.save(user);
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(USER_WITH_ID_TEXT
                        + userId + NOT_FOUND_TEXT));
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new EntityNotFoundException(USER_WITH_ID_TEXT
                        + friendId + NOT_FOUND_TEXT));

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