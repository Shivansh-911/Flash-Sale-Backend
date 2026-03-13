package com.shivansh.InventoryEngine.service.impl;

import java.time.Duration;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.shivansh.InventoryEngine.service.IdempotencyService;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class IdempotencyServiceImpl implements IdempotencyService {

    private final StringRedisTemplate redis;

    private static final Duration TTL = Duration.ofMinutes(10); // 1 hour in seconds



    // Key not exist -> set key with value "processing" and return true -> caller can proceed with processing the request
    // Key exist with value "processing" -> return false -> caller should not process the request
    @Override
    public boolean tryStart(String key) {
        
        Boolean success = redis.opsForValue().setIfAbsent(key, "processing", TTL);
        return Boolean.TRUE.equals(success);
        
    }


    // After processing the request, set the key with the orderId as value and TTL to prevent memory leak in Redis
    @Override
    public void complete(String key,UUID orderId) {
        
        redis.opsForValue().set(key, orderId.toString(), TTL);
    }


    @Override
    public String get(String key) {
        
        return redis.opsForValue().get(key);
    }

    @Override
    public void delete(String key) {
        redis.delete(key);
    }
    
}
