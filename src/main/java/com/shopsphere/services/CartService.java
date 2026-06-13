package com.shopsphere.services;

import com.shopsphere.entity.Cart;
import com.shopsphere.entity.CartItem;
import com.shopsphere.repository.CartRepository;

import jakarta.servlet.http.HttpSession;

import com.shopsphere.repository.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
public class CartService {
    
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private CartItemRepository cartItemRepository;
    
    // Get or create cart for session
    public Cart getOrCreateCart(HttpSession session, Long userId) {
        String sessionId = session.getId();
        
        // Try to find cart by user ID (if logged in)
        if (userId != null) {
            Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
            if (cartOpt.isPresent()) {
                return cartOpt.get();
            }
        }
        
        // Try to find cart by session ID
        Optional<Cart> cartOpt = cartRepository.findBySessionId(sessionId);
        if (cartOpt.isPresent()) {
            return cartOpt.get();
        }
        
        // Create new cart
        Cart cart = new Cart();
        cart.setSessionId(sessionId);
        cart.setUserId(userId);
        return cartRepository.save(cart);
    }
    
    // Add item to cart
    @Transactional
    public Cart addToCart(Long productId, String productName, Double price, 
                          Integer quantity, HttpSession session, Long userId) {
        
        Cart cart = getOrCreateCart(session, userId);
        
        // Check if product already in cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();
        
        if (existingItem.isPresent()) {
            // Update quantity
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            // Add new item
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProductId(productId);
            newItem.setProductName(productName);
            newItem.setPrice(price);
            newItem.setQuantity(quantity);
            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }
        
        return cartRepository.save(cart);
    }
    
    // Remove item from cart
    @Transactional
    public void removeFromCart(Long cartId, Long productId) {
        cartItemRepository.deleteByCartIdAndProductId(cartId, productId);
    }
    
    // Update quantity
    @Transactional
    public void updateQuantity(Long cartId, Long productId, Integer quantity) {
        Cart cart = cartRepository.findById(cartId).orElse(null);
        if (cart != null) {
            cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .ifPresent(item -> item.setQuantity(quantity));
            cartRepository.save(cart);
        }
    }
    
    // Clear cart
    @Transactional
    public void clearCart(Long cartId) {
        cartRepository.deleteById(cartId);
    }
}