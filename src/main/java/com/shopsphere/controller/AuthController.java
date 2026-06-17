package com.shopsphere.controller;

import com.shopsphere.entity.User;
import com.shopsphere.repository.UserRepository;
import com.shopsphere.services.CartService;
import com.shopsphere.services.UserService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CartService cartService;
    
    
    @GetMapping("/login")
    public String login(@RequestParam(value ="error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        
        if (error != null) {
            model.addAttribute("error", "Invalid email or password");
        }
        
        if (logout != null) {
            model.addAttribute("success", "You have been logged out successfully");
        }
        
        return "auth/login";
    }
    // Merge guest cart when user logs in
    
    @PostMapping("/login")
    public String loginProcess(HttpSession session, Authentication authentication) {
        // Force merge on manual login
        if (authentication != null && authentication.isAuthenticated()) {
            String sessionId = session.getId();
            User user = userRepository.findByEmail(authentication.getName()).orElse(null);
            if (user != null) {
                cartService.mergeGuestCart(sessionId, user);
            }
        }
        return "redirect:/";
    }
  
    
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }
    
    @PostMapping("/register")
    public String registerUser(@RequestParam String firstName,
                               @RequestParam(required = false) String lastName,
                               @RequestParam String email,
                               @RequestParam(required = false) String phoneNumber,
                               @RequestParam String password,
                               @RequestParam String confirmPassword,
                               @RequestParam(defaultValue = "USER") String role,
                               RedirectAttributes redirectAttributes) {
        
        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match");
            return "redirect:/register";
        }
        
        // Check password length
        if (password.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Password must be at least 6 characters");
            return "redirect:/register";
        }
        
        // Create new user with all fields
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        user.setPassword(password);
        user.setRole(role);
        
        // Register user
        boolean registered = userService.registerUser(user);
        
        if (registered) {
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/login";
        } else {
            redirectAttributes.addFlashAttribute("error", "Email already exists");
            return "redirect:/register";
        }
    }
}