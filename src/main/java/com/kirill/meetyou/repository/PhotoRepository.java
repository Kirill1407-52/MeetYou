package com.kirill.meetyou.repository;

import com.kirill.meetyou.model.Photo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
    // Основные методы выборки
    List<Photo> findByUserId(Long userId);

    Optional<Photo> findByIdAndUserId(Long id, Long userId);

    // Методы сброса флагов
    @Modifying
    @Transactional
    @Query("UPDATE Photo p SET p.isMain = 'false' WHERE p.user.id = :userId")
    void clearMainPhotos(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query(value = """
        UPDATE user_photos 
        SET is_main = 'true' 
        WHERE user_id = :userId 
        AND id = (
            SELECT id FROM user_photos 
            WHERE user_id = :userId 
            ORDER BY upload_date DESC 
            LIMIT 1
           )""",    
            nativeQuery = true)
    void setNewestPhotoAsMain(@Param("userId") Long userId);
}