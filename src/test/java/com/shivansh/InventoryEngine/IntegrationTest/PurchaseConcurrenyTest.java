package com.shivansh.InventoryEngine.IntegrationTest;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.shivansh.InventoryEngine.repository.OrderRepository;
import com.shivansh.InventoryEngine.repository.ProductRepository;
import com.shivansh.InventoryEngine.service.ProductService;
import com.shivansh.InventoryEngine.service.PurchaseService;

@SpringBootTest
public class PurchaseConcurrenyTest {
    
    @Autowired
    PurchaseService purchaseService;

    @Autowired
    ProductService productService;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    OrderRepository orderRepository;

    @Test
    public void ConcurrencyTest_ShouldNotOverflow() throws InterruptedException {

        System.out.println("START OF TEST");

        UUID productId = productService.createProduct("Maggie Noddles", 10, 10.5);

        System.out.println("Product ID = "+ productId);


        int threads = 50;

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        for(int i=0;i<threads;i++) {
            
            int idx = i;


            // Execution.submit will start the implementation of all the threads and 
            // when a thread is complete it will decrease the latch value and .await will block the code till latch becomes 0
            executor.submit(() -> {
                try {
                    
                    purchaseService.purchase(
                        UUID.randomUUID(), 
                        productId, 
                        1,
                        "key-"+idx
                    );
                } catch (Exception ignored) {    
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        var product = productRepository.findById(productId).orElseThrow();

        System.out.println("Final Stock = " + product.getStock());

        long orderCount = orderRepository.countByProductId(productId);

        System.out.println("Order Count = " + orderCount);

        System.out.println("END OF TEST");

    }

}
