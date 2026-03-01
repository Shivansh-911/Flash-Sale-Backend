package com.shivansh.InventoryEngine.domain.dto;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PurchaseRequest(

    @NotNull(message = "id is required")
    UUID userId,

    @NotNull(message = "name is required")
    UUID productId,
    
    @Min(value = 1,message = "quantity must be atleast 1")
    int quantity,

    @NotBlank(message = "idempotencyKey is required")
    String idempotencyKey



) {
    
}
