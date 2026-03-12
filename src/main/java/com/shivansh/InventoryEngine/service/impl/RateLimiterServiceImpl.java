package com.shivansh.InventoryEngine.service.impl;

import java.time.Duration;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.shivansh.InventoryEngine.service.RateLimiterService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RateLimiterServiceImpl implements RateLimiterService {

    private final StringRedisTemplate redis;

    private static final int LIMIT = 5;
    private static final Duration WINDOW = Duration.ofSeconds(10);

    @Override
    public void checkRateLimit(UUID userId) {
        
        String redisKey = "rate_limit:" + userId.toString();

        Long currentCount = redis.opsForValue().increment(redisKey);

        if(currentCount == 1) {
            // Set TTL for the key when it's created
            redis.expire(redisKey, WINDOW);
        }

        if(currentCount > LIMIT) {
            throw new RuntimeException("Rate limit exceeded");
        }

    }
    
}
