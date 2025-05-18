package com.kirill.meetyou.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class VisitResponse {
    private long totalVisits;
    private LocalDateTime visitTime;

    public VisitResponse(long totalVisits, LocalDateTime visitTime) {
        this.totalVisits = totalVisits;
        this.visitTime = visitTime;
    }
}