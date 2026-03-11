package com.shivansh.InventoryEngine.service;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.shivansh.InventoryEngine.domain.entity.OrderStatus;
import com.shivansh.InventoryEngine.repository.OrderRepository;
import com.shivansh.InventoryEngine.repository.OutboxRepository;
import com.shivansh.InventoryEngine.repository.ProductRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OutboxProcessor {

    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;
    private final PaymentService paymentService;
    private final ProductRepository productRepository;
    

    @Transactional
    @Scheduled(fixedDelay = 1000)
    public void process() {


        System.out.println("START OF THE OUTBOX PROCESSOR");

        // Right Now i only have one type of event -> Payment processing, so i am directly converting the payload to orderId, 
        // but in real world we will have multiple types of events and we can use a common format for payload like JSON with a field eventType 
        // to identify the type of event and then process accordingly. 
         
        // Also i have only one worker thread for processing the outbox event but in real worls there can be many workers so 
        // to avoid race conditions we can do pessimistic locking on the outbox event table by locking a certain number of rows to one worker 
        // so that they are skipped by other workers
        var events = outboxRepository.findTop50ByProcessedFalseOrderByCreatedAtAsc();

        System.out.println("Outbox worker running. Events: " + events.size());

        for (var event : events) {

            try {

                UUID orderId = UUID.fromString(event.getPayload());

                boolean success = paymentService.processPayment(orderId);

                if (success) {
                    handleSuccess(orderId);
                } else {
                    handleFailure(orderId);
                }

                event.setProcessed(true);

            } catch (Exception e) {
                // DO NOT mark processed -> retry
            }
        }
    }

    private void handleSuccess(UUID orderId) {
        var order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus(OrderStatus.COMPLETED);
    }

    private void handleFailure(UUID orderId) {

        var order = orderRepository.findById(orderId).orElseThrow();

        // idempotency safety
        if (order.getStatus() == OrderStatus.FAILED) {
            return;
        }

        order.setStatus(OrderStatus.FAILED);
        var product = productRepository.findById(order.getProductId()).orElseThrow();
        product.setStock(product.getStock() + order.getQuantity());
    }
}

