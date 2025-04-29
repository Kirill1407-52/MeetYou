package com.kirill.meetyou.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "photos")
@Getter
@Setter
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "is_main", nullable = false)
    private String isMain;  // Хранит "true" или "false" как String

    @Column(nullable = false)
    private LocalDate uploadDate;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    //     Дополнительные методы для прямого доступа к строковому значению
    @JsonIgnore
    public String getIsMainString() {
        return this.isMain;
    }

    public void setIsMainString(String isMain) {
        if (!"true".equals(isMain) && !"false".equals(isMain)) {
            throw new IllegalArgumentException("isMain must be either 'true' or 'false'");
        }
        this.isMain = isMain;
    }
}