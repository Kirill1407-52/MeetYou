package com.kirill.meetyou.service;

import com.kirill.meetyou.cache.UserCache;
import com.kirill.meetyou.model.User;
import com.kirill.meetyou.repository.Repository;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.stereotype.Service
public class Service {
    private final Repository repository;
    private final UserCache cache;

    public Service(Repository repository, UserCache cache) {
        this.repository = repository;
        this.cache = cache;
    }

    public List<User> findAll() {
        return repository.findAll();
    }

    public Optional<User> findById(Long id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Некорректный ID пользователя");
        }

        if (cache.contains(id)) {
            return Optional.ofNullable(cache.get(id));
        }

        Optional<User> userOptional = repository.findById(id);
        userOptional.ifPresent(user -> cache.put(id, user));
        return userOptional;
    }

    public User create(User user) {
        if (user == null || user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Не указан email пользователя");
        }

        Optional<User> optionalUser = repository.findByEmail(user.getEmail());
        if (optionalUser.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Пользователь с таким email уже существует");
        }

        if (user.getBirth() == null || user.getBirth().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Некорректная дата рождения");
        }

        user.setAge(Period.between(user.getBirth(), LocalDate.now()).getYears());
        User savedUser = repository.save(user);
        cache.put(savedUser.getId(), savedUser);
        return savedUser;
    }

    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Некорректный ID пользователя");
        }

        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Пользователь с id: " + id + " не найден");
        }

        repository.deleteById(id);
        cache.remove(id);
    }

    @Transactional
    public void update(Long id, String email, String name) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Некорректный ID пользователя");
        }

        User user = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Пользователь с id: " + id + " не найден"));

        if (email != null && !email.equals(user.getEmail())) {
            if (email.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Email не может быть пустым");
            }
            if (repository.findByEmail(email).isPresent()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Пользователь с таким email уже существует");
            }
            user.setEmail(email);
        }

        if (name != null) {
            if (name.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Имя не может быть пустым");
            }
            user.setName(name);
        }

        User updatedUser = repository.save(user);
        cache.put(id, updatedUser);
    }
}