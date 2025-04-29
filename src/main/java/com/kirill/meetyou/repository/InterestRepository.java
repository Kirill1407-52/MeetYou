package com.kirill.meetyou.repository;

import com.kirill.meetyou.model.Interest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterestRepository extends JpaRepository<Interest, Long> {
    Interest findByInterestType(Interest.InterestType interestType);

    boolean existsByInterestType(Interest.InterestType interestType);
}