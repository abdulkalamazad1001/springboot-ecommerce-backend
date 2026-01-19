package com.example.ecommerce.dto;

import lombok.Data;

@Data
public class PaymentRequest {
    private String orderId;
    private Double amount;
}
