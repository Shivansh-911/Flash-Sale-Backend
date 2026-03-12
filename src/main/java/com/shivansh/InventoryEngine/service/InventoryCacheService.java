package com.shivansh.InventoryEngine.service;

import java.util.UUID;

public interface InventoryCacheService {

    void initializeStock(UUID productId, int stock);

    boolean tryUpdateStock(UUID productId, int quantity);

    void restoreStock(UUID productId, int quantity);

}
