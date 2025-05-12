package com.kirill.meetyou.service;

import com.kirill.meetyou.cache.UserCache;
import com.kirill.meetyou.dto.BulkResponse;
import com.kirill.meetyou.dto.UserCreateDto;
import com.kirill.meetyou.model.User;
import com.kirill.meetyou.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCache cache;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserCreateDto testUserCreateDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setBirth(LocalDate.of(1990, 1, 1));
        testUser.setAge(33);

        testUserCreateDto = new UserCreateDto();
        testUserCreateDto.setName("New User");
        testUserCreateDto.setEmail("new@example.com");
        testUserCreateDto.setBirth(LocalDate.of(1995, 5, 5));
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        // Arrange
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        // Act
        List<User> result = userService.findAll();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testUser, result.get(0));
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void findById_WithValidId_ShouldReturnUser() {
        // Arrange
        when(cache.get(1L)).thenReturn(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(cache, times(1)).get(1L);
        verify(userRepository, times(1)).findById(1L);
        verify(cache, times(1)).put(1L, testUser);
    }

    @Test
    void findById_WithCachedUser_ShouldReturnUserFromCache() {
        // Arrange
        when(cache.get(1L)).thenReturn(testUser);

        // Act
        Optional<User> result = userService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(cache, times(1)).get(1L);
        verify(userRepository, never()).findById(anyLong());
        verify(cache, never()).put(anyLong(), any());
    }

    @Test
    void findById_WithInvalidId_ShouldThrowException() {
        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.findById(-1L));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Некорректный ID пользователя", exception.getReason());
    }

    @Test
    void create_WithValidUser_ShouldSaveAndCacheUser() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.create(testUser);

        // Assert
        assertEquals(testUser, result);
        verify(userRepository, times(1)).save(testUser);
        verify(cache, times(1)).put(testUser.getId(), testUser);
    }

    @Test
    void create_WithDuplicateEmail_ShouldThrowException() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.create(testUser));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Пользователь с таким email уже существует", exception.getReason());
    }

    @Test
    void create_WithEmptyEmail_ShouldThrowException() {
        // Arrange
        testUser.setEmail("");

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.create(testUser));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Email пользователя не может быть пустым", exception.getReason());
    }

    @Test
    void create_WithFutureBirthDate_ShouldThrowException() {
        // Arrange
        testUser.setBirth(LocalDate.now().plusDays(1));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.create(testUser));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Некорректная дата рождения", exception.getReason());
    }

    @Test
    void delete_WithValidId_ShouldDeleteAndRemoveFromCache() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);

        // Act
        userService.delete(1L);

        // Assert
        verify(userRepository, times(1)).deleteById(1L);
        verify(cache, times(1)).remove(1L);
    }

    @Test
    void delete_WithNonExistentId_ShouldThrowException() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.delete(1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Пользователь с id: 1 не найден", exception.getReason());
    }

    @Test
    void delete_WithInvalidId_ShouldThrowException() {
        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.delete(-1L));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Некорректный ID пользователя", exception.getReason());
    }

    @Test
    void update_WithValidData_ShouldUpdateAndCacheUser() {
        // Arrange
        String newEmail = "new@example.com";
        String newName = "New Name";

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail(newEmail)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.update(1L, newEmail, newName);

        // Assert
        assertEquals(testUser, result);
        assertEquals(newEmail, testUser.getEmail());
        assertEquals(newName, testUser.getName());
        verify(cache, times(1)).put(1L, testUser);
    }

    @Test
    void update_WithEmptyName_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.update(1L, "valid@email.com", ""));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Имя не может быть пустым", exception.getReason());
    }

    @Test
    void update_WithDuplicateEmail_ShouldThrowException() {
        // Arrange
        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setEmail("existing@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.update(1L, "existing@example.com", "Valid Name"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Пользователь с таким email уже существует", exception.getReason());
    }

    @Test
    void bulkCreate_WithValidUsers_ShouldReturnSuccessCount() {
        // Arrange
        User savedUser = new User();
        savedUser.setId(1L); // Устанавливаем ID
        savedUser.setName(testUserCreateDto.getName());
        savedUser.setEmail(testUserCreateDto.getEmail());
        savedUser.setBirth(testUserCreateDto.getBirth());
        savedUser.setAge(30);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        BulkResponse response = userService.bulkCreate(List.of(testUserCreateDto));

        // Assert
        assertEquals(1, response.getSuccessCount());
        assertEquals(0, response.getFailCount());
        assertTrue(response.getErrors().isEmpty());
        verify(cache).put(1L, savedUser); // Проверяем с конкретным ID
    }

    @Test
    void create_WithTodayBirthDate_ShouldSuccess() {
        User user = new User();
        user.setEmail("valid@email.com");
        user.setBirth(LocalDate.now());
        user.setName("Valid Name");

        when(userRepository.save(any())).thenReturn(user);

        User result = userService.create(user);
        assertNotNull(result);
    }

    @Test
    void update_WithSameEmail_ShouldNotThrow() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("existing@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        assertDoesNotThrow(() -> userService.update(1L, "existing@example.com", "New Name"));
    }

    @Test
    void create_WithMinimalAge_ShouldCalculateCorrectly() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setBirth(LocalDate.now().minusYears(18));
        user.setName("Adult User");

        when(userRepository.save(any())).thenReturn(user);

        User result = userService.create(user);
        assertEquals(18, result.getAge());
    }

    @Test
    void update_WithNullName_ShouldNotUpdateName() {
        // Arrange
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("Original Name");
        existingUser.setEmail("original@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.update(1L, null, null);

        // Assert
        assertNotNull(result); // Проверяем, что результат не null
        assertEquals("Original Name", result.getName()); // Имя не должно измениться
        assertEquals("original@example.com", result.getEmail()); // Email не должен измениться
    }

    @Test
    void bulkCreate_WithInvalidUser_ShouldReturnFailCount() {
        // Arrange
        testUserCreateDto.setEmail(""); // Невалидный email

        // Act
        BulkResponse response = userService.bulkCreate(List.of(testUserCreateDto));

        // Assert
        assertEquals(0, response.getSuccessCount());
        assertEquals(1, response.getFailCount());
        assertFalse(response.getErrors().isEmpty());
        verify(cache, never()).put(anyLong(), any()); // Проверяем, что кэш не обновлялся
    }
}