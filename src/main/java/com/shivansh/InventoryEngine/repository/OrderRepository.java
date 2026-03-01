package com.shivansh.InventoryEngine.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shivansh.InventoryEngine.domain.entity.Order;

import java.util.Optional;



@Repository
public interface OrderRepository extends JpaRepository<Order, UUID>{ 

    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    long countByProductId(UUID productId);


}
