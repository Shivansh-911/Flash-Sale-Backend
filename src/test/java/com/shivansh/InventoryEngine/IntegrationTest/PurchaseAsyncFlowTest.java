package com.shivansh.InventoryEngine.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
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

    private static final Logger log = LogManager.getLogger(PurchaseAsyncFlowTest.class);
    
    @Test
    public void async_flow_test() throws Exception {

        log.info("===== TEST START =====");

        UUID productId = productService.createProduct("Samosa", 10, 20);

        log.info("PRODUCT CREATED | productId={} | initialStock={}", productId, 10);

        int threads = 100;

        log.info("STARTING CONCURRENCY TEST | threads={}", threads);

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        long startTime = System.currentTimeMillis();

        for(int i=0;i<threads;i++) {
            int idx = i;

            executor.submit(() -> {

                log.debug("THREAD START | idemKey={}", "key-"+idx);

                try {
                    
                    UUID orderId = purchaseService.purchasePessimistic(UUID.randomUUID(), productId, 1, "key-"+idx);
                    //UUID orderId = purchaseService.purchase(UUID.randomUUID(), productId, 1, "key-"+idx);

                    log.info("PURCHASE SUCCESS | idemKey={} | orderId={}", "key-"+idx, orderId);

                } catch (Exception ex) {

                    log.warn("PURCHASE FAILED | idemKey={} | error={}", "key-"+idx, ex.getMessage());
                
                } finally {
                    
                    latch.countDown();
                    log.debug("THREAD END | idemKey={}", "key-"+idx);
                }
            });
        }

        latch.await();

        executor.shutdown();

        long endTime = System.currentTimeMillis();

        Thread.sleep(5000);

        log.info("FETCHING ORDERS FROM DATABASE");


        List<Order> orders = orderRepository.findAll();

        int completed = (int)orders.stream().filter(o -> o.getStatus() == OrderStatus.COMPLETED).count();
        
        int failed = (int)orders.stream().filter(o -> o.getStatus() == OrderStatus.FAILED).count();

        int pending = (int)orders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();


        Product product = productRepository.findById(productId).orElseThrow();

        int final_stock = product.getStock();

        int expected_stock = 10 - completed;

        log.info("===== ORDER SUMMARY =====");
        log.info("Orders Created = {}", orders.size());
        log.info("Completed orders payment successful = {}", completed);
        log.info("Failed orders payment unsuccessful = {}", failed);
        log.info("Pending orders payment pending = {}", pending);
        log.info("Final stock = {}", final_stock);
        log.info("Expected stock = {}", expected_stock);
        log.info("Total time taken = {} ms", (endTime - startTime));

        assertThat(final_stock).isEqualTo(expected_stock);

        assertThat(final_stock).isGreaterThanOrEqualTo(0);

        assertThat(completed + failed).isEqualTo(orders.size());


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