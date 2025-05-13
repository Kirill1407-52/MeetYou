package com.kirill.meetyou.service;

import com.kirill.meetyou.exception.ResourceNotFoundException;
import com.kirill.meetyou.model.Photo;
import com.kirill.meetyou.model.User;
import com.kirill.meetyou.repository.PhotoRepository;
import com.kirill.meetyou.repository.UserRepository;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoService {
    private static final String CLEAR_MAIN_PHOTOS_LOG = "Очистка текущих главных фотографий для пользователя {}";
    private final PhotoRepository photoRepository;
    private final UserRepository userRepository;

    @Transactional
    public Photo addPhoto(Long userId, Photo photo) {
        try {
            validateUserId(userId);
            validatePhoto(photo);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Пользователь"
                            + " с id: " + userId + " не найден"));

            if (isMainPhoto(photo)) {
                log.debug(CLEAR_MAIN_PHOTOS_LOG, userId);
                photoRepository.clearMainPhotos(userId);
            }

            photo.setUser(user);
            photo.setUploadDate(LocalDate.now());
            Photo savedPhoto = photoRepository.save(photo);
            log.info("Фотография успешно добавлена для пользователя {}", userId);
            return savedPhoto;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка при добавлении "
                    + "фотографии для пользователя {}: {}", userId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Не удалось добавить фотографию из-за некорректных данных или ошибки сервера");
        }
    }

    public List<Photo> getAllUserPhotos(Long userId) {
        try {
            validateUserId(userId);
            log.debug("Получение всех фотографий для пользователя {}", userId);
            return photoRepository.findByUserId(userId);
        } catch (Exception e) {
            log.error("Ошибка при получении "
                    + "фотографий для пользователя {}: {}", userId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не"
                    + " удалось получить фотографии");
        }
    }

    public Photo getPhotoById(Long userId, Long photoId) {
        try {
            validateUserId(userId);
            validatePhotoId(photoId);
            log.debug("Получение фотографии {} для пользователя {}", photoId, userId);
            return photoRepository.findByIdAndUserId(photoId, userId)
                    .orElseThrow(() -> new
                            ResourceNotFoundException(
                            "Фотография с id: " + photoId
                                    + " для пользователя с id: " + userId + " не найдена"));
        } catch (Exception e) {
            log.error("Ошибка при получении "
                    + "фотографии {} для пользователя {}:"
                    + " {}", photoId, userId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось"
                    + " получить фотографию");
        }
    }

    @Transactional
    public Photo updatePhoto(Long userId, Long photoId, Photo photoDetails) {
        try {
            validateUserId(userId);
            validatePhotoId(photoId);
            validatePhoto(photoDetails);

            Photo photo = getPhotoById(userId, photoId);

            if (isMainPhoto(photoDetails) && !isMainPhoto(photo)) {
                log.debug(CLEAR_MAIN_PHOTOS_LOG, userId);
                photoRepository.clearMainPhotos(userId);
            }

            if (photoDetails.getPhotoUrl() != null
                    && !photoDetails.getPhotoUrl().trim().isEmpty()) {
                photo.setPhotoUrl(photoDetails.getPhotoUrl());
            }

            if (photoDetails.getIsMainString() != null) {
                photo.setIsMainString(photoDetails.getIsMainString());
            }

            if (photoDetails.getUploadDate() != null) {
                photo.setUploadDate(photoDetails.getUploadDate());
            }

            Photo updatedPhoto = photoRepository.save(photo);
            log.info("Фотография {} успешно обновлена для пользователя {}", photoId, userId);
            return updatedPhoto;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка при обновлении фотографии"
                    + " {} для пользователя {}: {}", photoId, userId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Не удалось"
                    + " обновить фотографию из-за некорректных данных или ошибки сервера");
        }
    }

    @Transactional
    public void deletePhoto(Long userId, Long photoId) {
        try {
            validateUserId(userId);
            validatePhotoId(photoId);

            Photo photo = getPhotoById(userId, photoId);
            photoRepository.delete(photo);

            if (isMainPhoto(photo)) {
                log.debug("Установка новой главной фотографии для пользователя {}", userId);
                photoRepository.setNewestPhotoAsMain(userId);
            }
            log.info("Фотография {} успешно удалена для пользователя {}", photoId, userId);
        } catch (Exception e) {
            log.error("Ошибка при удалении фотографии {} для"
                    + " пользователя"
                    + " {}: {}", photoId, userId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось"
                    + " удалить фотографию");
        }
    }

    @Transactional
    public List<Photo> addMultiplePhotos(Long userId, List<Photo> photos) {
        try {
            validateUserId(userId);
            if (photos == null) {
                log.warn("Передан null список фотографий для пользователя {}", userId);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Список"
                        + " фотографий не может быть null");
            }
            if (photos.isEmpty()) {
                log.debug("Передан пустой список фотографий для пользователя {}", userId);
                return Collections.emptyList();
            }

            photos.forEach(this::validatePhoto);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Пользователь"
                            + " с id: " + userId + " не найден"));

            boolean hasMainPhoto = photos.stream().anyMatch(this::isMainPhoto);

            if (hasMainPhoto) {
                log.debug(CLEAR_MAIN_PHOTOS_LOG, userId);
                photoRepository.clearMainPhotos(userId);
            }

            photos.forEach(photo -> {
                photo.setUser(user);
                photo.setUploadDate(LocalDate.now());
            });

            List<Photo> savedPhotos = photoRepository.saveAll(photos);
            log.info("Успешно добавлено {} фотографий"
                    + " для пользователя {}", savedPhotos.size(), userId);
            return savedPhotos;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка при добавлении нескольких фотографий"
                    + " для пользователя {}: {}", userId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Не удалось"
                    + " добавить фотографии из-за некорректных данных или ошибки сервера");
        }
    }

    @Transactional
    public Photo setPhotoAsMain(Long userId, Long photoId) {
        try {
            validateUserId(userId);
            validatePhotoId(photoId);

            photoRepository.clearMainPhotos(userId);
            Photo photo = getPhotoById(userId, photoId);
            photo.setIsMainString("true");

            Photo savedPhoto = photoRepository.save(photo);
            log.info("Фотография {} установлена как главная для пользователя {}", photoId, userId);
            return savedPhoto;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка при установке фотографии {} как"
                    + " главной для пользователя {}: {}", photoId, userId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Не удалось "
                    + "установить фотографию как главную");
        }
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            log.warn("Некорректный ID пользователя: {}", userId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректный"
                    + " ID пользователя");
        }
    }

    private void validatePhotoId(Long photoId) {
        if (photoId == null || photoId <= 0) {
            log.warn("Некорректный ID фотографии: {}", photoId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректный ID фотографии");
        }
    }

    private void validatePhoto(Photo photo) {
        if (photo == null) {
            log.warn("Передана null фотография");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Объект"
                    + " фотографии не может быть null");
        }
        if (photo.getPhotoUrl() == null || photo.getPhotoUrl().trim().isEmpty()) {
            log.warn("Пустой или null URL фотографии");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "URL фотографии "
                    + "не может быть пустым или null");
        }
    }

    private boolean isMainPhoto(Photo photo) {
        return "true".equals(photo.getIsMainString());
    }
}