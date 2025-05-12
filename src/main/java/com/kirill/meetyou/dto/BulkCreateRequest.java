package com.kirill.meetyou.dto;

import jakarta.validation.Valid;
import java.util.List;
import lombok.Data;

@Data
public class BulkCreateRequest {
    @Valid  // Для каскадной валидации
    private List<UserCreateDto> users;
}