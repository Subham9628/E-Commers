package com.shopsphere.config;

import com.shopsphere.entity.User;
import com.shopsphere.repository.UserRepository;
import com.shopsphere.services.CartService;
import com.shopsphere.services.CustomUserDetailsService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CartService cartService;
    
    
    @Bean
   SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .userDetailsService(userDetailsService)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/products/**",
                    "/product/**",
                    "/login",
                    "/register",
                    "/css/**",
                    "/js/**",
                    "/images/**"
                ).permitAll()
                .requestMatchers("/cart/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
            	    .loginPage("/login")
            	    .successHandler(loginSuccessHandler())
            	    .permitAll()
            	)
            
        .logout(logout -> logout
        	    .logoutUrl("/logout")
        	    .logoutSuccessUrl("/login?logout=true")
        	    .invalidateHttpSession(true)
        	    .deleteCookies("JSESSIONID", "remember-me")
        	    .permitAll()
        	)
			.rememberMe(rememberMe -> rememberMe
				.key("uniqueAndSecret")
				.tokenValiditySeconds(7 * 24 * 60 * 60) // 7 days
			)
			.csrf(csrf -> csrf.disable()); // Disable CSRF for simplicity, enable in production
        return http.build();
    }
    
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
     PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
   
    @Bean
    AuthenticationSuccessHandler loginSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request,
                                                HttpServletResponse response,
                                                Authentication authentication) 
                                                throws IOException, ServletException {
                
                System.out.println("===== LOGIN SUCCESS HANDLER CALLED =====");
                
                // 1️⃣ Get session
                HttpSession session = request.getSession(false);
                String sessionId = null;
                String storedGuestSession = null;
                
                if (session != null) {
                    // 2️⃣ Get current session ID
                    sessionId = session.getId();
                    System.out.println("📌 Current session ID: " + sessionId);
                    
                    // 3️⃣ Get stored guest session ID (from when items were added)
                    storedGuestSession = (String) session.getAttribute("guestSessionId");
                    if (storedGuestSession != null) {
                        System.out.println("📌 Stored guest session ID: " + storedGuestSession);
                    }
                }
                
                // 4️⃣ Get user
                String email = authentication.getName();
                User user = userRepository.findByEmail(email).orElse(null);
                
                if (user != null) {
                    System.out.println("📌 User: " + email + " (ID: " + user.getId() + ")");
                    
                    // 5️⃣ Try stored session ID first, then current session ID
                    if (storedGuestSession != null) {
                        System.out.println("🔍 Merging with stored session: " + storedGuestSession);
                        cartService.mergeGuestCart(storedGuestSession, user);
                    } else if (sessionId != null) {
                        System.out.println("🔍 Merging with current session: " + sessionId);
                        cartService.mergeGuestCart(sessionId, user);
                    } else {
                        System.out.println("❌ No session ID available for merge");
                    }
                }
                
                response.sendRedirect("/");
            }
        };
    }
    
}