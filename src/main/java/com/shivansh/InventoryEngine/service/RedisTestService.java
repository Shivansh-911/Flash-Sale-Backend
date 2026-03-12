package com.shivansh.InventoryEngine.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;



@Service
@RequiredArgsConstructor
public class RedisTestService {

    private final StringRedisTemplate redis;

    public void testRedis() {

        redis.opsForValue().set("test-key", "hello");

        String value = redis.opsForValue().get("test-key");

        System.out.println("Redis value: " + value);
    }
}
