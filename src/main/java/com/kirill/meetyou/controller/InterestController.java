package com.kirill.meetyou.controller;

import com.kirill.meetyou.model.Interest;
import com.kirill.meetyou.service.InterestService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/interests")
@RequiredArgsConstructor
public class InterestController {
    private final InterestService interestService;

    @PostMapping
    public void addInterest(
            @PathVariable Long userId,
            @RequestParam Interest.InterestType interestType) {
        interestService.addInterestToUser(userId, interestType);
    }

    @GetMapping
    public Set<Interest> getUserInterests(@PathVariable Long userId) {
        return interestService.getUserInterests(userId);
    }

    @DeleteMapping
    public void removeInterest(
            @PathVariable Long userId,
            @RequestParam Interest.InterestType interestType) {
        interestService.removeInterestFromUser(userId, interestType);
    }
}