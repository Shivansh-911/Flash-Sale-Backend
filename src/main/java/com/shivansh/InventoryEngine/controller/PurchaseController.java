package com.shivansh.InventoryEngine.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shivansh.InventoryEngine.domain.dto.PurchaseRequest;
import com.shivansh.InventoryEngine.service.PurchaseService;

import jakarta.validation.Valid;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


//@RequestBody controller will convert json to java object 
//It uses Sptring boot MappingJackson2HttpMessageConverter to convert json to java object.
//@ResponseEntity is used to return response with status code and body. It is a good practice to use ResponseEntity in controller methods.
//It converts java object to json 


@RestController
@RequestMapping("/api/purchase")
public class PurchaseController {
    

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @PostMapping("/")
    public ResponseEntity<UUID> purchase(@Valid @RequestBody PurchaseRequest req) {

        UUID orderId = purchaseService.purchasePessimistic(
            req.userId(),
            req.productId(),
            req.quantity(),
            req.idempotencyKey()
        );

        if(orderId == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(orderId);
    
    }
    
}
