package com.kirill.meetyou.service;

import com.kirill.meetyou.exception.ResourceNotFoundException;
import com.kirill.meetyou.model.Photo;
import com.kirill.meetyou.model.User;
import com.kirill.meetyou.repository.PhotoRepository;
import com.kirill.meetyou.repository.UserRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoService {
    private static final String UPLOAD_DIR = "/home/kirill/Изображения/";
    private static final String IS_MAIN_FALSE = "false";
    private static final String IS_MAIN_TRUE = "true";
    private static final String CLEAR_MAIN_PHOTOS_LOG = "Очистка текущих главных фотографий для пользователя {}";
    private final PhotoRepository photoRepository;
    private final UserRepository userRepository;

    @Transactional
    public Photo addPhoto(Long userId, MultipartFile file, String isMain) {
        try {
            return addPhotoInternal(userId, file, isMain);
        } catch (IOException e) {
            log.error("Ошибка при сохранении файла для пользователя {}: {}", userId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось сохранить файл: " + e.getMessage());
        } catch (ResourceNotFoundException | ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Неизвестная ошибка при добавлении фотографии для пользователя {}: {}", userId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось добавить фотографию");
        }
    }

    private Photo addPhotoInternal(Long userId, MultipartFile file, String isMain) throws IOException {
        validateUserId(userId);
        validateFile(file);
        validateIsMain(isMain);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с id: " + userId + " не найден"));

        String fileName = saveFile(file);
        String photoUrl = UPLOAD_DIR + fileName;

        Photo photo = new Photo();
        photo.setPhotoUrl(photoUrl);
        photo.setIsMainString(isMain != null && isMain.equals(IS_MAIN_TRUE) ? IS_MAIN_TRUE : IS_MAIN_FALSE);
        photo.setUploadDate(LocalDate.now());
        photo.setUser(user);

        if (isMainPhoto(photo)) {
            log.debug(CLEAR_MAIN_PHOTOS_LOG, userId);
            photoRepository.clearMainPhotos(userId);
        }

        Photo savedPhoto = photoRepository.save(photo);
        log.info("Фотография успешно добавлена для пользователя {}", userId);
        return savedPhoto;
    }

    public List<Photo> getAllUserPhotos(Long userId) {
        try {
            validateUserId(userId);
            log.debug("Получение всех фотографий для пользователя {}", userId);
            List<Photo> photos = photoRepository.findByUserId(userId);
            if (photos.isEmpty()) {
                log.info("Фотографии для пользователя {} не найдены", userId);
            }
            return photos;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка базы данных при получении фотографий для пользователя {}: {}", userId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось получить фотографии: " + e.getMessage());
        }
    }

    public Photo getPhotoById(Long userId, Long photoId) {
        try {
            validateUserId(userId);
            validatePhotoId(photoId);
            log.debug("Получение фотографии {} для пользователя {}", photoId, userId);
            return photoRepository.findByIdAndUserId(photoId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "ФотографияФотография с id: " + photoId + " для пользователя с id: " + userId + " не найдена"));
        } catch (Exception e) {
            log.error("Ошибка при получении фотографии {} для пользователя {}: {}", photoId, userId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось получить фотографию");
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

            if (photoDetails.getPhotoUrl() != null && !photoDetails.getPhotoUrl().trim().isEmpty()) {
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
            log.error("Ошибка при обновлении фотографии {} для пользователя {}: {}", photoId, userId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Не удалось обновить фотографию из-за некорректных данных или ошибки сервера");
        }
    }

    @Transactional
    public void deletePhoto(Long userId, Long photoId) {
        try {
            validateUserId(userId);
            validatePhotoId(photoId);

            Photo photo = getPhotoById(userId, photoId);

            String photoUrl = photo.getPhotoUrl();
            if (photoUrl != null && !photoUrl.isEmpty()) {
                Path filePath = Paths.get(UPLOAD_DIR, photoUrl);
                try {
                    if (Files.exists(filePath)) {
                        Files.delete(filePath);
                        log.info("Файл {} успешно удален из хранилища", photoUrl);
                    } else {
                        log.warn("Файл {} не найден в хранилище", photoUrl);
                    }
                } catch (IOException e) {
                    log.error("Ошибка при удалении файла {}: {}", photoUrl, e.getMessage(), e);
                }
            }

            photoRepository.delete(photo);

            if (isMainPhoto(photo)) {
                log.debug("Удалена основная фотография для пользователя {}", userId);
            }

            log.info("Фотография {} успешно удалена для пользователя {}", photoId, userId);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка при удалении фотографии {} для пользователя {}: {}", photoId, userId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось удалить фотографию: " + e.getMessage());
        }
    }

    @Transactional
    public List<Photo> addMultiplePhotos(Long userId, List<MultipartFile> files, String isMain) {
        try {
            validateUserId(userId);
            if (files == null || files.isEmpty()) {
                log.debug("Передан пустой список файлов для пользователя {}", userId);
                return Collections.emptyList();
            }
            validateIsMain(isMain);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Пользователь с id: " + userId + " не найден"));

            boolean hasMainPhoto = isMain != null && isMain.equals(IS_MAIN_TRUE);
            if (hasMainPhoto) {
                log.debug(CLEAR_MAIN_PHOTOS_LOG, userId);
                photoRepository.clearMainPhotos(userId);
            }

            return processMultiplePhotos(userId, files, user, hasMainPhoto);
        } catch (ResourceNotFoundException | ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Неизвестная ошибка при добавлении нескольких фотографий для пользователя {}: {}", userId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось добавить фотографии");
        }
    }

    private List<Photo> processMultiplePhotos(Long userId, List<MultipartFile> files, User user, boolean hasMainPhoto) {
        List<Photo> photos = new ArrayList<>();
        List<String> failedFiles = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            try {
                processSinglePhoto(files.get(i), user, hasMainPhoto, i, photos);
            } catch (ResponseStatusException | IOException e) {
                log.error("Ошибка при сохранении файла {} для пользователя {}: {}", files.get(i).getOriginalFilename(), userId, e.getMessage());
                failedFiles.add(files.get(i).getOriginalFilename());
            }
        }

        if (!photos.isEmpty()) {
            List<Photo> savedPhotos = photoRepository.saveAll(photos);
            log.info("Успешно добавлено {} фотографий для пользователя {}", savedPhotos.size(), userId);
            if (!failedFiles.isEmpty()) {
                log.warn("Не удалось сохранить файлы: {}", failedFiles);
                throw new ResponseStatusException(HttpStatus.PARTIAL_CONTENT, "Частично добавлены фотографии, не удалось сохранить: " + failedFiles);
            }
            return savedPhotos;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Не удалось сохранить ни один файл: " + failedFiles);
    }

    private void processSinglePhoto(MultipartFile file, User user, boolean hasMainPhoto, int index, List<Photo> photos) throws IOException {
        validateFile(file);
        String fileName = saveFile(file);
        String photoUrl = UPLOAD_DIR + fileName;

        Photo photo = new Photo();
        photo.setPhotoUrl(photoUrl);
        photo.setIsMainString(hasMainPhoto && index == 0 ? IS_MAIN_TRUE : IS_MAIN_FALSE);
        photo.setUploadDate(LocalDate.now());
        photo.setUser(user);
        photos.add(photo);
    }

    private String saveFile(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        Path filePath = uploadPath.resolve(fileName);
        Files.write(filePath, file.getBytes());
        return fileName;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("Передан пустой или null файл");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Файл не может быть пустым или null");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            log.warn("Недопустимый тип файла: {}", contentType);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Файл должен быть изображением");
        }
    }

    @Transactional
    public Photo setPhotoAsMain(Long userId, Long photoId) {
        try {
            validateUserId(userId);
            validatePhotoId(photoId);

            photoRepository.clearMainPhotos(userId);
            Photo photo = getPhotoById(userId, photoId);
            photo.setIsMainString(IS_MAIN_TRUE);

            Photo savedPhoto = photoRepository.save(photo);
            log.info("Фотография {} установлена как главная для пользователя {}", photoId, userId);
            return savedPhoto;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка при установке фотографии {} как главной для пользователя {}: {}", photoId, userId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Не удалось установить фотографию как главную");
        }
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            log.warn("Некорректный ID пользователя: {}", userId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректный ID пользователя");
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Объект фотографии не может быть null");
        }
        if (photo.getPhotoUrl() == null || photo.getPhotoUrl().trim().isEmpty()) {
            log.warn("Пустой или null URL фотографии");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "URL фотографии не может быть пустым или null");
        }
    }

    private void validateIsMain(String isMain) {
        if (isMain != null && !isMain.equals(IS_MAIN_TRUE) && !isMain.equals(IS_MAIN_FALSE)) {
            log.warn("Некорректное значение isMain: {}", isMain);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "isMain должен быть 'true' или 'false'");
        }
    }

    private boolean isMainPhoto(Photo photo) {
        return IS_MAIN_TRUE.equals(photo.getIsMainString());
    }
}