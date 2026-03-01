package com.shivansh.InventoryEngine.service;

import java.util.UUID;

public interface ProductService {
    
    UUID createProduct(String name, int stock, double price);

}
