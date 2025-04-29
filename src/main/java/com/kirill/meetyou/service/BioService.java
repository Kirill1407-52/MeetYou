package com.kirill.meetyou.service;

import com.kirill.meetyou.dto.BioDto.CreateRequest;
import com.kirill.meetyou.dto.BioDto.Response;
import com.kirill.meetyou.dto.BioDto.UpdateBioRequest;
import com.kirill.meetyou.dto.BioDto.UpdateInterestFactRequest;
import com.kirill.meetyou.exception.ResourceAlreadyExistsException;
import com.kirill.meetyou.exception.ResourceNotFoundException;
import com.kirill.meetyou.model.Bio;
import com.kirill.meetyou.model.User;
import com.kirill.meetyou.repository.BioRepository;
import com.kirill.meetyou.repository.Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BioService {
    private static final String FOR_USER_ID_TEXT = "for user id: ";
    private static final String BIO_NOT_FOUND_TEXT = "Bio not found ";

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
        userBio.setUserBio(request.getBio());
        userBio.setInterestFact(request.getInterestFact());
        userBio.setUser(user);

        Bio savedBio = bioRepository.save(userBio);
        return mapToResponse(savedBio);
    }

    @Transactional(readOnly = true)
    public String getBioByUserId(Long userId) {
        Bio bio = bioRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(BIO_NOT_FOUND_TEXT
                        + FOR_USER_ID_TEXT + userId));

        if (bio.getUserBio() == null || bio.getUserBio().trim().isEmpty()) {
            throw new ResourceNotFoundException("Bio text not found " + FOR_USER_ID_TEXT + userId);
        }
        return bio.getUserBio();
    }

    @Transactional(readOnly = true)
    public String getInterestFactByUserId(Long userId) {
        Bio bio = bioRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(BIO_NOT_FOUND_TEXT
                        + FOR_USER_ID_TEXT + userId));

        if (bio.getInterestFact() == null || bio.getInterestFact().trim().isEmpty()) {
            throw new ResourceNotFoundException("Interest fact not found "
                    + FOR_USER_ID_TEXT + userId);
        }
        return bio.getInterestFact();
    }

    @Transactional(readOnly = true)
    public Response getFullBioByUserId(Long userId) {
        Bio bio = bioRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(BIO_NOT_FOUND_TEXT
                        + FOR_USER_ID_TEXT + userId));
        return mapToResponse(bio);
    }

    @Transactional
    public Response updateBio(Long userId, UpdateBioRequest request) {
        if (request.getBio() == null || request.getBio().trim().isEmpty()) {
            throw new IllegalArgumentException("Bio text cannot be empty");
        }

        Bio userBio = bioRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(BIO_NOT_FOUND_TEXT
                        + FOR_USER_ID_TEXT + userId));

        userBio.setUserBio(request.getBio());
        Bio updatedBio = bioRepository.save(userBio);
        return mapToResponse(updatedBio);
    }

    @Transactional
    public Response updateInterestFact(Long userId, UpdateInterestFactRequest request) {
        Bio userBio = bioRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(BIO_NOT_FOUND_TEXT
                        + FOR_USER_ID_TEXT + userId));

        userBio.setInterestFact(request.getInterestFact());
        Bio updatedBio = bioRepository.save(userBio);
        return mapToResponse(updatedBio);
    }

    @Transactional
    public void deleteUserBio(Long userId) {
        Bio userBio = bioRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(BIO_NOT_FOUND_TEXT
                        + FOR_USER_ID_TEXT + userId));
        bioRepository.delete(userBio);
    }

    @Transactional
    public void deleteInterestFact(Long userId) {
        Bio userBio = bioRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(BIO_NOT_FOUND_TEXT
                        + FOR_USER_ID_TEXT + userId));

        userBio.setInterestFact(null);
        bioRepository.save(userBio);
    }

    private Response mapToResponse(Bio userBio) {
        return Response.builder()
                .bio(userBio.getUserBio())
                .interestFact(userBio.getInterestFact())
                .build();
    }
}