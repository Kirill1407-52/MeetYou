package com.kirill.meetyou.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BioDto {
    // Приватный конструктор, чтобы нельзя было создать экземпляр этого класса
    private BioDto() {
        throw new UnsupportedOperationException("Это utility-класс, не предназначенный"
                + " для создания экземпляров");
    }

    @Data
    @Builder
    public static class Response {
        private String bio;
        private String interestFact;
    }

    @Data
    @Builder
    public static class CreateRequest {
        @NotBlank
        @Size(max = 300)
        public String bio;

        @Size(max = 150)
        public String interestFact;
    }

    @Data
    @Builder
    public static class UpdateBioRequest {
        @NotBlank
        @Size(max = 300)
        public String bio;
    }

    @Data
    @Builder
    public static class UpdateInterestFactRequest {
        @Size(max = 150)
        public String interestFact;
    }
}