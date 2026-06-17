package com.shopsphere.services;

import com.shopsphere.entity.Cart;
import com.shopsphere.entity.CartItem;
import com.shopsphere.entity.User;
import com.shopsphere.repository.CartRepository;
import com.shopsphere.repository.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {
    
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private CartItemRepository cartItemRepository;
    
    // Get cart for logged-in user
    public Cart getCartByUser(User user) {
        return cartRepository.findByUser(user).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });
    }
    
    // Get cart for guest user
    public Cart getCartBySession(String sessionId) {
        return cartRepository.findBySessionId(sessionId).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setSessionId(sessionId);
            return cartRepository.save(newCart);
        });
    }
    
    // Add item to cart
    @Transactional
    public Cart addToCart(Long productId, String productName, Double price, 
                          Integer quantity, User user, String sessionId) {
        
        Cart cart;
        if (user != null) {
            cart = getCartByUser(user);
        } else {
            cart = getCartBySession(sessionId);
        }
        
        // Check if product already in cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();
        
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
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
    
    // Update quantity
    @Transactional
    public void updateQuantity(Long cartId, Long productId, Integer quantity) {
        Cart cart = cartRepository.findById(cartId).orElse(null);
        if (cart != null) {
            cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .ifPresent(item -> {
                    item.setQuantity(quantity);
                    cartItemRepository.save(item);
                });
        }
    }
    
    // Remove item
    @Transactional
    public void removeItem(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId).orElse(null);
        if (cart != null) {
            cart.getItems().removeIf(item -> item.getProductId().equals(productId));
            cartRepository.save(cart);
        }
    }
    
    // Clear cart
    @Transactional
    public void clearCart(Long cartId) {
        cartRepository.deleteById(cartId);
    }
    
    // Merge guest cart with user cart after login
    @Transactional
    public void mergeGuestCart(String sessionId, User user) {
        System.out.println("===== 🔄 MERGE GUEST CART START =====");
        System.out.println("📌 Session ID: " + sessionId);
        System.out.println("📌 User: " + user.getEmail());
        
        // 1️⃣ Find guest cart by session ID
        Optional<Cart> guestCartOpt = cartRepository.findBySessionId(sessionId);
        
        if (guestCartOpt.isEmpty()) {
            System.out.println("❌ No guest cart found for session: " + sessionId);
            System.out.println("🔍 Checking for any guest cart with items...");
            
            // 2️⃣ If not found, check for ANY guest cart with items
            List<Cart> guestCarts = cartRepository.findAll().stream()
                    .filter(c -> c.getUser() == null && !c.getItems().isEmpty())
                    .collect(Collectors.toList());
            
            if (!guestCarts.isEmpty()) {
                System.out.println("📌 Found " + guestCarts.size() + " guest carts with items");
                // Merge the FIRST one (or you can merge all)
                Cart guestCart = guestCarts.get(0);
                System.out.println("📌 Using guest cart ID: " + guestCart.getId() + " (session: " + guestCart.getSessionId() + ")");
                mergeCartWithUser(guestCart, user);
            } else {
                System.out.println("❌ No guest carts with items found");
            }
            return;
        }
        
        Cart guestCart = guestCartOpt.get();
        mergeCartWithUser(guestCart, user);
    }

    // New method to merge a specific cart with user
    private void mergeCartWithUser(Cart guestCart, User user) {
        System.out.println("📌 Merging guest cart ID: " + guestCart.getId());
        System.out.println("📌 Guest cart items: " + guestCart.getItems().size());
        
        // Print guest items
        for (CartItem item : guestCart.getItems()) {
            System.out.println("   - " + item.getProductName() + " x " + item.getQuantity());
        }
        
        // Get user cart
        Cart userCart = getCartByUser(user);
        System.out.println("📌 User cart ID: " + userCart.getId());
        System.out.println("📌 User cart items before merge: " + userCart.getItems().size());
        
        // Merge items
        for (CartItem guestItem : guestCart.getItems()) {
            Optional<CartItem> existingItem = userCart.getItems().stream()
                    .filter(item -> item.getProductId().equals(guestItem.getProductId()))
                    .findFirst();
            
            if (existingItem.isPresent()) {
                // Update quantity
                CartItem item = existingItem.get();
                int newQty = item.getQuantity() + guestItem.getQuantity();
                item.setQuantity(newQty);
                cartItemRepository.save(item);
                System.out.println("   ✅ Updated: " + guestItem.getProductName() + " → qty: " + newQty);
            } else {
                // Add new item
                guestItem.setCart(userCart);
                userCart.getItems().add(guestItem);
                cartItemRepository.save(guestItem);
                System.out.println("   ➕ Added: " + guestItem.getProductName() + " x " + guestItem.getQuantity());
            }
        }
        
        // Save user cart
        cartRepository.save(userCart);
        System.out.println("📌 User cart items after merge: " + userCart.getItems().size());
        
        // Delete guest cart
        cartRepository.delete(guestCart);
        System.out.println("🗑️ Guest cart deleted");
        System.out.println("===== ✅ MERGE COMPLETE =====");
    }
}