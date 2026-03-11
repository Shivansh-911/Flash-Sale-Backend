package com.shivansh.InventoryEngine.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.jdbc.Sql;

import com.shivansh.InventoryEngine.domain.entity.Order;
import com.shivansh.InventoryEngine.domain.entity.OrderStatus;
import com.shivansh.InventoryEngine.domain.entity.Product;
import com.shivansh.InventoryEngine.repository.OrderRepository;
import com.shivansh.InventoryEngine.repository.ProductRepository;
import com.shivansh.InventoryEngine.service.OutboxProcessor;
import com.shivansh.InventoryEngine.service.ProductService;
import com.shivansh.InventoryEngine.service.PurchaseService;

@SpringBootTest
@EnableScheduling
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class PurchaseAsyncFlowTest {

    //Same as declaring a  variable with required args constructor
    @Autowired
    ProductRepository productRepository;
    
    @Autowired
    ProductService productService;

    @Autowired
    PurchaseService purchaseService;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OutboxProcessor outboxProcessor;
    
    @Test
    public void async_flow_test() throws Exception {

        System.out.println("START OF THE TEST");

        UUID productId = productService.createProduct("Samosa", 10, 20);

        int threads = 30;

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        for(int i=0;i<threads;i++) {
            int idx = i;

            executor.submit(() -> {
                try {
                    
                    purchaseService.purchase(UUID.randomUUID(), productId, 1, "Key + "+idx);

                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        executor.shutdown();

        Thread.sleep(5000);

        List<Order> orders = orderRepository.findAll();

        int completed = (int)orders.stream().filter(o -> o.getStatus() == OrderStatus.COMPLETED).count();
        
        int failed = (int)orders.stream().filter(o -> o.getStatus() == OrderStatus.FAILED).count();

        int pending = (int)orders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();


        Product product = productRepository.findById(productId).orElseThrow();

        int final_stock = product.getStock();

        int expected_stock = 10 - completed;

        assertThat(final_stock).isEqualTo(expected_stock);

        assertThat(final_stock).isGreaterThanOrEqualTo(0);

        assertThat(completed + failed).isEqualTo(orders.size());

        System.out.println("Completed: " + completed);
        System.out.println("Failed: " + failed);
        System.out.println("Pending: " + pending);
        System.out.println("Final stock: " + final_stock);


        // // outboxProcessor.process();

        // // orders = orderRepository.findAll();
        // // completed = (int)orders.stream().filter(o -> o.getStatus() == OrderStatus.COMPLETED).count();
        // // failed = (int)orders.stream().filter(o -> o.getStatus() == OrderStatus.FAILED).count();
        // // pending = (int)orders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();

        // // product = productRepository.findById(productId).orElseThrow();
        // // final_stock = product.getStock();

        // // System.out.println("After outbox processing:");
        // // System.out.println("Completed: " + completed);
        // // System.out.println("Failed: " + failed);
        // // System.out.println("Pending: " + pending);
        // // System.out.println("Final stock: " + final_stock);

        // // System.out.println("After outbox processing:");
        // // System.out.println("Completed: " + completed);
        // // System.out.println("Failed: " + failed);
        



    }



}