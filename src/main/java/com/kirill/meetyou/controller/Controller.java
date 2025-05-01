package com.kirill.meetyou.controller;

import com.kirill.meetyou.model.User;
import com.kirill.meetyou.repository.Repository;
import com.kirill.meetyou.service.Service;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(path = "api/users")
@RequiredArgsConstructor
public class Controller {
    private final Service service;
    private final Repository repository;

    @GetMapping
    public ResponseEntity<List<User>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Юзер с id: " + id + " не найден")));
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<User> create(@RequestBody User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(user));
    }

    @DeleteMapping(path = "{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(path = "{id}")
    public ResponseEntity<Void> update(
            @PathVariable Long id,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String name
    ) {
        service.update(id, email, name);
        return ResponseEntity.ok().build();
    }

    // Обновленные методы для работы с интересами (теперь через String)
    @GetMapping("/by-interest")
    public ResponseEntity<List<User>> getUsersByInterest(
            @RequestParam String interestType) {
        return ResponseEntity.ok(repository.findUsersByInterestType(interestType));
    }

    @GetMapping("/by-all-interests")
    public ResponseEntity<List<User>> getUsersByAllInterests(
            @RequestParam Set<String> interestTypes) {
        if (interestTypes == null || interestTypes.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Не указаны интересы для поиска");
        }
        return ResponseEntity.ok(repository.findUsersByAllInterestTypes(interestTypes,
                interestTypes.size()));
    }

    @GetMapping("/by-any-interest")
    public ResponseEntity<List<User>> getUsersByAnyInterest(
            @RequestParam Set<String> interestTypes) {
        if (interestTypes == null || interestTypes.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Не указаны интересы для поиска");
        }
        return ResponseEntity.ok(repository.findUsersByAnyInterestTypes(interestTypes));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}