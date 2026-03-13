package com.shivansh.InventoryEngine.service.impl;

import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.shivansh.InventoryEngine.service.InventoryCacheService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryCacheServiceImpl implements InventoryCacheService {

    private final StringRedisTemplate redis;

    //Initialze Stock in Redis
    @Override
    public void initializeStock(UUID productId, int stock) {
        
        String redisKey = "stock:"+ productId.toString();
        redis.opsForValue().set(redisKey, String.valueOf(stock));
    }

    @Override
    public boolean tryUpdateStock(UUID productId, int quantity) {
        
        String redisKey = "stock:"+ productId.toString();

        Long remainingStock = redis.opsForValue().decrement(redisKey, quantity);

        if(remainingStock == null) {
            throw new RuntimeException("Product not found in cache");
        }
        if(remainingStock < 0) {
            // Restore the stock if not enough available
            redis.opsForValue().increment(redisKey, quantity);
            return false;
        }

        return true;

    }

    @Override
    public void restoreStock(UUID productId, int quantity) {
        
        String redisKey = "stock:"+ productId.toString();
        redis.opsForValue().increment(redisKey, quantity);
    }
    
}
