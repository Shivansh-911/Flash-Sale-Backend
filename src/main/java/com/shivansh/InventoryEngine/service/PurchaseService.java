package com.shivansh.InventoryEngine.service;

import java.util.UUID;


public interface PurchaseService {
    
    UUID purchase(UUID userId, UUID productId, int qty, String idemKey);

}
