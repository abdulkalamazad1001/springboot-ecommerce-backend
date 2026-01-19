package com.example.ecommerce.service;

import com.example.ecommerce.dto.AddToCartRequest;
import com.example.ecommerce.dto.CartItemResponse;
import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    public CartItem addToCart(AddToCartRequest request) {
        // Validate product exists
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Check if item already in cart
        Optional<CartItem> existingItem = cartRepository.findByUserIdAndProductId(request.getUserId(), request.getProductId());

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            return cartRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setUserId(request.getUserId());
            newItem.setProductId(request.getProductId());
            newItem.setQuantity(request.getQuantity());
            return cartRepository.save(newItem);
        }
    }

    public List<CartItemResponse> getUserCart(String userId) {
        List<CartItem> items = cartRepository.findByUserId(userId);
        List<CartItemResponse> response = new ArrayList<>();

        for (CartItem item : items) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if (product != null) {
                response.add(new CartItemResponse(item.getId(), product, item.getQuantity()));
            }
        }
        return response;
    }

    public void clearCart(String userId) {
        cartRepository.deleteByUserId(userId);
    }
}
