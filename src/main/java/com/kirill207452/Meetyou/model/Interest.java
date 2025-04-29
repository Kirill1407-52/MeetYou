package com.kirill207452.Meetyou.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "interests")
@Getter
@Setter
public class Interest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private InterestType interestType;

    @ManyToMany(mappedBy = "interests")
    @JsonBackReference
    private Set<User> users = new HashSet<>();

    public enum InterestType {
        NATURE,
        HOOKAH,
        BAR,
        CLUB,
        MUSEUM,
        EXCURSIONS,
        BOOKS,
        SPORTS,
        CHESS,
        TRAVEL
    }

    // Конструкторы, геттеры и сеттеры генерируются через Lombok
}