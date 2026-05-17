package com.kiranastore.kirana.controller;

import com.kiranastore.kirana.entity.User;
import com.kiranastore.kirana.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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

    @Value("${app.version}")
    private String backendAppVersion;

    private static final String VERSION_ERROR_MESSAGE =
            "App version update required. Kripya app update karein ya Admin se contact karein. Support: 8130703196";

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
    public ResponseEntity<?> getProfile(
            @RequestHeader(value = "X-App-Version", required = false) String frontendAppVersion,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            if (!isCompatibleAppVersion(frontendAppVersion)) {
                return ResponseEntity.status(HttpStatus.UPGRADE_REQUIRED).body(Map.of(
                        "code", "APP_VERSION_MISMATCH",
                        "message", VERSION_ERROR_MESSAGE,
                        "frontendVersion", frontendAppVersion == null ? "" : frontendAppVersion,
                        "backendVersion", backendAppVersion
                ));
            }

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

    private boolean isCompatibleAppVersion(String frontendAppVersion) {
        return frontendAppVersion != null
                && frontendAppVersion.trim().equals(backendAppVersion.trim());
    }
}
