package com.example.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentWebhookRequest {
    private String orderId;
    private String paymentId;
    private String status; // SUCCESS or FAILED
}
