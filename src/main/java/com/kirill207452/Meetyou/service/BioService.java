package com.kirill207452.Meetyou.service;

import com.example.demo.dto.BioDto.CreateRequest;
import com.example.demo.dto.BioDto.Response;
import com.example.demo.dto.BioDto.UpdateBioRequest;
import com.example.demo.dto.BioDto.UpdateInterestFactRequest;
import com.example.demo.exception.ResourceAlreadyExistsException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Bio;
import com.example.demo.model.User;
import com.example.demo.repository.BioRepository;
import com.example.demo.repository.Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BioService {
    private final BioRepository bioRepository;
    private final Repository repository;

    @Transactional
    public Response createUserBio(Long userId, CreateRequest request) {
        if (request.getBio() == null || request.getBio().trim().isEmpty()) {
            throw new IllegalArgumentException("Bio text cannot be empty");
        }

        User user;
        user = repository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not "
                        + "found with id: " + userId));

        if (bioRepository.existsByUserId(userId)) {
            throw new ResourceAlreadyExistsException("Bio already exists for user id: " + userId);
        }

        Bio userBio = new Bio();
        userBio.setBio(request.getBio());
        userBio.setInterestFact(request.getInterestFact());
        userBio.setUser(user);

        Bio savedBio = bioRepository.save(userBio);
        return mapToResponse(savedBio);
    }

    @Transactional(readOnly = true)
    public String getBioByUserId(Long userId) {
        Bio bio = bioRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Bio not found"
                        + " for user id: " + userId));

        if (bio.getBio() == null || bio.getBio().trim().isEmpty()) {
            throw new ResourceNotFoundException("Bio text not found for user id: " + userId);
        }
        return bio.getBio();
    }

    @Transactional(readOnly = true)
    public String getInterestFactByUserId(Long userId) {
        Bio bio = bioRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Bio not found "
                        + "for user id: " + userId));

        if (bio.getInterestFact() == null || bio.getInterestFact().trim().isEmpty()) {
            throw new ResourceNotFoundException("Interest fact not found for user id: " + userId);
        }
        return bio.getInterestFact();
    }

    @Transactional(readOnly = true)
    public Response getFullBioByUserId(Long userId) {
        Bio bio = bioRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Bio not "
                        + "found for user id: " + userId));
        return mapToResponse(bio);
    }

    @Transactional
    public Response updateBio(Long userId, UpdateBioRequest request) {
        if (request.getBio() == null || request.getBio().trim().isEmpty()) {
            throw new IllegalArgumentException("Bio text cannot be empty");
        }

        Bio userBio = bioRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Bio not"
                        + " found for user id: " + userId));

        userBio.setBio(request.getBio());
        Bio updatedBio = bioRepository.save(userBio);
        return mapToResponse(updatedBio);
    }

    @Transactional
    public Response updateInterestFact(Long userId, UpdateInterestFactRequest request) {
        Bio userBio = bioRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Bio not found "
                        + "for user id: " + userId));

        userBio.setInterestFact(request.getInterestFact());
        Bio updatedBio = bioRepository.save(userBio);
        return mapToResponse(updatedBio);
    }

    @Transactional
    public void deleteUserBio(Long userId) {
        Bio userBio = bioRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Bio not found"
                        + " for user id: " + userId));
        bioRepository.delete(userBio);
    }

    @Transactional
    public void deleteInterestFact(Long userId) {
        Bio userBio = bioRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Bio not found "
                        + "for user id: " + userId));

        userBio.setInterestFact(null);
        bioRepository.save(userBio);
    }

    private Response mapToResponse(Bio userBio) {
        return Response.builder()
                .bio(userBio.getBio())
                .interestFact(userBio.getInterestFact())
                .build();
    }
}