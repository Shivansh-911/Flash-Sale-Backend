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
    //@Scheduled(fixedDelay = 1000)
    public void process() {


        System.out.println("START OF THE OUTBOX PROCESSOR");

        var events = outboxRepository.findTop50ByProcessedFalseOrderByCreatedAtAsc();

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

