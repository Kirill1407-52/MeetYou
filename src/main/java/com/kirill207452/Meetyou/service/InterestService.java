package com.kirill207452.Meetyou.service;

import com.example.demo.model.Interest;
import com.example.demo.model.User;
import com.example.demo.repository.InterestRepository;
import com.example.demo.repository.Repository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InterestService {
    private final Repository repository;
    private final InterestRepository interestsRepository;

    @Transactional
    public void addInterestToUser(Long userId, Interest.InterestType interestType) {
        User user = repository.findById(userId).orElseThrow();
        Interest interest = interestsRepository.findByInterestType(interestType);
        user.getInterests().add(interest);
        repository.save(user);
    }

    @Transactional
    public void removeInterestFromUser(Long userId, Interest.InterestType interestType) {
        User user = repository.findById(userId).orElseThrow();
        Interest interest = interestsRepository.findByInterestType(interestType);
        user.getInterests().remove(interest);
        repository.save(user);
    }

    @Transactional(readOnly = true)
    public Set<Interest> getUserInterests(Long userId) {
        User user = repository.findById(userId).orElseThrow();
        return user.getInterests();
    }
}