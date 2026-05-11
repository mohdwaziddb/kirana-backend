package com.kiranastore.kirana.controller;

import com.kiranastore.kirana.entity.User;
import com.kiranastore.kirana.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private AuthService authService;


    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(
            @RequestBody Map<String, String> profileRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String name = profileRequest.get("name");
            
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Name cannot be empty"));
            }

            User user = authService.getUserByEmail(userDetails.getUsername());
            user.setName(name.trim());
            
            User updatedUser = authService.updateUser(user);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profile updated successfully");
            response.put("user", Map.of(
                    "id", updatedUser.getId(),
                    "name", updatedUser.getName(),
                    "email", updatedUser.getEmail(),
                    "role", updatedUser.getRole()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Profile update failed: " + e.getMessage()));
        }
    }

    
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = authService.getUserByEmail(userDetails.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("user", Map.of(
                    "id", user.getId(),
                    "name", user.getName(),
                    "email", user.getEmail(),
                    "role", user.getRole()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to get profile: " + e.getMessage()));
        }
    }
}
