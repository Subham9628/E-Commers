package com.shopsphere.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("pageTitle", "Home");
        return "index";
    }
    
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }
    
    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }
    
    @PostMapping("/register")
    public String registerUser(@RequestParam String firstName,
                               @RequestParam String lastName,
                               @RequestParam String email,
                               @RequestParam String password,
                               RedirectAttributes redirectAttributes) {
        // Here you would save user to database
        // For now, just redirect to login with success message
        redirectAttributes.addAttribute("registered", true);
        return "redirect:/login";
    }
    
    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("pageTitle", "Products");
        return "products";
    }
    
    @GetMapping("/product/{id}")
    public String productDetail() {
        return "product-detail";
    }
}