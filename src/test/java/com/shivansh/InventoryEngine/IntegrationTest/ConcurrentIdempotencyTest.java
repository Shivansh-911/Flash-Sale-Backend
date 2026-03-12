package com.shivansh.InventoryEngine.IntegrationTest;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import com.shivansh.InventoryEngine.service.ProductService;
import com.shivansh.InventoryEngine.service.PurchaseService;

@SpringBootTest
class ConcurrentIdempotencyTest {

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private ProductService productService;

    

    @Test
    void concurrentDuplicateRequests() throws Exception {

        // System.out.println("START OF THE TEST");

        // UUID userId = UUID.randomUUID();
        // UUID productId = productService.createProduct("Samosa", 10, 20);

        // String idemKey = "flash-sale-key";

        // int threads = 10;

        // ExecutorService executor = Executors.newFixedThreadPool(threads);
        // CountDownLatch latch = new CountDownLatch(threads);

        // List<UUID> results = Collections.synchronizedList(new ArrayList<>());

        // for (int i = 0; i < threads; i++) {

        //     executor.submit(() -> {
        //         try {

        //             UUID orderId = purchaseService.purchase(userId, productId, 1, idemKey);
        //             results.add(orderId);

        //         } catch (Exception ignored) {
        //         } finally {
        //             latch.countDown();
        //         }
        //     });
        // }

        // latch.await();

        // System.out.println("All threads completed. Total results = " + results.size());

        // Set<UUID> uniqueOrders = new HashSet<>(results);

        // System.out.println("Unique orders = " + uniqueOrders.size());

        // assertEquals(1, uniqueOrders.size());
    }
}