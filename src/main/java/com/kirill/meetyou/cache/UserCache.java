package com.kirill.meetyou.cache;

import com.kirill.meetyou.model.User;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class UserCache {
    private final Map<Long, User> cache = new ConcurrentHashMap<>();

    public User get(Long id) {
        return cache.get(id);
    }

    public void put(Long id, User user) {
        cache.put(id, user);
    }

    public void remove(Long id) {
        cache.remove(id);
    }

    public boolean contains(Long id) {
        return cache.containsKey(id);
    }
}
