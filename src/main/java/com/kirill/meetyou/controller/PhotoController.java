package com.kirill.meetyou.controller;

import com.kirill.meetyou.model.Photo;
import com.kirill.meetyou.service.PhotoService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/photos")
@RequiredArgsConstructor
public class PhotoController {
    private static final String ERROR_KEY = "error";
    private final PhotoService photoService;

    @PostMapping
    public ResponseEntity<Object> addPhoto(
            @PathVariable Long userId,
            @RequestBody Photo photo) {
        try {
            Photo createdPhoto = photoService.addPhoto(userId, photo);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPhoto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of(ERROR_KEY, e.getMessage()));
        }
    }

    @PostMapping("/batch")
    public ResponseEntity<Object> addMultiplePhotos(
            @PathVariable Long userId,
            @RequestBody List<Photo> photos) {
        try {
            List<Photo> createdPhotos = photoService.addMultiplePhotos(userId, photos);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPhotos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of(ERROR_KEY, e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Photo>> getAllPhotos(
            @PathVariable Long userId) {
        List<Photo> photos = photoService.getAllUserPhotos(userId);
        return ResponseEntity.ok(photos);
    }

    @GetMapping("/{photoId}")
    public ResponseEntity<Object> getPhoto(
            @PathVariable Long userId,
            @PathVariable Long photoId) {
        try {
            Photo photo = photoService.getPhotoById(userId, photoId);
            return ResponseEntity.ok(photo);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(ERROR_KEY, e.getMessage()));
        }
    }

    @PutMapping("/{photoId}")
    public ResponseEntity<Object> updatePhoto(
            @PathVariable Long userId,
            @PathVariable Long photoId,
            @RequestBody Photo photoDetails) {
        try {
            Photo updatedPhoto = photoService.updatePhoto(userId, photoId, photoDetails);
            return ResponseEntity.ok(updatedPhoto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(ERROR_KEY, e.getMessage()));
        }
    }

    @PutMapping("/{photoId}/set-main")
    public ResponseEntity<Object> setAsMainPhoto(
            @PathVariable Long userId,
            @PathVariable Long photoId) {
        try {
            Photo updated = photoService.setPhotoAsMain(userId, photoId);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(ERROR_KEY, e.getMessage()));
        }
    }

    @DeleteMapping("/{photoId}")
    public ResponseEntity<Object> deletePhoto(
            @PathVariable Long userId,
            @PathVariable Long photoId) {
        try {
            photoService.deletePhoto(userId, photoId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(ERROR_KEY, e.getMessage()));
        }
    }
}