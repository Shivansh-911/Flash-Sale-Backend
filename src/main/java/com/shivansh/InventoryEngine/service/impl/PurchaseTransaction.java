package com.shivansh.InventoryEngine.service.impl;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.shivansh.InventoryEngine.domain.entity.Order;
import com.shivansh.InventoryEngine.domain.entity.OrderStatus;
import com.shivansh.InventoryEngine.domain.entity.OutboxEvent;
import com.shivansh.InventoryEngine.domain.entity.Product;
import com.shivansh.InventoryEngine.repository.OrderRepository;
import com.shivansh.InventoryEngine.repository.OutboxRepository;
import com.shivansh.InventoryEngine.repository.ProductRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PurchaseTransaction {
    
    private final OrderRepository orderRepository;

    private final ProductRepository productRepository;

    private final OutboxRepository outboxRepository;

    // Because Spring Framework transactions work via proxy.
    // Calling this.purchaseTx() will NOT start new transaction.
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public UUID purchaseTx(UUID userId, UUID productId, int qty, String idemKey) {
        
        // idempotency (phase 1 basic)
        var existing = orderRepository.findByIdempotencyKey(idemKey);
        if (existing.isPresent()) {
            return existing.get().getId();
        }

        Product product = productRepository.findById(productId)
                .orElseThrow();

        if (product.getStock() < qty) {
            throw new RuntimeException("Out of stock");
        }


        // OPTIMISTIC LOCKING 

        product.setStock(product.getStock() - qty);
        productRepository.save(product); // optimistic locking happens here

        //--------
        // Step 2
        // flushing is the operation of explicitly forcing data from a temporary memory storage area, called a buffer, to its final destination
        // does sql operations now and not wait till the end of transaction
        entityManager.flush();


        //--------

        Order order = Order.builder()
                .userId(userId)
                .productId(productId)
                .quantity(qty)
                .status(OrderStatus.PENDING)
                .idempotencyKey(idemKey)
                .build();

        orderRepository.save(order);

        //----------
        //   Step 3 
        // after order creation it is saved in outbox table for the async worker to process payment

        OutboxEvent event = OutboxEvent.builder()
                                        .aggregateType("ORDER")
                                        .aggregateId(order.getId())
                                        .type("ORDER_CREATED")
                                        .payload(order.getId().toString())
                                        .processed(false)
                                        .createdAt(Instant.now())
                                        .build();

        outboxRepository.save(event);                                        


        //----------
        // CON For the implementation without flush and outbox event
        // Order is saved even when stock update fails 
        // In Spring JPA Optimistic lock exception usually happens at flush / commit, not at save.
        // Your purchase method lacks: 
        //  explicit flush after stock update
        //  So order insert and stock update are batched together.
        //  Race window appears.
        // Multiple threads insert orders before version conflict is detected.
        // This is a classic flush timing bug.


        return order.getId();
    }


    @Transactional
    public UUID purchaseTxPessimistic(UUID userId, UUID productId, int qty, String idemKey) {
        
        var existing = orderRepository.findByIdempotencyKey(idemKey);
        if (existing.isPresent()) {
            return existing.get().getId();
        }

        Product product = productRepository.findById(productId).orElseThrow();

        if (product.getStock() < qty) {
            throw new RuntimeException("Out of stock");
        }

        // PESSIMISTIC LOCKING
        // In pessimistic locking, the stock is decremented directly in the database using a custom query with a WHERE clause that checks for sufficient stock. 
        // If the update affects 0 rows, it means another transaction has already decremented 
        // if answer 1 then sufficient , if 0 then insufficient stock or concurrent update 

        int updatedRows = productRepository.decrementStock(productId, qty);
        
        if (updatedRows == 0) {
            throw new RuntimeException("Out of stock");
        }

        Order order = Order.builder()
                .userId(userId)
                .productId(productId)
                .quantity(qty)
                .status(OrderStatus.PENDING)
                .idempotencyKey(idemKey)
                .build();

        orderRepository.save(order);

        OutboxEvent event = OutboxEvent.builder()
                                        .aggregateType("ORDER")
                                        .aggregateId(order.getId())
                                        .type("ORDER_CREATED")
                                        .payload(order.getId().toString())
                                        .processed(false)
                                        .createdAt(Instant.now())
                                        .build();

        outboxRepository.save(event); 

        return order.getId();

    }

}
