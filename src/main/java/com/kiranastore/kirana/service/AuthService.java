package com.kiranastore.kirana.service;

import com.kiranastore.kirana.entity.User;
import com.kiranastore.kirana.entity.UserRole;
import com.kiranastore.kirana.repository.UserRepository;
import com.kiranastore.kirana.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    
    public User registerUser(String name, String username, String email, String mobile, String password, UserRole role) {
        // Check if user already exists
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        // Check if username already exists
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already taken");
        }

        // Hash the password
        String hashedPassword = passwordEncoder.encode(password);

        // Create new user
        User user = new User(email, hashedPassword, name, mobile, username, role);
        return userRepository.save(user);
    }

    public Map<String, Object> loginUser(String identifier, String password) {
        try {
            // Try to find user by email, username, or mobile
            User user = userRepository.findByEmailAndActive(identifier, true)
                    .orElseGet(() -> userRepository.findByUsernameAndActive(identifier, true)
                    .orElseGet(() -> userRepository.findByMobileAndActive(identifier, true)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"))));

            // Verify password
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new RuntimeException("Invalid credentials");
            }

            // Generate token
            String token = jwtTokenUtil.generateToken(user.getEmail());

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "name", user.getName(),
                "username", user.getUsername(),
                "mobile", user.getMobile(),
                "role", user.getRole().toString()
            ));

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Login failed: " + e.getMessage());
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailAndActive(email, true)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().toString())
                .build();
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmailAndActive(email, true)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public boolean changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmailAndActive(email, true)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false;
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }
}
