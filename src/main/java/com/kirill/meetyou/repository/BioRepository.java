package com.kirill.meetyou.repository;

import com.kirill.meetyou.model.Bio;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BioRepository extends JpaRepository<Bio, Long> {
    Optional<Bio> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}