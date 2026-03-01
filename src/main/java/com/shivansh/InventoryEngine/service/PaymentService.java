package com.shivansh.InventoryEngine.service;

import java.util.UUID;

public interface PaymentService {
    
    boolean processPayment(UUID orderID);

}
