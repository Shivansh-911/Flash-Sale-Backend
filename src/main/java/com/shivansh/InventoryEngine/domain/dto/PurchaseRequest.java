package com.shivansh.InventoryEngine.domain.dto;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

//record class is immutable and thread safe. It is used to store data and it is a good choice for DTOs.

public record PurchaseRequest(

    @NotNull(message = "id is required")
    UUID userId,

    @NotNull(message = "productId is required")
    UUID productId,
    
    @Min(value = 1,message = "quantity must be atleast 1")
    int quantity,

    @NotBlank(message = "idempotencyKey is required")
    String idempotencyKey



) {
    
}
