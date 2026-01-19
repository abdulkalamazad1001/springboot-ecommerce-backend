package com.example.ecommerce.dto;

import lombok.Data;

@Data
public class AddToCartRequest {
    private String userId;
    private String productId;
    private Integer quantity;
}
