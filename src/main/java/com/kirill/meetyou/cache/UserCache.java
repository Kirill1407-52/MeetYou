package com.kirill.meetyou.cache;

import com.kirill.meetyou.model.User;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

@Component
public class UserCache {
    private static final int MAX_SIZE = 1000;
    private static final long TTL = 30 * 60 * 1000; // 30 minutes
    private static final long USER_SIZE_ESTIMATE = 200L; // ~200 bytes per user
    private static final long MAX_CACHE_SIZE_BYTES = 500_000_000; // 500MB

    private final Map<Long, CacheEntry> cache;
    private long currentCacheSize = 0;
    private final ScheduledExecutorService scheduler;

    public UserCache() {
        this.cache = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, CacheEntry> eldest) {
                if (size() > MAX_SIZE || currentCacheSize > MAX_CACHE_SIZE_BYTES) {
                    currentCacheSize -= USER_SIZE_ESTIMATE;
                    return true;
                }
                return false;
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
                    currentCacheSize -= USER_SIZE_ESTIMATE;
                }
                return null;
            }
            return entry.user;
        }
    }

    public void put(Long id, User user) {
        synchronized (cache) {
            if (cache.containsKey(id)) {
                currentCacheSize -= USER_SIZE_ESTIMATE;
            }
            cache.put(id, new CacheEntry(user));
            currentCacheSize += USER_SIZE_ESTIMATE;
        }
    }

    public void remove(Long id) {
        synchronized (cache) {
            CacheEntry removed = cache.remove(id);
            if (removed != null) {
                currentCacheSize -= USER_SIZE_ESTIMATE;
            }
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
            cache.entrySet().removeIf(entry -> {
                if (entry.getValue().isExpired()) {
                    currentCacheSize -= USER_SIZE_ESTIMATE;
                    return true;
                }
                return false;
            });
        }
    }

    private void clear() {
        synchronized (cache) {
            cache.clear();
            currentCacheSize = 0;
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