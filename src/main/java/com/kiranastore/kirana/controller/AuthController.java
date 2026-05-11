package com.kiranastore.kirana.controller;

import com.kiranastore.kirana.entity.UserRole;
import com.kiranastore.kirana.entity.User;
import com.kiranastore.kirana.service.AuthService;
import com.kiranastore.kirana.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, Object> registrationData) {
        try {
            String name = (String) registrationData.get("name");
            String username = (String) registrationData.get("username");
            String email = (String) registrationData.get("email");
            String mobile = (String) registrationData.get("mobile");
            String password = (String) registrationData.get("password");
            String roleStr = (String) registrationData.get("role");

            // Validate input
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Name is required"));
            }
            
            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Username is required"));
            }
            
            if (username.length() < 3) {
                return ResponseEntity.badRequest().body(Map.of("message", "Username must be at least 3 characters"));
            }
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
            }
            
            if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid email format"));
            }
            
            if (mobile == null || mobile.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Mobile number is required"));
            }
            
            if (!mobile.matches("^[0-9]{10}$")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Mobile number must be 10 digits"));
            }
            
            if (password == null || password.length() < 6) {
                return ResponseEntity.badRequest().body(Map.of("message", "Password must be at least 6 characters"));
            }
            
            if (roleStr == null || (!roleStr.equals("BUYER") && !roleStr.equals("SELLER"))) {
                return ResponseEntity.badRequest().body(Map.of("message", "Role must be either BUYER or SELLER"));
            }

            UserRole role = UserRole.valueOf(roleStr);

            User user = authService.registerUser(name, username, email, mobile, password, role);

            return ResponseEntity.ok(Map.of(
                "message", "User registered successfully",
                "user", Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "name", user.getName(),
                    "username", user.getUsername(),
                    "mobile", user.getMobile(),
                    "role", user.getRole().toString()
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginRequest) {
        try {
            String identifier = loginRequest.get("identifier");
            String password = loginRequest.get("password");

            if (identifier == null || identifier.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Email, username, or mobile is required"));
            }

            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Password is required"));
            }

            Map<String, Object> result = authService.loginUser(identifier, password);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/test")
    public ResponseEntity<?> testAuth() {
        return ResponseEntity.ok(Map.of("message", "Auth endpoint is working"));
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String authHeader, @RequestBody Map<String, String> passwordRequest) {
        try {
            String currentPassword = passwordRequest.get("currentPassword");
            String newPassword = passwordRequest.get("newPassword");

            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Current password is required"));
            }

            if (newPassword == null || newPassword.length() < 6) {
                return ResponseEntity.badRequest().body(Map.of("message", "New password must be at least 6 characters"));
            }

            // Extract email from token
            String token = authHeader.replace("Bearer ", "");
            String email = jwtTokenUtil.getUsernameFromToken(token);

            boolean success = authService.changePassword(email, currentPassword, newPassword);

            if (success) {
                return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "Current password is incorrect"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
