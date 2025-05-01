package com.kirill.meetyou.repository;

import com.kirill.meetyou.model.Interest;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterestRepository extends JpaRepository<Interest, Long> {
    Optional<Interest> findByInterestType(String interestType);

    boolean existsByInterestType(String interestType);
}