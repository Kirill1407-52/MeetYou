package com.kirill.meetyou.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BulkResponse {
    private int successCount;
    private int failCount;
    private List<String> errors;  // Сообщения об ошибках
}