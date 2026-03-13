package com.shivansh.InventoryEngine.service.impl;

import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
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

    private static final Logger log = LogManager.getLogger(PurchaseServiceImpl.class);

    @Override
    public UUID purchase(UUID userId, UUID productId, int qty, String idemKey) {

        
        log.info("REQ START | idemKey={} | user={} | product={} | qty={}", idemKey, userId, productId, qty);

        //----------------------
        // Rate Limiting
        rateLimiterService.checkRateLimit(userId);
        log.info("RATE LIMIT OK | idemKey={} | user={}", idemKey, userId);
        //----------------------
        
    
        //----------------------
        // Hot Inventory Protection
        // Try to update stock in Redis first, if successful then proceed to update stock in DB, otherwise throw out of stock exception
        // In Redis decrement stock for each order request , if less than 0 or no key found do not go further 
        
        boolean stockUpdated = inventoryCacheService.tryUpdateStock(productId, qty);
        log.info("REDIS STOCK RESULT | idemKey={} | updated={}", idemKey, stockUpdated);        

        if(!stockUpdated) {
            log.warn("REDIS STOCK FAILED | idemKey={} | product={}", idemKey, productId);
            throw new RuntimeException("Product is out of stock ( REDIS )");
        }

        //----------------------




        
        // ---------------------
        // Idempotency (phase 3) -> Redis based distributed lock with TTL to prevent memory leak in case of failure of the service after acquiring the lock
        String redisKey = "idempotency:" + idemKey;
        boolean canProceed = idempotencyService.tryStart(redisKey);
        
        log.info("IDEMPOTENCY TRY | redisKey={} | canProceed={}", redisKey, canProceed);
        
        if(!canProceed) {

            String orderIdStr = idempotencyService.get(redisKey);
            
            //If orderIdStr is not null and not "processing", it means the request has already been processed and we can return the orderId
            if(orderIdStr != null && !orderIdStr.equals("processing")) {
                log.info("IDEMPOTENCY HIT | redisKey={} | existingOrder={}", redisKey, orderIdStr);
                return UUID.fromString(orderIdStr);
            } 

            log.warn("DUPLICATE REQUEST BLOCKED | redisKey={}", redisKey);
            throw new RuntimeException("Duplicate Request");

        }

        log.info("IDEMPOTENCY LOCK ACQUIRED | redisKey={}", redisKey);

        //--------------------------


        

        int attempt = 0;
        int maxAttempts = 5;
        try {

            while(true) {

                try {
                    
                    log.info("DB TRANSACTION START | redisKey={} | attempt={}", redisKey, attempt);

                    UUID orderId = purchaseTransaction.purchaseTx(userId, productId, qty, idemKey);

                    // After successful processing of the request, set the key with the orderId as value and TTL to prevent memory leak in Redis
                    idempotencyService.complete(redisKey, orderId);

                    log.info("PURCHASE SUCCESS | orderId={} | redisKey={}", orderId, redisKey);
                    
                    return orderId;
                
                } catch (Exception ex) {

                    attempt++;

                    log.warn("OPTIMISTIC LOCK RETRY | redisKey={} | attempt={} | exception={}", redisKey, attempt, ex.getClass().getSimpleName());

                    if(attempt >= maxAttempts) {

                        log.error("MAX RETRIES REACHED | redisKey={} | deleting idempotency key", redisKey);
                        idempotencyService.delete(redisKey);
                        throw ex;
                    }

                }
            }

        } catch (Exception ex) {
            // Restore stock in Redis in case of any failure
            log.error("PURCHASE FAILED | redisKey={} | restoring redis stock | exception={}", redisKey, ex.getClass().getSimpleName());
            inventoryCacheService.restoreStock(productId, qty);
            throw ex;
        }
        

        //return purchaseTransaction.purchaseTx(userId, productId, qty, idemKey);
    }

    @Override
    public UUID purchasePessimistic(UUID userId, UUID productId, int qty, String idemKey) {
        
        log.info("REQ START | idemKey={} | user={} | product={} | qty={}", idemKey, userId, productId, qty);

        //rate limit
        rateLimiterService.checkRateLimit(userId);
        log.info("RATE LIMIT OK | idemKey={} | user={}", idemKey, userId);

        //Stock check and update in Redis
        boolean stockUpdates = inventoryCacheService.tryUpdateStock(productId, qty);
        log.info("REDIS STOCK RESULT | idemKey={} | updated={}", idemKey, stockUpdates);

        if(!stockUpdates) {
            log.warn("REDIS STOCK FAILED | idemKey={} | product={}", idemKey, productId);
            throw new RuntimeException("Product is out of stock ( REDIS )");
        }

        // Idempotency check
        String redisKey = "idempotency:" + idemKey;
        boolean canProceed = idempotencyService.tryStart(redisKey);
        log.info("IDEMPOTENCY TRY | redisKey={} | canProceed={}", redisKey, canProceed);
        if(!canProceed) {

            String orderIdStr = idempotencyService.get(redisKey);
            
            //If orderIdStr is not null and not "processing", it means the request has already been processed and we can return the orderId
            if(orderIdStr != null && !orderIdStr.equals("processing")) {
                log.info("IDEMPOTENCY HIT | redisKey={} | existingOrder={}", redisKey, orderIdStr);
                return UUID.fromString(orderIdStr);
            } 

            log.warn("DUPLICATE REQUEST BLOCKED | redisKey={}", redisKey);
            throw new RuntimeException("Duplicate Request");

        }
        log.info("IDEMPOTENCY LOCK ACQUIRED | redisKey={}", redisKey);




        // DB Atomic Transaction with Pessimistic Locking
        try {
            
            UUID orderId = purchaseTransaction.purchaseTxPessimistic(userId, productId, qty, idemKey);
            
            idempotencyService.complete(redisKey, orderId);

            log.info("PURCHASE SUCCESS | orderId={} | redisKey={}", orderId, redisKey);

            return orderId;
        
        } catch (Exception ex) {

            log.error("PURCHASE FAILED | redisKey={} | restoring redis stock | deleting idempotency key | exception={}", redisKey, ex.getClass().getSimpleName());
            inventoryCacheService.restoreStock(productId, qty);
            idempotencyService.delete(redisKey);
            throw ex;
        }


    }
    

    

}
