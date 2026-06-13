package com.shopsphere.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("pageTitle", "Home");
        return "index";
    }
    
   
    
    
    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("pageTitle", "Products");
        return "products/list";
    }
    
    
    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        // Create product data based on ID
        Map<String, Object> product = getProductById(id);
        model.addAttribute("product", product);
        return "products/product-details";
    }
    
    private Map<String, Object> getProductById(Long id) {
        Map<String, Object> product = new HashMap<>();
        
        if (id == 1) {
            product.put("id", 1);
            product.put("name", "Premium Wireless Headphones");
            product.put("price", 99.99);
            product.put("oldPrice", 149.99);
            product.put("description", "Experience crystal clear sound with our premium wireless headphones. Features active noise cancellation, 30-hour battery life, and comfortable over-ear design.");
            product.put("brand", "SoundMasters");
            product.put("sku", "WH-1000X");
            product.put("color", "Black, Silver, Blue");
            product.put("warranty", "2 Years");
            product.put("rating", 4.5);
            product.put("reviews", 128);
            product.put("inStock", true);
            product.put("icon", "fa-headphones");
        } else if (id == 2) {
            product.put("id", 2);
            product.put("name", "Smart Watch Pro");
            product.put("price", 199.99);
            product.put("oldPrice", 299.99);
            product.put("description", "Track your fitness, receive notifications, and monitor your health with our advanced smart watch.");
            product.put("brand", "TechFit");
            product.put("sku", "SW-002");
            product.put("color", "Silver, Black");
            product.put("warranty", "1 Year");
            product.put("rating", 5.0);
            product.put("reviews", 256);
            product.put("inStock", true);
            product.put("icon", "fa-clock");
        } else {
            product.put("id", id);
            product.put("name", "Product " + id);
            product.put("price", 99.99);
            product.put("oldPrice", 129.99);
            product.put("description", "Product description goes here.");
            product.put("brand", "Generic");
            product.put("sku", "PRD-" + id);
            product.put("color", "Black");
            product.put("warranty", "1 Year");
            product.put("rating", 4.0);
            product.put("reviews", 50);
            product.put("inStock", true);
            product.put("icon", "fa-box");
        }
        
        return product;
    }
    
    
    
    
    
    
    
    
}