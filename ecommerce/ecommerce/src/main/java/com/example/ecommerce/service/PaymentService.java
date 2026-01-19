package com.example.ecommerce.service;

import com.example.ecommerce.dto.PaymentRequest;
import com.example.ecommerce.dto.PaymentWebhookRequest;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.Payment;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    // We will use this to call our OWN webhook to simulate the bank
    private final RestTemplate restTemplate = new RestTemplate();

    public Payment initiatePayment(PaymentRequest request) {
        // 1. Check if order exists
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!"CREATED".equals(order.getStatus())) {
            throw new RuntimeException("Order is already processed or cancelled");
        }

        // 2. Create Payment Record (Pending)
        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setStatus("PENDING");
        payment.setPaymentId(UUID.randomUUID().toString()); // Generate fake bank ID
        payment.setCreatedAt(Instant.now());

        Payment savedPayment = paymentRepository.save(payment);

        // 3. SIMULATE EXTERNAL BANK (Mock Logic)
        // This runs in the background (async) so the user gets a response immediately
        CompletableFuture.runAsync(() -> {
            try {
                // Wait 3 seconds to simulate bank processing
                Thread.sleep(3000);

                // Create the webhook payload
                PaymentWebhookRequest webhookPayload = new PaymentWebhookRequest(
                        request.getOrderId(),
                        savedPayment.getPaymentId(),
                        "SUCCESS"
                );

                // Call our own webhook endpoint
                String webhookUrl = "http://localhost:8080/api/webhooks/payment";
                restTemplate.postForEntity(webhookUrl, webhookPayload, String.class);

                System.out.println("Mock Bank: Webhook sent successfully for Order " + request.getOrderId());

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return savedPayment;
    }

    // This handles the Webhook (Callback)
    public void processWebhook(PaymentWebhookRequest payload) {
        // 1. Update Payment Status
        Payment payment = paymentRepository.findByOrderId(payload.getOrderId())
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus(payload.getStatus());
        paymentRepository.save(payment);

        // 2. Update Order Status
        Order order = orderRepository.findById(payload.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if ("SUCCESS".equals(payload.getStatus())) {
            order.setStatus("PAID");
        } else {
            order.setStatus("FAILED");
        }
        orderRepository.save(order);
    }
}
