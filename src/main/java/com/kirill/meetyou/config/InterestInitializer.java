package com.kirill.meetyou.config;

import com.kirill.meetyou.model.Interest;
import com.kirill.meetyou.repository.InterestRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InterestInitializer {
    private final InterestRepository interestsRepository;

    @PostConstruct
    public void init() {
        for (Interest.InterestType type : Interest.InterestType.values()) {
            if (!interestsRepository.existsByInterestType(type)) {
                Interest interest = new Interest();
                interest.setInterestType(type);
                interestsRepository.save(interest);
            }
        }
    }
}