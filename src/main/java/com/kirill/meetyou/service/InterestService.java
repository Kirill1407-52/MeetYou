package com.kirill.meetyou.service;

import com.kirill.meetyou.model.Interest;
import com.kirill.meetyou.model.User;
import com.kirill.meetyou.repository.InterestRepository;
import com.kirill.meetyou.repository.Repository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InterestService {
    private static final String USER_NOT_FOUND = "User not found";
    private static final String INTEREST_ALREADY_EXISTS = "Interest already exists";
    private static final String INTEREST_NOT_FOUND = "Interest not found";

    private final Repository repository;
    private final InterestRepository interestRepository;
    @Lazy
    private final InterestService self; // Injected self-reference

    @Transactional
    public void addInterestToUser(Long userId, String interestType) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

        // Call via self proxy
        Interest interest = self.getOrCreateInterest(interestType);

        user.getInterests().add(interest);
        repository.save(user);
    }

    @Transactional
    public Interest createNewInterest(String interestType) {
        if (interestRepository.existsByInterestType(interestType)) {
            throw new IllegalArgumentException(INTEREST_ALREADY_EXISTS);
        }

        Interest newInterest = new Interest();
        newInterest.setInterestType(interestType.trim());
        return interestRepository.save(newInterest);
    }

    @Transactional
    protected Interest getOrCreateInterest(String interestType) {
        return interestRepository.findByInterestType(interestType)
                .orElseGet(() -> self.createNewInterest(interestType));
    }

    @Transactional
    public void removeInterestFromUser(Long userId, String interestName) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

        Interest interest = interestRepository.findByInterestType(interestName)
                .orElseThrow(() -> new IllegalArgumentException(INTEREST_NOT_FOUND));

        user.getInterests().remove(interest);
        repository.save(user);
    }

    @Transactional(readOnly = true)
    public Set<Interest> getUserInterests(Long userId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
        return user.getInterests();
    }
}