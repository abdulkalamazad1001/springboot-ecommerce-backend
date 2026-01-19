package com.example.ecommerce.service;

import com.example.ecommerce.dto.CreateOrderRequest;
import com.example.ecommerce.model.*;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartService cartService; // To reuse clearCart

    @Transactional // Ensures if one part fails, everything rolls back
    public Order createOrder(CreateOrderRequest request) {
        // 1. Get cart items
        List<CartItem> cartItems = cartRepository.findByUserId(request.getUserId());

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0.0;

        // 2. Process each item
        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // Calculate item total
            double itemTotal = product.getPrice() * cartItem.getQuantity();
            totalAmount += itemTotal;

            // Create OrderItem snapshot
            OrderItem orderItem = new OrderItem(
                    product.getId(),
                    product.getName(),
                    cartItem.getQuantity(),
                    product.getPrice()
            );
            orderItems.add(orderItem);
        }

        // 3. Create Order
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setTotalAmount(totalAmount);
        order.setStatus("CREATED");
        order.setCreatedAt(Instant.now());
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        // 4. Clear Cart
        cartService.clearCart(request.getUserId());

        return savedOrder;
    }

    public Order getOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }
}
