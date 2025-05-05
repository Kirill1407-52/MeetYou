package com.kirill.meetyou.cache;

import com.kirill.meetyou.model.User;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class UserCache {
    private static final int MAX_SIZE = 1000; // Максимальное количество пользователей в кэше
    private static final long TTL = 30 * 60 * 1000; // 30 минут в миллисекундах

    private final Map<Long, CacheEntry> cache;
    private long currentCacheSize = 0;
    private static final long MAX_CACHE_SIZE_BYTES = 500_000_000; // 500MB (примерная оценка)

    public UserCache() {
        this.cache = new LinkedHashMap<Long, CacheEntry>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, CacheEntry> eldest) {
                if (size() > MAX_SIZE || currentCacheSize > MAX_CACHE_SIZE_BYTES) {
                    currentCacheSize -= estimateSize(eldest.getValue().user);
                    return true;
                }
                return false;
            }
        };

        initCleanupTask();
    }

    public User get(Long id) {
        synchronized (cache) {
            CacheEntry entry = cache.get(id);
            if (entry == null) {
                return null;
            }

            if (entry.isExpired()) {
                cache.remove(id);
                currentCacheSize -= estimateSize(entry.user);
                return null;
            }

            return entry.user;
        }
    }

    public void put(Long id, User user) {
        synchronized (cache) {
            if (cache.containsKey(id)) {
                currentCacheSize -= estimateSize(cache.get(id).user);
            }

            cache.put(id, new CacheEntry(user));
            currentCacheSize += estimateSize(user);
        }
    }

    public void remove(Long id) {
        synchronized (cache) {
            CacheEntry removed = cache.remove(id);
            if (removed != null) {
                currentCacheSize -= estimateSize(removed.user);
            }
        }
    }

    public boolean contains(Long id) {
        synchronized (cache) {
            CacheEntry entry = cache.get(id);
            if (entry == null) {
                return false;
            }

            if (entry.isExpired()) {
                cache.remove(id);
                currentCacheSize -= estimateSize(entry.user);
                return false;
            }

            return true;
        }
    }

    public void clear() {
        synchronized (cache) {
            cache.clear();
            currentCacheSize = 0;
        }
    }

    private void initCleanupTask() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::clearExpired, TTL, TTL, TimeUnit.MILLISECONDS);
    }

    private void clearExpired() {
        synchronized (cache) {
            cache.entrySet().removeIf(entry -> {
                if (entry.getValue().isExpired()) {
                    currentCacheSize -= estimateSize(entry.getValue().user);
                    return true;
                }
                return false;
            });
        }
    }

    private long estimateSize(User user) {
        // Примерная оценка размера объекта User в байтах
        // Можно уточнить на основе реальных данных
        return 200L; // ~200 байт на пользователя
    }

    private class CacheEntry {
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