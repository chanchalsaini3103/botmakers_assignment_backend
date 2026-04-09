package com.example.botmakersassignment.service;

import com.example.botmakersassignment.entity.User;
import com.example.botmakersassignment.repo.UserRepository;
import com.example.botmakersassignment.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public Map<String, Object> registerUser(String name, String email, String password, String role) {
        Map<String, Object> response = new HashMap<>();

        if (userRepository.findByEmail(email).isPresent()) {
            response.put("error", "Email already exists");
            return response;
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        if (role != null && role.equalsIgnoreCase("ADMIN")) {
            user.setRole(User.Role.ADMIN);
        } else {
            user.setRole(User.Role.USER);
        }

        userRepository.save(user);
        String token = jwtUtil.generateToken(user);

        response.put("message", "User registered successfully");
        response.put("token", token);
        response.put("email", user.getEmail());
        response.put("role", user.getRole().name());
        response.put("name", user.getName());

        return response;
    }

    public Map<String, Object> loginUser(String email, String password) {
        Map<String, Object> response = new HashMap<>();

        User user = userRepository.findByEmail(email)
                .orElse(null);

        if (user == null) {
            response.put("error", "User not found");
            return response;
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            response.put("error", "Invalid credentials");
            return response;
        }

        String token = jwtUtil.generateToken(user);
        response.put("token", token);
        response.put("email", user.getEmail());
        response.put("role", user.getRole().name());
        response.put("name", user.getName());

        return response;
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}