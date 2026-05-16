package com.kiranastore.kirana.controller;

import com.kiranastore.kirana.entity.History;
import com.kiranastore.kirana.entity.User;
import com.kiranastore.kirana.service.HistoryService;
import com.kiranastore.kirana.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/history")
@CrossOrigin(origins = "*")
public class HistoryController {
    
    @Autowired
    private HistoryService historyService;
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/save")
    public ResponseEntity<?> saveHistory(@RequestBody Map<String, Object> request, Authentication authentication) {
        try {
            // Get authenticated user details
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            
            // Find user from database
            User user = authService.getUserByEmail(username);
            if (user == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.badRequest().body(error);
            }
            
            Long userId = user.getId();
            String tableData = (String) request.get("tableData");
            String action = (String) request.get("action");
            String customerName = request.get("customerName") == null ? "" : String.valueOf(request.get("customerName")).trim();
            
            if (userId == null || tableData == null || action == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Missing required fields: userId, tableData, action");
                return ResponseEntity.badRequest().body(error);
            }
            
            History savedHistory = historyService.saveHistory(userId, tableData, action, customerName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedHistory.getId());
            response.put("userId", savedHistory.getUserId());
            response.put("tableData", savedHistory.getTableData());
            response.put("customerName", savedHistory.getCustomerName());
            response.put("action", savedHistory.getAction().toString());
            response.put("timestamp", savedHistory.getTimestamp());
            response.put("message", "History saved successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to save history: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserHistory(@PathVariable Long userId, Authentication authentication) {
        try {
            // Get authenticated user details and verify authorization
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            
            // Find user from database
            User user = authService.getUserByEmail(username);
            if (user == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.status(403).body(error);
            }
            
            // Users can only see their own history
            if (!user.getId().equals(userId)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized to access this user's history");
                return ResponseEntity.status(403).body(error);
            }
            
            List<History> historyList = historyService.getUserHistory(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("history", historyList);
            response.put("count", historyList.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch user history: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    @GetMapping("/{historyId}")
    public ResponseEntity<?> getHistoryById(@PathVariable Long historyId, Authentication authentication) {
        try {
            // Get authenticated user details
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            
            // Find user from database
            User user = authService.getUserByEmail(username);
            if (user == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.notFound().build();
            }
            
            Optional<History> historyOpt = historyService.getHistoryByIdAndUserId(historyId, user.getId());
            
            if (historyOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "History not found or unauthorized");
                return ResponseEntity.notFound().build();
            }
            
            History history = historyOpt.get();
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", history.getId());
            response.put("userId", history.getUserId());
            response.put("tableData", history.getTableData());
            response.put("customerName", history.getCustomerName());
            response.put("action", history.getAction().toString());
            response.put("timestamp", history.getTimestamp());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch history: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    @DeleteMapping("/{historyId}")
    public ResponseEntity<?> deleteHistory(@PathVariable Long historyId, Authentication authentication) {
        try {
            // Get authenticated user details
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            
            // Find user from database
            User user = authService.getUserByEmail(username);
            if (user == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.notFound().build();
            }
            
            boolean deleted = historyService.deleteHistory(historyId, user.getId());
            
            if (deleted) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "History deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "History not found or unauthorized");
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete history: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PutMapping("/{historyId}")
    public ResponseEntity<?> updateHistory(@PathVariable Long historyId, @RequestBody Map<String, Object> request, Authentication authentication) {
        try {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();

            User user = authService.getUserByEmail(username);
            if (user == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.notFound().build();
            }

            String tableData = (String) request.get("tableData");
            String customerName = request.get("customerName") == null ? "" : String.valueOf(request.get("customerName")).trim();
            if (tableData == null || tableData.isBlank()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Missing required field: tableData");
                return ResponseEntity.badRequest().body(error);
            }

            Optional<History> historyOpt = historyService.updateHistory(historyId, user.getId(), tableData, customerName);
            if (historyOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "History not found or unauthorized");
                return ResponseEntity.notFound().build();
            }

            History history = historyOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("id", history.getId());
            response.put("userId", history.getUserId());
            response.put("tableData", history.getTableData());
            response.put("customerName", history.getCustomerName());
            response.put("action", history.getAction().toString());
            response.put("timestamp", history.getTimestamp());
            response.put("message", "History updated successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update history: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
