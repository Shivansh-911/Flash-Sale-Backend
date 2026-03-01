package com.shivansh.InventoryEngine.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;


import com.shivansh.InventoryEngine.service.PurchaseService;


import lombok.RequiredArgsConstructor;

// Optimistic locking assumes conflicts are rare, allowing simultaneous read/write access and checking for data changes only at commit, 
// making it ideal for low-contention, high-read systems. 
// 
// Pessimistic locking assumes conflicts are likely, locking data upon read to prevent others from modifying it, 
// ensuring data integrity in high-contention, critical-operation systems



@Service
//makes the default arguments constructer for bean allocation 
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

    final private PurchaseTransaction purchaseTransaction;

    @Override
    public UUID purchase(UUID userId, UUID productId, int qty, String idemKey) {
        
        int attempt = 0;
        int maxAttempts = 5;
        
        while(true) {

            try {

                return purchaseTransaction.purchaseTx(userId, productId, qty, idemKey);
            
            } catch (Exception ex) {

                attempt++;

                System.out.println("OPTIMISTIC LOCK — retry attempt = " + attempt);
                System.out.println("EXCEPTION = " + ex.getClass().getName());

                if(attempt >= maxAttempts)
                    throw ex;

            }


        }

        //return purchaseTransaction.purchaseTx(userId, productId, qty, idemKey);
    }


    

    

}
