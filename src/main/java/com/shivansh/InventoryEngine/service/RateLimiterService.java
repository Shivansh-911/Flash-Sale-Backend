package com.shivansh.InventoryEngine.service;

import java.util.UUID;

public interface RateLimiterService {

 
    void checkRateLimit(UUID userId);

}
