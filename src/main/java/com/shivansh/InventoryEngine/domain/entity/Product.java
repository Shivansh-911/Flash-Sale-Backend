package com.shivansh.InventoryEngine.domain.entity;


import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    
    @Id
    @GeneratedValue
    @Column(name="product_id", nullable = false, updatable = false)
    private UUID id;


    @Column(name="product_name",nullable = false)
    private String name;

    @Column(name="product_stock")
    private int stock;

    @Column(name="product_price")
    private double price;

    @Version
    private int version;

    @Column(name="created_at")
    private LocalDateTime createdAt;


}
