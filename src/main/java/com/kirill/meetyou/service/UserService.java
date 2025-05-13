package com.kirill.meetyou.service;

import com.kirill.meetyou.cache.UserCache;
import com.kirill.meetyou.dto.BulkResponse;
import com.kirill.meetyou.dto.UserCreateDto;
import com.kirill.meetyou.model.User;
import com.kirill.meetyou.repository.UserRepository;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserCache cache;

    public UserService(UserRepository userRepository, UserCache cache) {
        this.userRepository = userRepository;
        this.cache = cache;
    }

    public List<User> findAll() {
        try {
            log.debug("Fetching all users");
            return userRepository.findAll();
        } catch (Exception e) {
            log.error("Failed to fetch all users. Error: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error while fetching users");
        }
    }

    public Optional<User> findById(Long id) {
        try {
            if (id == null || id <= 0) {
                log.warn("Invalid user ID requested: {}", id);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Некорректный ID пользователя");
            }

            log.debug("⚡ [Cache Check] Checking cache for user ID: {}", id);
            User cachedUser = cache.get(id);
            if (cachedUser != null) {
                log.info("✅ [Cache Hit] Successfully retrieved user {} FROM CACHE", id);
                log.debug("📦 Cached user details: {}", cachedUser);
                return Optional.of(cachedUser);
            }

            log.info("⏳ [Cache Miss] User {} not found in cache, querying database...", id);
            Optional<User> userOptional = userRepository.findById(id);

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                log.debug("🔧 [Cache Update] Caching user {}", id);
                cache.put(id, user);
                log.info("📥 [Cache Store] Stored user {} in cache", id);
            } else {
                log.debug("⚠ [Cache Skip] User {} not found in database - nothing to cache", id);
            }

            return userOptional;
        } catch (Exception e) {
            log.error("Failed to find user with ID: {}. Error: {}", id, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error while finding user");
        }
    }

    public User create(User user) {
        try {
            log.info("Creating new user with email: {}", user != null ? user.getEmail() : "null");

            validateUserForCreation(user);

            user.setAge(Period.between(user.getBirth(), LocalDate.now()).getYears());
            User savedUser = userRepository.save(user);

            log.debug("🔧 [Cache Update] Caching newly created user {}", savedUser.getId());
            cache.put(savedUser.getId(), savedUser);
            log.info("📥 [Cache Store] Stored new user {} in cache", savedUser.getId());

            return savedUser;
        } catch (ResponseStatusException e) {
            throw e; // Re-throw validation or known errors
        } catch (Exception e) {
            log.error("Failed to create user with email: {}. Error: {}", user != null ? user.getEmail() : "null", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Не удалось создать пользователя из-за некорректных данных или существующего email");
        }
    }

    public void delete(Long id) {
        try {
            log.info("Deleting user with ID: {}", id);

            validateUserId(id);

            if (!userRepository.existsById(id)) {
                log.warn("Attempt to delete non-existent user: {}", id);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Пользователь с id: " + id + " не найден");
            }

            log.debug("🗑 [Cache Remove] Removing user {} from cache", id);
            cache.remove(id);
            userRepository.deleteById(id);
            log.info("Successfully deleted user: {}", id);
        } catch (Exception e) {
            log.error("Failed to delete user with ID: {}. Error: {}", id, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error while deleting user");
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    @Transactional
    public User update(Long id, String email, String name) {
        try {
            log.info("Updating user {} with email: {}, name: {}", id, email, name);

            validateUserId(id);

            User user = userRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("User not found for update: {}", id);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Пользователь с id: " + id + " не найден");
                    });

            updateUserEmail(user, email);
            updateUserName(user, name);

            User updatedUser = userRepository.save(user);

            log.debug("🔧 [Cache Update] Updating cache for user {}", id);
            cache.put(id, updatedUser);
            log.info("📥 [Cache Store] Updated user {} in cache", id);

            return updatedUser;
        } catch (Exception e) {
            log.error("Failed to update user with ID: {}. Error: {}", id, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error while updating user");
        }
    }

    private void validateUserForCreation(User user) {
        if (user == null) {
            log.error("Attempt to create null user");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Объект пользователя не может быть null");
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty() || user.getEmail().trim().equalsIgnoreCase("null")) {
            log.error("Attempt to create user with null, empty, or 'null' email");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Email пользователя не может быть пустым, null или 'null'");
        }

        if (user.getName() == null || user.getName().trim().isEmpty()) {
            log.error("Attempt to create user with null or empty name");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Имя пользователя не может быть пустым или null");
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            log.warn("Duplicate email attempt: {}", user.getEmail());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Пользователь с таким email уже существует");
        }

        if (user.getBirth() == null || user.getBirth().isAfter(LocalDate.now())) {
            log.warn("Invalid birth date for user: {}", user.getBirth());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Некорректная дата рождения");
        }
    }

    private void validateUserId(Long id) {
        if (id == null || id <= 0) {
            log.warn("Invalid user ID: {}", id);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Некорректный ID пользователя");
        }
    }

    private void updateUserEmail(User user, String email) {
        if (email != null && !email.equals(user.getEmail())) {
            if (email.trim().isEmpty() || email.trim().equalsIgnoreCase("null")) {
                log.warn("Attempt to set empty or 'null' email for user: {}", user.getId());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Email не может быть пустым или 'null'");
            }
            if (userRepository.findByEmail(email).isPresent()) {
                log.warn("Duplicate email attempt during update: {}", email);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Пользователь с таким email уже существует");
            }
            user.setEmail(email);
            log.debug("Updated email for user: {}", user.getId());
        }
    }

    private void updateUserName(User user, String name) {
        if (name != null) {
            if (name.trim().isEmpty()) {
                log.warn("Attempt to set empty name for user: {}", user.getId());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Имя не может быть пустым");
            }
            user.setName(name);
            log.debug("Updated name for user: {}", user.getId());
        }
    }

    @Transactional
    public BulkResponse bulkCreate(List<UserCreateDto> userDtos) {
        try {
            if (userDtos == null) {
                log.error("Attempt to bulk create with null user DTO list");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Список пользователей не может быть null");
            }

            BulkResponse.BulkResponseBuilder responseBuilder = BulkResponse.builder()
                    .successCount(0)
                    .failCount(0)
                    .errors(new ArrayList<>());

            userDtos.forEach(dto -> {
                try {
                    User user = new User();
                    user.setName(dto.getName());
                    user.setEmail(dto.getEmail());
                    user.setBirth(dto.getBirth());

                    validateUserForCreation(user); // Добавляем валидацию

                    user.setAge(Period.between(dto.getBirth(), LocalDate.now()).getYears());
                    User savedUser = userRepository.save(user);
                    cache.put(savedUser.getId(), savedUser);
                    responseBuilder.successCount(responseBuilder.build().getSuccessCount() + 1);
                } catch (ResponseStatusException e) {
                    responseBuilder.failCount(responseBuilder.build().getFailCount() + 1);
                    responseBuilder.errors(List.of("Ошибка при создании пользователя с email " + dto.getEmail() + ": " + e.getReason()));
                } catch (Exception e) {
                    responseBuilder.failCount(responseBuilder.build().getFailCount() + 1);
                    responseBuilder.errors(List.of("Ошибка при создании пользователя с email " + dto.getEmail() + ": " + e.getMessage()));
                }
            });

            return responseBuilder.build();
        } catch (Exception e) {
            log.error("Failed to bulk create users. Error: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error during bulk user creation");
        }
    }
}