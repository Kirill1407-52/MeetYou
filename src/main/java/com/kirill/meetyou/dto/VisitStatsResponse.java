package com.kirill.meetyou.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class VisitStatsResponse {
    private long totalVisits;
    private LocalDateTime firstVisit;
    private LocalDateTime lastVisit;

    public VisitStatsResponse(long totalVisits,
                              LocalDateTime firstVisit,
                              LocalDateTime lastVisit) {
        this.totalVisits = totalVisits;
        this.firstVisit = firstVisit;
        this.lastVisit = lastVisit;
    }
}