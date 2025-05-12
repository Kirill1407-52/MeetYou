package com.kirill.meetyou.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class UserCreateDto {
    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    @NotNull
    private LocalDate birth;
}