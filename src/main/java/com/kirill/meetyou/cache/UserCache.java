package com.kirill.meetyou.cache;

import com.kirill.meetyou.model.User;
import jakarta.annotation.PreDestroy;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class UserCache {
    private static final int MAX_SIZE = 100;
    private static final long TTL = 1 * 10 * 1000;

    private final Map<Long, CacheEntry> cache;
    private final ScheduledExecutorService scheduler;

    public UserCache() {
        this.cache = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, CacheEntry> eldest) {
                return size() > MAX_SIZE;
            }
        };

        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        initCleanupTask();
    }

    public User get(Long id) {
        synchronized (cache) {
            CacheEntry entry = cache.get(id);
            if (entry == null || entry.isExpired()) {
                if (entry != null) {
                    cache.remove(id);
                }
                return null;
            }
            return entry.user;
        }
    }

    public void put(Long id, User user) {
        synchronized (cache) {
            cache.put(id, new CacheEntry(user));
        }
    }

    public void remove(Long id) {
        synchronized (cache) {
            cache.remove(id);
        }
    }

    @PreDestroy
    public void cleanup() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        clear();
    }

    private void initCleanupTask() {
        scheduler.scheduleAtFixedRate(this::clearExpired, TTL, TTL, TimeUnit.MILLISECONDS);
    }

    private void clearExpired() {
        synchronized (cache) {
            cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        }
    }

    private void clear() {
        synchronized (cache) {
            cache.clear();
        }
    }

    private static class CacheEntry {
        final User user;
        final long timestamp;

        CacheEntry(User user) {
            this.user = user;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > TTL;
        }
    }
}