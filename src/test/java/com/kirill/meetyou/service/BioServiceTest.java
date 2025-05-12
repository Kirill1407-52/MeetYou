package com.kirill.meetyou.service;

import com.kirill.meetyou.dto.BioDto.CreateRequest;
import com.kirill.meetyou.dto.BioDto.Response;
import com.kirill.meetyou.dto.BioDto.UpdateBioRequest;
import com.kirill.meetyou.model.Bio;
import com.kirill.meetyou.model.User;
import com.kirill.meetyou.repository.BioRepository;
import com.kirill.meetyou.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BioServiceTest {

    @Mock
    private BioRepository bioRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BioService bioService;

    private User testUser;
    private Bio testBio;
    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(userId);
        testUser.setName("Test User");

        testBio = new Bio();
        testBio.setId(1L);
        testBio.setUserBio("Test bio text");
        testBio.setInterestFact("Interesting fact");
        testBio.setUser(testUser);
    }

    private CreateRequest createRequest(String bio, String interestFact) {
        return CreateRequest.builder()
                .bio(bio)
                .interestFact(interestFact)
                .build();
    }

    private UpdateBioRequest updateBioRequest(String bio) {
        return UpdateBioRequest.builder()
                .bio(bio)
                .build();
    }



    @Test
    void createUserBio_ShouldCreateNewBio() {
        CreateRequest request = createRequest("Valid bio", "Interesting fact");
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(bioRepository.existsByUserId(userId)).thenReturn(false);
        when(bioRepository.save(any(Bio.class))).thenReturn(testBio);

        Response result = bioService.createUserBio(userId, request);

        assertNotNull(result);
        assertEquals("Test bio text", result.getBio());
        verify(bioRepository).save(any(Bio.class));
    }

    @Test
    void createUserBio_WithEmptyBio_ShouldThrowException() {
        CreateRequest request = createRequest("", "fact");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> bioService.createUserBio(userId, request));

        assertEquals("Bio text cannot be empty", ex.getMessage());
    }

    @Test
    void updateBio_ShouldUpdateBioText() {
        UpdateBioRequest request = updateBioRequest("Updated valid bio");
        when(bioRepository.findByUserId(userId)).thenReturn(Optional.of(testBio));
        when(bioRepository.save(any(Bio.class))).thenReturn(testBio);

        Response result = bioService.updateBio(userId, request);

        assertNotNull(result);
        verify(bioRepository).save(testBio);
    }

    @Test
    void updateBio_WithEmptyText_ShouldThrowException() {
        UpdateBioRequest request = updateBioRequest("");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> bioService.updateBio(userId, request));

        assertEquals("Bio text cannot be empty", ex.getMessage());
        verify(bioRepository, never()).findByUserId(any());
        verify(bioRepository, never()).save(any());
    }
}