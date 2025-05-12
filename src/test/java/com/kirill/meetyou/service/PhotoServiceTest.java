package com.kirill.meetyou.service;

import com.kirill.meetyou.exception.ResourceNotFoundException;
import com.kirill.meetyou.model.Photo;
import com.kirill.meetyou.model.User;
import com.kirill.meetyou.repository.PhotoRepository;
import com.kirill.meetyou.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedConstruction;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhotoServiceTest {

    @Mock
    private PhotoRepository photoRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PhotoService photoService;

    private User testUser;
    private Photo testPhoto;
    private Photo testMainPhoto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");

        testPhoto = new Photo();
        testPhoto.setId(1L);
        testPhoto.setPhotoUrl("photo1.jpg");
        testPhoto.setIsMainString("false");
        testPhoto.setUploadDate(LocalDate.now());
        testPhoto.setUser(testUser);

        testMainPhoto = new Photo();
        testMainPhoto.setId(2L);
        testMainPhoto.setPhotoUrl("main.jpg");
        testMainPhoto.setIsMainString("true");
        testMainPhoto.setUploadDate(LocalDate.now());
        testMainPhoto.setUser(testUser);
    }

    @Test
    void addPhoto_ShouldSavePhoto() {
        try (MockedConstruction<Photo> ignored = mockConstruction(Photo.class)) {
            when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
            when(photoRepository.save(any(Photo.class))).thenReturn(testPhoto);

            Photo result = photoService.addPhoto(1L, testPhoto);

            assertNotNull(result);
            assertEquals(testPhoto.getId(), result.getId());
            verify(photoRepository).save(testPhoto);
        }
    }

    @Test
    void addPhoto_WithMainPhoto_ShouldClearPreviousMainPhotos() {
        testPhoto.setIsMainString("true");
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(photoRepository.save(any(Photo.class))).thenReturn(testPhoto);

        Photo result = photoService.addPhoto(1L, testPhoto);

        verify(photoRepository).clearMainPhotos(1L);
        assertEquals("true", result.getIsMainString());
    }

    @Test
    void addPhoto_UserNotFound_ShouldThrowException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                photoService.addPhoto(1L, testPhoto));
    }

    @Test
    void getAllUserPhotos_ShouldReturnPhotos() {
        List<Photo> photos = Arrays.asList(testPhoto, testMainPhoto);
        when(photoRepository.findByUserId(anyLong())).thenReturn(photos);

        List<Photo> result = photoService.getAllUserPhotos(1L);

        assertEquals(2, result.size());
        verify(photoRepository).findByUserId(1L);
    }

    @Test
    void getAllUserPhotos_NoPhotos_ShouldReturnEmptyList() {
        when(photoRepository.findByUserId(anyLong())).thenReturn(Collections.emptyList());

        List<Photo> result = photoService.getAllUserPhotos(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getPhotoById_ShouldReturnPhoto() {
        when(photoRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(testPhoto));

        Photo result = photoService.getPhotoById(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getPhotoById_NotFound_ShouldThrowException() {
        when(photoRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                photoService.getPhotoById(1L, 1L));
    }

    @Test
    void updatePhoto_ShouldUpdateFields() {
        Photo updatedPhoto = new Photo();
        updatedPhoto.setPhotoUrl("new.jpg");
        updatedPhoto.setIsMainString("true");

        when(photoRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(testPhoto));
        when(photoRepository.save(any(Photo.class))).thenReturn(testPhoto);

        Photo result = photoService.updatePhoto(1L, 1L, updatedPhoto);

        verify(photoRepository).clearMainPhotos(1L);
        assertEquals("new.jpg", result.getPhotoUrl());
        assertEquals("true", result.getIsMainString());
    }

    @Test
    void updatePhoto_WithNullFields_ShouldNotUpdate() {
        Photo updatedPhoto = new Photo();

        when(photoRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(testPhoto));
        when(photoRepository.save(any(Photo.class))).thenReturn(testPhoto);

        Photo result = photoService.updatePhoto(1L, 1L, updatedPhoto);

        assertEquals("photo1.jpg", result.getPhotoUrl());
        assertEquals("false", result.getIsMainString());
    }

    @Test
    void updatePhoto_NotFound_ShouldThrowException() {
        when(photoRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                photoService.updatePhoto(1L, 1L, new Photo()));
    }

    @Test
    void deletePhoto_ShouldDeleteAndSetNewMainIfNeeded() {
        when(photoRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(testMainPhoto));

        photoService.deletePhoto(1L, 1L);

        verify(photoRepository).delete(testMainPhoto);
        verify(photoRepository).setNewestPhotoAsMain(1L);
    }

    @Test
    void deletePhoto_NotMainPhoto_ShouldNotSetNewMain() {
        when(photoRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(testPhoto));

        photoService.deletePhoto(1L, 1L);

        verify(photoRepository).delete(testPhoto);
        verify(photoRepository, never()).setNewestPhotoAsMain(1L);
    }

    @Test
    void deletePhoto_NotFound_ShouldThrowException() {
        when(photoRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                photoService.deletePhoto(1L, 1L));
    }

    @Test
    void addMultiplePhotos_ShouldSaveAllPhotos() {
        List<Photo> photos = Arrays.asList(testPhoto, testMainPhoto);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(photoRepository.saveAll(anyList())).thenReturn(photos);

        List<Photo> result = photoService.addMultiplePhotos(1L, photos);

        assertEquals(2, result.size());
        verify(photoRepository).saveAll(photos);
    }

    @Test
    void addMultiplePhotos_WithMainPhoto_ShouldClearPreviousMain() {
        testPhoto.setIsMainString("true");
        List<Photo> photos = List.of(testPhoto);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(photoRepository.saveAll(anyList())).thenReturn(photos);

        photoService.addMultiplePhotos(1L, photos);

        verify(photoRepository).clearMainPhotos(1L);
    }

    @Test
    void addMultiplePhotos_EmptyList_ShouldReturnEmptyList() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));

        List<Photo> result = photoService.addMultiplePhotos(1L, Collections.emptyList());

        assertTrue(result.isEmpty());
        verify(photoRepository, never()).saveAll(anyList());
    }

    @Test
    void setPhotoAsMain_ShouldUpdatePhoto() {
        when(photoRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(testPhoto));
        when(photoRepository.save(any(Photo.class))).thenReturn(testPhoto);

        Photo result = photoService.setPhotoAsMain(1L, 1L);

        verify(photoRepository).clearMainPhotos(1L);
        assertEquals("true", result.getIsMainString());
    }

    @Test
    void setPhotoAsMain_NotFound_ShouldThrowException() {
        when(photoRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                photoService.setPhotoAsMain(1L, 1L));
    }
}