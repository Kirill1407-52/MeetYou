package com.kirill207452.Meetyou.service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Photo;
import com.example.demo.model.User;
import com.example.demo.repository.PhotoRepository;
import com.example.demo.repository.Repository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PhotoService {
    private final PhotoRepository photoRepository;
    private final Repository repository;

    @Transactional
    public Photo addPhoto(Long userId, Photo photo) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if ("true".equals(photo.getIsMainString())) {
            photoRepository.clearMainPhotos(userId);
        }

        photo.setUser(user);
        photo.setUploadDate(LocalDate.now());
        return photoRepository.save(photo);
    }

    public List<Photo> getAllUserPhotos(Long userId) {
        return photoRepository.findByUserId(userId);
    }

    public Photo getPhotoById(Long userId, Long photoId) {
        return photoRepository.findByIdAndUserId(photoId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Photo not found with id: " + photoId + " for user id: " + userId));
    }

    @Transactional
    public Photo updatePhoto(Long userId, Long photoId, Photo photoDetails) {
        Photo photo = getPhotoById(userId, photoId);

        if ("true".equals(photoDetails.getIsMainString())
                && !"true".equals(photo.getIsMainString())) {
            photoRepository.clearMainPhotos(userId);
        }

        if (photoDetails.getPhotoUrl() != null) {
            photo.setPhotoUrl(photoDetails.getPhotoUrl());
        }

        if (photoDetails.getIsMainString() != null) {
            photo.setIsMainString(photoDetails.getIsMainString());
        }

        if (photoDetails.getUploadDate() != null) {
            photo.setUploadDate(photoDetails.getUploadDate());
        }

        return photoRepository.save(photo);
    }

    @Transactional
    public void deletePhoto(Long userId, Long photoId) {
        Photo photo = getPhotoById(userId, photoId);
        photoRepository.delete(photo);

        if ("true".equals(photo.getIsMainString())) {
            photoRepository.setNewestPhotoAsMain(userId);
        }
    }

    @Transactional
    public List<Photo> addMultiplePhotos(Long userId, List<Photo> photos) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean hasMainPhoto = photos.stream()
                .anyMatch(p -> "true".equals(p.getIsMainString()));

        if (hasMainPhoto) {
            photoRepository.clearMainPhotos(userId);
        }

        photos.forEach(photo -> {
            photo.setUser(user);
            photo.setUploadDate(LocalDate.now());
        });

        return photoRepository.saveAll(photos);
    }

    @Transactional
    public Photo setPhotoAsMain(Long userId, Long photoId) {
        photoRepository.clearMainPhotos(userId);

        Photo photo = getPhotoById(userId, photoId);
        photo.setIsMainString("true");

        return photoRepository.save(photo);
    }
}