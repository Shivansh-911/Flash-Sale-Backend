package com.shivansh.InventoryEngine.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shivansh.InventoryEngine.domain.dto.PurchaseRequest;
import com.shivansh.InventoryEngine.service.PurchaseService;

import java.util.UUID;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/purchase")
public class PurchaseController {
    

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @PostMapping("/")
    public UUID purchase(@RequestBody PurchaseRequest req) {

        return purchaseService.purchase(
            req.userId(),
            req.productId(),
            req.quantity(),
            req.idempotencyKey()
        );
    
    }
    
}
