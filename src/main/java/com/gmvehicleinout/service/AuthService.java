package com.gmvehicleinout.service;

import com.gmvehicleinout.dto.CreateUserRequest;
import com.gmvehicleinout.dto.LoginRequest;
import com.gmvehicleinout.entity.ApiResponse;
import com.gmvehicleinout.entity.User;
import com.gmvehicleinout.repository.UserRepository;
import com.gmvehicleinout.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
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

    // ============================
    //           LOGIN
    // ============================
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(LoginRequest request) {

        User user = userRepository.findByMobile(request.getMobile()).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>("Invalid mobile number", false));
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>("Invalid password", false));
        }

        // Generate tokens
        String accessToken = JwtUtil.generateAccessToken(user.getMobile());
        String refreshToken = JwtUtil.generateRefreshToken(user.getMobile());

        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", accessToken);
        data.put("refreshToken", refreshToken);
        data.put("fullName", user.getFullName());
        data.put("mobile", user.getMobile());

        return ResponseEntity.ok(new ApiResponse<>("Login successful", true, data));
    }


    // ============================
    //      REFRESH TOKEN
    // ============================
    public ResponseEntity<ApiResponse<Map<String, String>>> refreshAccessToken(String refreshToken) {

        try {
            String mobile = JwtUtil.extractMobile(refreshToken);

            String newAccessToken = JwtUtil.generateAccessToken(mobile);

            Map<String, String> data = new HashMap<>();
            data.put("accessToken", newAccessToken);

            return ResponseEntity.ok(
                    new ApiResponse<>("Access token refreshed successfully", true, data)
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>("Invalid or expired refresh token", false));
        }
    }


    // ============================
    //       CREATE USER
    // ============================
    public ResponseEntity<ApiResponse<?>> createUser(CreateUserRequest request) {

        if (userRepository.findByMobile(request.getMobile()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>("User already exists with this mobile number", false));
        }

        User user = new User();
        user.setMobile(request.getMobile());
        user.setFullName(request.getFullName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("User created successfully", true));
    }
}
