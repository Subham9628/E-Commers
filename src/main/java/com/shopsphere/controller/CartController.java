package com.shopsphere.controller;

import com.shopsphere.entity.Cart;
import com.shopsphere.services.CartService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    @PostMapping("/cart/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam String productName,
                            @RequestParam Double price,
                            @RequestParam(defaultValue = "1") Integer quantity,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        System.out.println("Adding to cart: " + productName + " (ID: " + productId + ") - Price: " + price + " - Quantity: " + quantity);
        // For now, userId is null (not logged in)
        Long userId = null; // Get from security context when you add authentication
        
        cartService.addToCart(productId, productName, price, quantity, session, userId);
        
        redirectAttributes.addFlashAttribute("success", "Product added to cart!");
        
        return "redirect:/product/" + productId;
    }
    
    @GetMapping("/cart")
    public String viewCart(HttpSession session, org.springframework.ui.Model model) {
        Long userId = null; // Get from security context
        Cart cart = cartService.getOrCreateCart(session, userId);
        model.addAttribute("cart", cart);
        return "cart/view";
    }
}