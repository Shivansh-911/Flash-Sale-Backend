package com.shivansh.InventoryEngine.service.impl;

import java.util.Random;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.shivansh.InventoryEngine.service.PaymentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService{

    private final Random random = new Random();

    @Override
    public boolean processPayment(UUID orderID) {
        
        try {
            // Adds a 300ms delay
            // Latency creates - Raace conditions , thread overlap , overselling risk
            Thread.sleep(300);
        } catch (Exception e) {
            System.out.println("Exception = "+e);

            // Set the interrupt status (a boolean flag) of the currently executing thread to true
            Thread.currentThread().interrupt();
        }

        //Returns with a 70% success rate
        return random.nextInt(100) < 70;

     
    }
    
}
