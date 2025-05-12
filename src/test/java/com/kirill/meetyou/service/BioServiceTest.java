package com.kirill.meetyou.service;

import com.kirill.meetyou.dto.BioDto;
import com.kirill.meetyou.exception.ResourceAlreadyExistsException;
import com.kirill.meetyou.exception.ResourceNotFoundException;
import com.kirill.meetyou.model.Bio;
import com.kirill.meetyou.model.User;
import com.kirill.meetyou.repository.BioRepository;
import com.kirill.meetyou.repository.UserRepository;
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

    private final Long userId = 1L;
    private final String bioText = "Test bio text";
    private final String interestFact = "Test interest fact";

    @Test
    void createUserBio_Success() {
        // Arrange
        BioDto.CreateRequest request = BioDto.CreateRequest.builder()
                .bio(bioText)
                .interestFact(interestFact)
                .build();

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bioRepository.existsByUserId(userId)).thenReturn(false);
        when(bioRepository.save(any(Bio.class))).thenAnswer(invocation -> {
            Bio bio = invocation.getArgument(0);
            bio.setId(1L);
            return bio;
        });

        // Act
        BioDto.Response response = bioService.createUserBio(userId, request);

        // Assert
        assertNotNull(response);
        assertEquals(bioText, response.getBio());
        assertEquals(interestFact, response.getInterestFact());

        verify(userRepository).findById(userId);
        verify(bioRepository).existsByUserId(userId);
        verify(bioRepository).save(any(Bio.class));
    }

    @Test
    void createUserBio_UserNotFound_ThrowsException() {
        // Arrange
        BioDto.CreateRequest request = BioDto.CreateRequest.builder()
                .bio(bioText)
                .interestFact(interestFact)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                bioService.createUserBio(userId, request));

        verify(userRepository).findById(userId);
        verifyNoInteractions(bioRepository);
    }

    @Test
    void createUserBio_BioAlreadyExists_ThrowsException() {
        // Arrange
        BioDto.CreateRequest request = BioDto.CreateRequest.builder()
                .bio(bioText)
                .interestFact(interestFact)
                .build();
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bioRepository.existsByUserId(userId)).thenReturn(true);

        // Act & Assert
        assertThrows(ResourceAlreadyExistsException.class, () ->
                bioService.createUserBio(userId, request));

        verify(userRepository).findById(userId);
        verify(bioRepository).existsByUserId(userId);
        verify(bioRepository, never()).save(any());
    }

    @Test
    void createUserBio_EmptyBioText_ThrowsException() {
        // Arrange
        BioDto.CreateRequest request = BioDto.CreateRequest.builder()
                .bio("")
                .interestFact(interestFact)
                .build();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                bioService.createUserBio(userId, request));

        verifyNoInteractions(userRepository);
        verifyNoInteractions(bioRepository);
    }

    @Test
    void getBioByUserId_Success() {
        // Arrange
        Bio bio = new Bio();
        bio.setUserBio(bioText);

        when(bioRepository.findByUserId(userId)).thenReturn(Optional.of(bio));

        // Act
        String result = bioService.getBioByUserId(userId);

        // Assert
        assertEquals(bioText, result);
        verify(bioRepository).findByUserId(userId);
    }

    @Test
    void getBioByUserId_BioNotFound_ThrowsException() {
        // Arrange
        when(bioRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                bioService.getBioByUserId(userId));

        verify(bioRepository).findByUserId(userId);
    }

    @Test
    void getBioByUserId_EmptyBioText_ThrowsException() {
        // Arrange
        Bio bio = new Bio();
        bio.setUserBio("");

        when(bioRepository.findByUserId(userId)).thenReturn(Optional.of(bio));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                bioService.getBioByUserId(userId));

        verify(bioRepository).findByUserId(userId);
    }

    @Test
    void getInterestFactByUserId_Success() {
        // Arrange
        Bio bio = new Bio();
        bio.setInterestFact(interestFact);

        when(bioRepository.findByUserId(userId)).thenReturn(Optional.of(bio));

        // Act
        String result = bioService.getInterestFactByUserId(userId);

        // Assert
        assertEquals(interestFact, result);
        verify(bioRepository).findByUserId(userId);
    }

    @Test
    void getInterestFactByUserId_BioNotFound_ThrowsException() {
        // Arrange
        when(bioRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                bioService.getInterestFactByUserId(userId));

        verify(bioRepository).findByUserId(userId);
    }

    @Test
    void getInterestFactByUserId_EmptyInterestFact_ThrowsException() {
        // Arrange
        Bio bio = new Bio();
        bio.setInterestFact("");

        when(bioRepository.findByUserId(userId)).thenReturn(Optional.of(bio));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                bioService.getInterestFactByUserId(userId));

        verify(bioRepository).findByUserId(userId);
    }

    @Test
    void getFullBioByUserId_Success() {
        // Arrange
        Bio bio = new Bio();
        bio.setUserBio(bioText);
        bio.setInterestFact(interestFact);

        when(bioRepository.findByUserId(userId)).thenReturn(Optional.of(bio));

        // Act
        BioDto.Response response = bioService.getFullBioByUserId(userId);

        // Assert
        assertNotNull(response);
        assertEquals(bioText, response.getBio());
        assertEquals(interestFact, response.getInterestFact());
        verify(bioRepository).findByUserId(userId);
    }

    @Test
    void getFullBioByUserId_BioNotFound_ThrowsException() {
        // Arrange
        when(bioRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                bioService.getFullBioByUserId(userId));

        verify(bioRepository).findByUserId(userId);
    }

    @Test
    void updateBio_Success() {
        // Arrange
        String updatedBioText = "Updated bio text";
        BioDto.UpdateBioRequest request = BioDto.UpdateBioRequest.builder()
                .bio(updatedBioText)
                .build();

        Bio existingBio = new Bio();
        existingBio.setUserBio(bioText);
        existingBio.setInterestFact(interestFact);

        when(bioRepository.findByUserId(userId)).thenReturn(Optional.of(existingBio));
        when(bioRepository.save(any(Bio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        BioDto.Response response = bioService.updateBio(userId, request);

        // Assert
        assertNotNull(response);
        assertEquals(updatedBioText, response.getBio());
        assertEquals(interestFact, response.getInterestFact());
        verify(bioRepository).findByUserId(userId);
        verify(bioRepository).save(existingBio);
    }

    @Test
    void updateBio_BioNotFound_ThrowsException() {
        // Arrange
        BioDto.UpdateBioRequest request = BioDto.UpdateBioRequest.builder()
                .bio("New bio")
                .build();
        when(bioRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                bioService.updateBio(userId, request));

        verify(bioRepository).findByUserId(userId);
        verify(bioRepository, never()).save(any());
    }

    @Test
    void updateBio_EmptyBioText_ThrowsException() {
        // Arrange
        BioDto.UpdateBioRequest request = BioDto.UpdateBioRequest.builder()
                .bio("")
                .build();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                bioService.updateBio(userId, request));

        verifyNoInteractions(bioRepository);
    }

    @Test
    void updateInterestFact_Success() {
        // Arrange
        String updatedInterestFact = "Updated interest fact";
        BioDto.UpdateInterestFactRequest request = BioDto.UpdateInterestFactRequest.builder()
                .interestFact(updatedInterestFact)
                .build();

        Bio existingBio = new Bio();
        existingBio.setUserBio(bioText);
        existingBio.setInterestFact(interestFact);

        when(bioRepository.findByUserId(userId)).thenReturn(Optional.of(existingBio));
        when(bioRepository.save(any(Bio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        BioDto.Response response = bioService.updateInterestFact(userId, request);

        // Assert
        assertNotNull(response);
        assertEquals(bioText, response.getBio());
        assertEquals(updatedInterestFact, response.getInterestFact());
        verify(bioRepository).findByUserId(userId);
        verify(bioRepository).save(existingBio);
    }

    @Test
    void updateInterestFact_BioNotFound_ThrowsException() {
        // Arrange
        BioDto.UpdateInterestFactRequest request = BioDto.UpdateInterestFactRequest.builder()
                .interestFact("New fact")
                .build();
        when(bioRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                bioService.updateInterestFact(userId, request));

        verify(bioRepository).findByUserId(userId);
        verify(bioRepository, never()).save(any());
    }

    @Test
    void deleteUserBio_Success() {
        // Arrange
        Bio existingBio = new Bio();
        when(bioRepository.findByUserId(userId)).thenReturn(Optional.of(existingBio));

        // Act
        bioService.deleteUserBio(userId);

        // Assert
        verify(bioRepository).findByUserId(userId);
        verify(bioRepository).delete(existingBio);
    }

    @Test
    void deleteUserBio_BioNotFound_ThrowsException() {
        // Arrange
        when(bioRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                bioService.deleteUserBio(userId));

        verify(bioRepository).findByUserId(userId);
        verify(bioRepository, never()).delete(any());
    }

    @Test
    void deleteInterestFact_Success() {
        // Arrange
        Bio existingBio = new Bio();
        existingBio.setInterestFact(interestFact);

        when(bioRepository.findByUserId(userId)).thenReturn(Optional.of(existingBio));
        when(bioRepository.save(any(Bio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        bioService.deleteInterestFact(userId);

        // Assert
        assertNull(existingBio.getInterestFact());
        verify(bioRepository).findByUserId(userId);
        verify(bioRepository).save(existingBio);
    }

    @Test
    void deleteInterestFact_BioNotFound_ThrowsException() {
        // Arrange
        when(bioRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                bioService.deleteInterestFact(userId));

        verify(bioRepository).findByUserId(userId);
        verify(bioRepository, never()).save(any());
    }
}