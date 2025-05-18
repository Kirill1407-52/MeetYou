package com.kirill.meetyou.service;

import com.kirill.meetyou.dto.VisitResponse;
import com.kirill.meetyou.dto.VisitStatsResponse;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

@Service
public class VisitService {
    private final AtomicInteger visitCounter = new AtomicInteger(0);
    private LocalDateTime firstVisitTime;
    private LocalDateTime lastVisitTime;

    public VisitResponse registerVisit() {
        LocalDateTime now = LocalDateTime.now();
        int count = visitCounter.incrementAndGet();

        synchronized (this) {
            if (count == 1) {
                firstVisitTime = now;
            }
            lastVisitTime = now;
        }

        return new VisitResponse(count, now);
    }

    public VisitStatsResponse getVisitStats() {
        int totalVisits = visitCounter.get();
        if (totalVisits == 0) {
            return new VisitStatsResponse(0, null, null);
        }
        return new VisitStatsResponse(
                totalVisits,
                firstVisitTime,
                lastVisitTime
        );
    }
}