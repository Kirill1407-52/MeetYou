package com.kirill.meetyou.service;

import com.kirill.meetyou.model.User;
import com.kirill.meetyou.repository.FriendRepository;
import com.kirill.meetyou.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendServiceTest {

    private static final String USER_WITH_ID_TEXT = "User with id ";
    private static final String NOT_FOUND_TEXT = " not found";

    @Mock
    private UserRepository userRepository;

    @Mock
    private FriendRepository friendRepository;

    @InjectMocks
    private FriendService friendService;

    private User testUser;
    private User testFriend;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");

        testFriend = new User();
        testFriend.setId(2L);
        testFriend.setName("Test Friend");
    }

    @Test
    void addFriend_ShouldAddFriendSuccessfully() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testFriend));
        when(friendRepository.existsFriendship(1L, 2L)).thenReturn(false);

        assertDoesNotThrow(() -> friendService.addFriend(1L, 2L));

        verify(userRepository, times(2)).findById(anyLong());
        verify(friendRepository).existsFriendship(1L, 2L);
        verify(userRepository).save(testUser);
        assertTrue(testUser.getFriends().contains(testFriend));
    }

    @Test
    void addFriend_WithSameUser_ShouldThrowException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> friendService.addFriend(1L, 1L));

        assertEquals("User cannot be friend with himself", ex.getMessage());
        verifyNoInteractions(userRepository, friendRepository);
    }

    @Test
    void addFriend_UserNotFound_ShouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> friendService.addFriend(1L, 2L));

        assertTrue(ex.getMessage().contains(USER_WITH_ID_TEXT + "1" + NOT_FOUND_TEXT));
        verify(userRepository).findById(1L);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(friendRepository);
    }

    @Test
    void addFriend_FriendNotFound_ShouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> friendService.addFriend(1L, 2L));

        assertTrue(ex.getMessage().contains(USER_WITH_ID_TEXT + "2" + NOT_FOUND_TEXT));
        verify(userRepository, times(2)).findById(anyLong());
        verifyNoInteractions(friendRepository);
    }

    @Test
    void addFriend_AlreadyFriends_ShouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testFriend));
        when(friendRepository.existsFriendship(1L, 2L)).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> friendService.addFriend(1L, 2L));

        assertEquals("Users are already friends", ex.getMessage());
        verify(friendRepository).existsFriendship(1L, 2L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void removeFriend_ShouldRemoveFriendSuccessfully() {
        testUser.addFriend(testFriend);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testFriend));
        when(friendRepository.existsFriendship(1L, 2L)).thenReturn(true);

        assertDoesNotThrow(() -> friendService.removeFriend(1L, 2L));

        verify(userRepository, times(2)).findById(anyLong());
        verify(friendRepository).existsFriendship(1L, 2L);
        verify(userRepository).save(testUser);
        assertFalse(testUser.getFriends().contains(testFriend));
    }

    @Test
    void removeFriend_UserNotFound_ShouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> friendService.removeFriend(1L, 2L));

        assertTrue(ex.getMessage().contains(USER_WITH_ID_TEXT + "1" + NOT_FOUND_TEXT));
        verify(userRepository).findById(1L);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(friendRepository);
    }

    @Test
    void removeFriend_FriendNotFound_ShouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> friendService.removeFriend(1L, 2L));

        assertTrue(ex.getMessage().contains(USER_WITH_ID_TEXT + "2" + NOT_FOUND_TEXT));
        verify(userRepository, times(2)).findById(anyLong());
        verifyNoInteractions(friendRepository);
    }

    @Test
    void removeFriend_NotFriends_ShouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testFriend));
        when(friendRepository.existsFriendship(1L, 2L)).thenReturn(false);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> friendService.removeFriend(1L, 2L));

        assertEquals("Users are not friends", ex.getMessage());
        verify(friendRepository).existsFriendship(1L, 2L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void getAllFriends_ShouldReturnFriendsList() {
        when(friendRepository.findFriendsOfUser(1L)).thenReturn(List.of(testFriend));

        List<User> result = assertDoesNotThrow(() -> friendService.getAllFriends(1L));

        assertEquals(1, result.size());
        assertEquals(testFriend, result.get(0));
        verify(friendRepository).findFriendsOfUser(1L);
    }

    @Test
    void getAllFriends_NoFriends_ShouldReturnEmptyList() {
        when(friendRepository.findFriendsOfUser(1L)).thenReturn(List.of());

        List<User> result = friendService.getAllFriends(1L);

        assertTrue(result.isEmpty());
        verify(friendRepository).findFriendsOfUser(1L);
    }

    @Test
    void checkFriendship_WhenFriends_ShouldReturnTrue() {
        when(friendRepository.existsFriendship(1L, 2L)).thenReturn(true);

        boolean result = friendService.checkFriendship(1L, 2L);

        assertTrue(result);
        verify(friendRepository).existsFriendship(1L, 2L);
    }

    @Test
    void checkFriendship_WhenNotFriends_ShouldReturnFalse() {
        when(friendRepository.existsFriendship(1L, 2L)).thenReturn(false);

        boolean result = friendService.checkFriendship(1L, 2L);

        assertFalse(result);
        verify(friendRepository).existsFriendship(1L, 2L);
    }

    @Test
    void removeFriend_VerifyFriendshipCheck() {
        testUser.addFriend(testFriend);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testFriend));
        when(friendRepository.existsFriendship(1L, 2L)).thenReturn(true);

        friendService.removeFriend(1L, 2L);

        verify(friendRepository).existsFriendship(1L, 2L);
    }
}