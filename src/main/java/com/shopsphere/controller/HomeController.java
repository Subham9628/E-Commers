package com.shopsphere.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        System.out.println("Home page accessed - should show now!");
        
        // Sample products for testing
        List<Map<String, Object>> featuredProducts = new ArrayList<>();
        
        Map<String, Object> product1 = new HashMap<>();
        product1.put("id", 1L);
        product1.put("name", "Premium Wireless Headphones");
        product1.put("shortDescription", "Noise-cancelling, 30hr battery life");
        product1.put("price", 99.99);
        featuredProducts.add(product1);
        
        Map<String, Object> product2 = new HashMap<>();
        product2.put("id", 2L);
        product2.put("name", "Smart Watch Pro");
        product2.put("shortDescription", "Fitness tracking, heart rate monitor");
        product2.put("price", 199.99);
        featuredProducts.add(product2);
        
        Map<String, Object> product3 = new HashMap<>();
        product3.put("id", 3L);
        product3.put("name", "4K Action Camera");
        product3.put("shortDescription", "Waterproof, Wi-Fi enabled");
        product3.put("price", 299.99);
        featuredProducts.add(product3);
        
        Map<String, Object> product4 = new HashMap<>();
        product4.put("id", 4L);
        product4.put("name", "Laptop Backpack");
        product4.put("shortDescription", "Water-resistant, 15.6 inch");
        product4.put("price", 49.99);
        featuredProducts.add(product4);
        
        List<Map<String, Object>> categories = new ArrayList<>();
        
        Map<String, Object> cat1 = new HashMap<>();
        cat1.put("id", 1L);
        cat1.put("name", "Electronics");
        categories.add(cat1);
        
        Map<String, Object> cat2 = new HashMap<>();
        cat2.put("id", 2L);
        cat2.put("name", "Fashion");
        categories.add(cat2);
        
        Map<String, Object> cat3 = new HashMap<>();
        cat3.put("id", 3L);
        cat3.put("name", "Home & Living");
        categories.add(cat3);
        
        Map<String, Object> cat4 = new HashMap<>();
        cat4.put("id", 4L);
        cat4.put("name", "Sports");
        categories.add(cat4);
        
        model.addAttribute("featuredProducts", featuredProducts);
        model.addAttribute("categories", categories);
        model.addAttribute("pageTitle", "Home");
        
        return "index";
    }
    
//    @GetMapping("/products")
//    public String products(Model model) {
//        model.addAttribute("pageTitle", "Products");
//        return "products/list";
//    }
    
//    @GetMapping("/product/{id}")
//    public String productDetail(@PathVariable Long id, Model model) {
//        model.addAttribute("productId", id);
//        model.addAttribute("pageTitle", "Product Details");
//        return "products/detail";
//    }
//    
//    @GetMapping("/login")
//    public String login(Model model) {
//        model.addAttribute("pageTitle", "Login");
//        return "auth/login";
//    }
//    
//    @GetMapping("/register")
//    public String register(Model model) {
//        model.addAttribute("pageTitle", "Register");
//        return "auth/register";
//    }
}