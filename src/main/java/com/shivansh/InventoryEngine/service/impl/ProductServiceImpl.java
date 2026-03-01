package com.shivansh.InventoryEngine.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.shivansh.InventoryEngine.domain.entity.Product;
import com.shivansh.InventoryEngine.repository.ProductRepository;
import com.shivansh.InventoryEngine.service.ProductService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService{

    private final ProductRepository productRepository;



    @Override
    public UUID createProduct(String name, int stock, double price) {
        
        LocalDateTime created_at = LocalDateTime.now();

        Product newProduct = Product.builder()
                                    .name(name)
                                    .stock(stock)
                                    .price(price)
                                    .createdAt(created_at)
                                    .build();
        
        productRepository.save(newProduct);
        
        return newProduct.getId();


    }
    

}
