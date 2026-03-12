package com.shivansh.InventoryEngine.service;

import java.util.UUID;

public interface IdempotencyService {

    public boolean tryStart(String key);

    public void copmplete(String key, UUID orderId);

    public String get(String key);

    public void delete(String key);
    
}
