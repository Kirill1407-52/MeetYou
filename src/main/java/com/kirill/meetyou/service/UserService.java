package com.kirill.meetyou.service;

import com.kirill.meetyou.cache.UserCache;
import com.kirill.meetyou.model.User;
import com.kirill.meetyou.repository.UserRepository;
import java.time.LocalDate;
import java.time.Period;
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
        log.debug("Fetching all users");
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        if (id == null || id <= 0) {
            log.warn("Invalid user ID requested: {}", id);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Некорректный ID пользователя");
        }

        if (cache.contains(id)) {
            log.debug("Retrieving user {} from cache", id);
            return Optional.ofNullable(cache.get(id));
        }

        log.debug("Fetching user {} from database", id);
        Optional<User> userOptional = userRepository.findById(id);
        userOptional.ifPresent(user -> {
            cache.put(id, user);
            log.debug("Cached user {}", id);
        });
        return userOptional;
    }

    public User create(User user) {
        log.info("Creating new user with email: {}", user.getEmail());

        // Проверка email (основная бизнес-логика)
        if (user.getEmail().isEmpty()) {
            log.error("Attempt to create user with empty email");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Email пользователя не может быть пустым");
        }

        // Проверка уникальности email
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            log.warn("Duplicate email attempt: {}", user.getEmail());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Пользователь с таким email уже существует");
        }

        // Валидация даты рождения
        if (user.getBirth() == null || user.getBirth().isAfter(LocalDate.now())) {
            log.warn("Invalid birth date for user: {}", user.getBirth());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Некорректная дата рождения");
        }

        user.setAge(Period.between(user.getBirth(), LocalDate.now()).getYears());
        User savedUser = userRepository.save(user);
        cache.put(savedUser.getId(), savedUser);
        log.info("Created new user with ID: {}", savedUser.getId());

        return savedUser;
    }

    public void delete(Long id) {
        log.info("Deleting user with ID: {}", id);

        if (id == null || id <= 0) {
            log.warn("Invalid deletion attempt with ID: {}", id);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Некорректный ID пользователя");
        }

        if (!userRepository.existsById(id)) {
            log.warn("Attempt to delete non-existent user: {}", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Пользователь с id: " + id + " не найден");
        }

        userRepository.deleteById(id);
        cache.remove(id);
        log.info("Successfully deleted user: {}", id);
    }

    @Transactional
    public void update(Long id, String email, String name) {
        log.info("Updating user {} with email: {}, name: {}", id, email, name);

        if (id == null || id <= 0) {
            log.warn("Invalid update attempt with ID: {}", id);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Некорректный ID пользователя");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found for update: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Пользователь с id: " + id + " не найден");
                });

        if (email != null && !email.equals(user.getEmail())) {
            if (email.isEmpty()) {
                log.warn("Attempt to set empty email for user: {}", id);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Email не может быть пустым");
            }
            if (userRepository.findByEmail(email).isPresent()) {
                log.warn("Duplicate email attempt during update: {}", email);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Пользователь с таким email уже существует");
            }
            user.setEmail(email);
            log.debug("Updated email for user: {}", id);
        }

        if (name != null) {
            if (name.isEmpty()) {
                log.warn("Attempt to set empty name for user: {}", id);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Имя не может быть пустым");
            }
            user.setName(name);
            log.debug("Updated name for user: {}", id);
        }

        User updatedUser = userRepository.save(user);
        cache.put(id, updatedUser);
        log.info("Successfully updated user: {}", id);
    }
}