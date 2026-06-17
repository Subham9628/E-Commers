package com.shopsphere.controller;

import com.shopsphere.entity.Cart;
import com.shopsphere.entity.User;
import com.shopsphere.repository.UserRepository;
import com.shopsphere.services.CartService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private UserRepository userRepository;
    
    @PostMapping("/cart/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam String productName,
                            @RequestParam Double price,
                            @RequestParam Integer quantity,
                            Authentication authentication,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        
        String sessionId = session.getId();
        System.out.println("🛒 Adding to cart - Session ID: " + sessionId);
        
        // ✅ Store session ID for later merge
        session.setAttribute("guestSessionId", sessionId);
        System.out.println("📌 Stored guestSessionId: " + sessionId);
        
        User user = null;
        if (authentication != null && authentication.isAuthenticated()) {
            user = userRepository.findByEmail(authentication.getName()).orElse(null);
        }
        
        cartService.addToCart(productId, productName, price, quantity, user, sessionId);
        
        redirectAttributes.addFlashAttribute("success", productName + " added to cart!");
        
        return "redirect:/cart";
    }
    
    @GetMapping("/cart")
    public String viewCart(Model model, Authentication authentication, HttpSession session) {
        Cart cart = null;
        
        // ✅ Use the SAME session ID for guest
        String sessionId = session.getId();
        System.out.println("Current session ID: " + sessionId);
        
        if (authentication != null && authentication.isAuthenticated()) {
            User user = userRepository.findByEmail(authentication.getName()).orElse(null);
            if (user != null) {
                cart = cartService.getCartByUser(user);
                System.out.println("User cart: " + cart.getId() + " items: " + cart.getItems().size());
            }
        } else {
            cart = cartService.getCartBySession(sessionId);
            System.out.println("Guest cart: " + cart.getId() + " items: " + cart.getItems().size());
        }
        
        model.addAttribute("cart", cart);
        return "cart/view";
    }
    
    @PostMapping("/cart/update")
    public String updateQuantity(@RequestParam Long productId,
                                 @RequestParam Integer quantity,
                                 Authentication authentication,
                                 HttpSession session) {
        
        Cart cart;
        if (authentication != null && authentication.isAuthenticated()) {
            User user = userRepository.findByEmail(authentication.getName()).orElse(null);
            cart = cartService.getCartByUser(user);
        } else {
            cart = cartService.getCartBySession(session.getId());
        }
        
        cartService.updateQuantity(cart.getId(), productId, quantity);
        
        return "redirect:/cart";
    }
    
    @PostMapping("/cart/remove")
    public String removeItem(@RequestParam Long productId,
                             Authentication authentication,
                             HttpSession session) {
        
        Cart cart;
        if (authentication != null && authentication.isAuthenticated()) {
            User user = userRepository.findByEmail(authentication.getName()).orElse(null);
            cart = cartService.getCartByUser(user);
        } else {
            cart = cartService.getCartBySession(session.getId());
        }
        
        cartService.removeItem(cart.getId(), productId);
        
        return "redirect:/cart";
    }
    
    
}