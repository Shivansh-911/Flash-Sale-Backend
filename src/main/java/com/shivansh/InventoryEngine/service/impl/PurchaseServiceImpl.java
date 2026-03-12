package com.shivansh.InventoryEngine.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.shivansh.InventoryEngine.service.IdempotencyService;
import com.shivansh.InventoryEngine.service.InventoryCacheService;
import com.shivansh.InventoryEngine.service.PurchaseService;
import com.shivansh.InventoryEngine.service.RateLimiterService;

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
    final private IdempotencyService idempotencyService;
    final private RateLimiterService rateLimiterService;
    final private InventoryCacheService inventoryCacheService;

    @Override
    public UUID purchase(UUID userId, UUID productId, int qty, String idemKey) {

        
        //----------------------
        // Rate Limiting
        rateLimiterService.checkRateLimit(userId);

        //----------------------
        

        //----------------------
        // Hot Inventory Protection
        // Try to update stock in Redis first, if successful then proceed to update stock in DB, otherwise throw out of stock exception
        // In Redis decrement stock for each order request , if less than 0 or no key found do not go further 
        boolean stockUpdated = inventoryCacheService.tryUpdateStock(productId, qty);
        if(!stockUpdated) {
            throw new RuntimeException("Product is out of stock ( REDIS )");
        }

        //----------------------




        
        // ---------------------
        // Idempotency (phase 3) -> Redis based distributed lock with TTL to prevent memory leak in case of failure of the service after acquiring the lock
        String redisKey = "idempotency:" + idemKey;

        boolean canProceed = idempotencyService.tryStart(redisKey);

        if(!canProceed) {

            String orderIdStr = idempotencyService.get(redisKey);
            //If orderIdStr is not null and not "processing", it means the request has already been processed and we can return the orderId
            if(orderIdStr != null && !orderIdStr.equals("processing")) {
                return UUID.fromString(orderIdStr);
            } 
            System.out.println("Duplicate request with idemKey = " + idemKey);
            throw new RuntimeException("Duplicate Request");
        }

        System.out.println("Acquired lock for redisKey = " + redisKey);

        //--------------------------


        

        int attempt = 0;
        int maxAttempts = 5;
        try {

            while(true) {

                try {
                    
                    UUID orderId = purchaseTransaction.purchaseTx(userId, productId, qty, idemKey);

                    // After successful processing of the request, set the key with the orderId as value and TTL to prevent memory leak in Redis
                    idempotencyService.copmplete(redisKey, orderId);
                    System.out.println("Purchase successful, orderId = " + orderId+ ", redisKey = " + redisKey);
                    return orderId;

                
                } catch (Exception ex) {

                    attempt++;

                    System.out.println("OPTIMISTIC LOCK — retry attempt = " + attempt);
                    System.out.println("EXCEPTION = " + ex.getClass().getName());

                    if(attempt >= maxAttempts) {

                        idempotencyService.delete(redisKey);
                        throw ex;
                    }

                }
            }

        } catch (Exception ex) {
            // Restore stock in Redis in case of any failure
            inventoryCacheService.restoreStock(productId, qty);
            throw ex;
        }
        

        //return purchaseTransaction.purchaseTx(userId, productId, qty, idemKey);
    }


    

    

}
