package com.kirill.meetyou.controller;

import com.kirill.meetyou.dto.BioDto.CreateRequest;
import com.kirill.meetyou.dto.BioDto.Response;
import com.kirill.meetyou.dto.BioDto.UpdateBioRequest;
import com.kirill.meetyou.dto.BioDto.UpdateInterestFactRequest;
import com.kirill.meetyou.exception.ResourceAlreadyExistsException;
import com.kirill.meetyou.exception.ResourceNotFoundException;
import com.kirill.meetyou.service.BioService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}")
@RequiredArgsConstructor
public class BioController {
    private final BioService bioService;

    @PostMapping("/bio")
    public ResponseEntity<Response> createUserBio(
            @PathVariable Long userId,
            @Valid @RequestBody CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bioService.createUserBio(userId, request));
    }

    @GetMapping("/bio")
    public ResponseEntity<String> getBio(@PathVariable Long userId) {
        return ResponseEntity.ok(bioService.getBioByUserId(userId));
    }

    @GetMapping("/interest_fact")
    public ResponseEntity<String> getInterestFact(@PathVariable Long userId) {
        return ResponseEntity.ok(bioService.getInterestFactByUserId(userId));
    }

    @GetMapping("/bioall")
    public ResponseEntity<Response> getFullBio(@PathVariable Long userId) {
        return ResponseEntity.ok(bioService.getFullBioByUserId(userId));
    }

    @PutMapping("/bio")
    public ResponseEntity<Response> updateBio(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateBioRequest request) {
        return ResponseEntity.ok(bioService.updateBio(userId, request));
    }

    @PutMapping("/interest_fact")
    public ResponseEntity<Response> updateInterestFact(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateInterestFactRequest request) {
        return ResponseEntity.ok(bioService.updateInterestFact(userId, request));
    }

    @DeleteMapping("/bio")
    public ResponseEntity<Void> deleteUserBio(@PathVariable Long userId) {
        bioService.deleteUserBio(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/interest_fact")
    public ResponseEntity<Void> deleteInterestFact(@PathVariable Long userId) {
        bioService.deleteInterestFact(userId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String>
        handleResourceAlreadyExistsException(ResourceAlreadyExistsException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}