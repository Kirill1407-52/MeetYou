package com.kirill.meetyou.controller;

import com.kirill.meetyou.model.Interest;
import com.kirill.meetyou.service.InterestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/{userId}/interests")
@RequiredArgsConstructor
@Tag(name = "Interest Management", description = "APIs for managing user interests")
public class InterestController {
    private final InterestService interestService;

    @PostMapping
    @Operation(summary = "Add interest", description = "Adds a new interest to the specified user")
    @ApiResponse(responseCode = "200", description = "Interest added successfully")
    public void addInterest(
            @PathVariable Long userId,
            @RequestParam String interestName) {
        interestService.addInterestToUser(userId, interestName);
    }

    @DeleteMapping
    @Operation(summary = "Remove interest", description = "Removes an interest from the specified user")
    @ApiResponse(responseCode = "200", description = "Interest removed successfully")
    public void removeInterest(
            @PathVariable Long userId,
            @RequestParam String interestName) {
        interestService.removeInterestFromUser(userId, interestName);
    }

    @GetMapping
    @Operation(summary = "Get user interests", description = "Retrieves all interests of the specified user")
    @ApiResponse(responseCode = "200", description = "Interests retrieved successfully")
    public Set<Interest> getUserInterests(@PathVariable Long userId) {
        return interestService.getUserInterests(userId);
    }
}