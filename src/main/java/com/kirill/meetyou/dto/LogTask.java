package com.kirill.meetyou.dto;

import com.kirill.meetyou.enums.LogTaskStatus;
import lombok.Data;

@Data
public class LogTask {
    private String id;
    private LogTaskStatus status;
    private String filePath;
    private String error;
}