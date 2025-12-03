package com.gmvehicleinout.controller;

import com.gmvehicleinout.dto.CreateUserRequest;
import com.gmvehicleinout.dto.LoginRequest;
import com.gmvehicleinout.entity.ApiResponse;
import com.gmvehicleinout.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private AuthService authService;

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    // REFRESH TOKEN
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<Map<String, String>>> refreshToken(
            @RequestHeader("Refresh-Token") String refreshToken
    ) {
        return authService.refreshAccessToken(refreshToken);
    }

    // CREATE USER
    @PostMapping("/create-user")
    public ResponseEntity<ApiResponse<?>> createUser(@RequestBody CreateUserRequest request) {
        return authService.createUser(request);
    }
}
