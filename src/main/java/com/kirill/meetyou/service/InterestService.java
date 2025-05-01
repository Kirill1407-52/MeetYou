package com.kirill.meetyou.service;

import com.kirill.meetyou.model.Interest;
import com.kirill.meetyou.model.User;
import com.kirill.meetyou.repository.InterestRepository;
import com.kirill.meetyou.repository.Repository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InterestService {
    private final Repository repository;
    private final InterestRepository interestRepository;

    @Transactional
    public void addInterestToUser(Long userId, String interestType) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Interest interest = interestRepository.findByInterestType(interestType)
                .orElseGet(() -> createNewInterest(interestType));

        user.getInterests().add(interest);
        repository.save(user);
    }

    @Transactional
    public Interest createNewInterest(String interestType) {
        if (interestRepository.existsByInterestType(interestType)) {
            throw new IllegalArgumentException("Interest already exists");
        }

        Interest newInterest = new Interest();
        newInterest.setInterestType(interestType.trim());
        return interestRepository.save(newInterest);
    }

    @Transactional
    public void removeInterestFromUser(Long userId, String interestName) { // Изменили параметр
        User user = repository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Interest interest = interestRepository.findByInterestType(interestName)
                .orElseThrow(() -> new IllegalArgumentException("Interest not found"));

        user.getInterests().remove(interest);
        repository.save(user);
    }

    @Transactional(readOnly = true)
    public Set<Interest> getUserInterests(Long userId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return user.getInterests();
    }
}