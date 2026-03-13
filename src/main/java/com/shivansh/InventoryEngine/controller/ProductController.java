package com.shivansh.InventoryEngine.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shivansh.InventoryEngine.domain.dto.CreateProduct;
import com.shivansh.InventoryEngine.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {
    
    final private ProductService productService;



    @PostMapping("/")
    public ResponseEntity<UUID> createProduct(@Valid @RequestBody CreateProduct req) {
        
        UUID productId = productService.createProduct(
            req.name(),
            req.stock(),
            req.price()
        );
        System.out.println("productId: " + productId);
        if(productId == null) {
            return ResponseEntity.badRequest().build();
        }   
        return ResponseEntity.ok(productId);
    }
    

}
