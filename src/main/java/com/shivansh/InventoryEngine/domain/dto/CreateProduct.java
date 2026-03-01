package com.shivansh.InventoryEngine.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateProduct(
    @NotNull(message="product name required")
    String name,

    @Min(value = 1,message = "product quantity must be more than 1")
    int stock,

    @Min(value = 1,message = "product price should be more tha 1")
    double price
) {

}
