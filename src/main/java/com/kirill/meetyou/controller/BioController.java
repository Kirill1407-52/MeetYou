package com.kirill.meetyou.controller;

import com.kirill.meetyou.dto.BioDto.CreateRequest;
import com.kirill.meetyou.dto.BioDto.Response;
import com.kirill.meetyou.dto.BioDto.UpdateBioRequest;
import com.kirill.meetyou.dto.BioDto.UpdateInterestFactRequest;
import com.kirill.meetyou.service.BioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/{userId}")
@RequiredArgsConstructor
@Tag(name = "Bio Management", description = "APIs for managing user bios and interest facts")
public class BioController {
    private final BioService bioService;

    @PostMapping("/bio")
    @Operation(summary = "Create user bio", description = "Creates a new bio for the specified user")
    @ApiResponse(responseCode = "201", description = "Bio created successfully")
    public ResponseEntity<Response> createUserBio(
            @PathVariable Long userId,
            @Valid @RequestBody CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bioService.createUserBio(userId, request));
    }

    @GetMapping("/bio")
    @Operation(summary = "Get user bio", description = "Retrieves the bio text for the specified user")
    @ApiResponse(responseCode = "200", description = "Bio retrieved successfully")
    public ResponseEntity<String> getBio(@PathVariable Long userId) {
        return ResponseEntity.ok(bioService.getBioByUserId(userId));
    }

    @GetMapping("/interest_fact")
    @Operation(summary = "Get interest fact", description = "Retrieves the interest fact for the specified user")
    @ApiResponse(responseCode = "200", description = "Interest fact retrieved successfully")
    public ResponseEntity<String> getInterestFact(@PathVariable Long userId) {
        return ResponseEntity.ok(bioService.getInterestFactByUserId(userId));
    }

    @GetMapping("/bioall")
    @Operation(summary = "Get full bio", description = "Retrieves complete bio information including interest fact")
    @ApiResponse(responseCode = "200", description = "Full bio retrieved successfully")
    public ResponseEntity<Response> getFullBio(@PathVariable Long userId) {
        return ResponseEntity.ok(bioService.getFullBioByUserId(userId));
    }

    @PutMapping("/bio")
    @Operation(summary = "Update bio", description = "Updates the bio for the specified user")
    @ApiResponse(responseCode = "200", description = "Bio updated successfully")
    public ResponseEntity<Response> updateBio(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateBioRequest request) {
        return ResponseEntity.ok(bioService.updateBio(userId, request));
    }

    @PutMapping("/interest_fact")
    @Operation(summary = "Update interest fact", description = "Updates the interest fact for the specified user")
    @ApiResponse(responseCode = "200", description = "Interest fact updated successfully")
    public ResponseEntity<Response> updateInterestFact(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateInterestFactRequest request) {
        return ResponseEntity.ok(bioService.updateInterestFact(userId, request));
    }

    @DeleteMapping("/bio")
    @Operation(summary = "Delete bio", description = "Deletes the bio for the specified user")
    @ApiResponse(responseCode = "204", description = "Bio deleted successfully")
    public ResponseEntity<Void> deleteUserBio(@PathVariable Long userId) {
        bioService.deleteUserBio(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/interest_fact")
    @Operation(summary = "Delete interest fact", description = "Deletes the interest fact for the specified user")
    @ApiResponse(responseCode = "204", description = "Interest fact deleted successfully")
    public ResponseEntity<Void> deleteInterestFact(@PathVariable Long userId) {
        bioService.deleteInterestFact(userId);
        return ResponseEntity.noContent().build();
    }
}