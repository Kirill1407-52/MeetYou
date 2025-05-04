package com.kirill.meetyou.dto;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {
    private int status;
    private String message;
    private Map<String, String> errors;
}