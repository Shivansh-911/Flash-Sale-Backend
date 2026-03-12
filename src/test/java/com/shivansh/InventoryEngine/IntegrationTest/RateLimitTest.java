package com.shivansh.InventoryEngine.IntegrationTest;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.shivansh.InventoryEngine.service.ProductService;
import com.shivansh.InventoryEngine.service.PurchaseService;


@SpringBootTest
public class RateLimitTest {

    

    @Autowired
    private ProductService productService;

    @Autowired
    private PurchaseService purchaseService;

    @Test
    void rateLimitShouldBlockRequests() throws Exception {
    
        // System.out.println("START OF THE TEST");
        // UUID userId = UUID.randomUUID();
        // UUID productId = productService.createProduct("Test Product", 100, 10);

        // for(int i=0;i<10;i++) {

        //     try {
                
        //         purchaseService.purchase(userId, productId, 1, "key-"+i);
                
        //         System.out.println("Purchase attempt " + (i+1) + " succeeded");

        //     } catch (Exception e) {
        //         System.out.println("Blocked by rate limiter on attempt " + (i+1) + ": " + e.getMessage());
        //     }

        // }


    }

}
