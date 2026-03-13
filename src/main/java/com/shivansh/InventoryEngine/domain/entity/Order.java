package com.shivansh.InventoryEngine.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name="orders",
    uniqueConstraints = @UniqueConstraint(columnNames = "idempotencyKey"),
    indexes = {
        @Index(name = "idx_order_product_id", columnList = "productId"),
        @Index(name = "index_order_status_created_at", columnList = "orderStatus, createdAt")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue
    @Column(name="order_id", nullable = false)
    private UUID id;

    @Column(name="order_user_id", nullable = false)
    private UUID userId;

    @Column(name="order_product_id", nullable = false)
    private UUID productId;

    @Column(name="quantity")
    private int quantity;

    @Column(name="order_status")
    private OrderStatus status;


    //Idempotency means:
    //Multiple identical requests produce the same result
    //If client sends the same request multiple times, it should not cause unintended side effects or duplicate processing.
    //Happens due to network issues, double clicks, client retries etc.
    //Use Redis instead of DB as it is fast and atomic 
    @Column(name="idempotency_key", unique = true)
    private String idempotencyKey;

    @Column(name="created_at")
    private LocalDateTime createdAt;

}
