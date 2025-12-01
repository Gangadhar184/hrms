package com.example.hrms.controllers;


import com.example.hrms.dto.*;
import com.example.hrms.services.AuthService;
import com.example.hrms.utils.SecurityUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")

    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login request received for username: {}", loginRequest.getUsername());

        AuthResponse response = authService.login(loginRequest);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")

    public ResponseEntity<AuthResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request received");

        AuthResponse response = authService.refreshToken(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")

    public ResponseEntity<MessageResponse> logout(@Valid @RequestBody LogoutRequest request) {
        log.info("Logout request received");

        MessageResponse response = authService.logout(request.getRefreshToken());

        return ResponseEntity.ok(response);
    }
    @PostMapping("/logout-all")

    public ResponseEntity<MessageResponse> logoutFromAllDevices() {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        log.info("Logout from all devices request for user: {}", username);

        MessageResponse response = authService.logoutFromAllDevices(username);

        return ResponseEntity.ok(response);
    }
    /**
     * Reset password endpoint (for first-time login or password change)
     */
    @PostMapping("/reset-password")

    public ResponseEntity<MessageResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        log.info("Password reset request for user: {}", username);

        MessageResponse response = authService.resetPassword(username, request);

        return ResponseEntity.ok(response);
    }
    /**
     * Check if password reset is required
     */
    @GetMapping("/requires-password-reset")

    public ResponseEntity<Boolean> requiresPasswordReset() {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        boolean required = authService.requiresPasswordReset(username);

        return ResponseEntity.ok(required);
    }
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<MessageResponse> health() {
        return ResponseEntity.ok(new MessageResponse("Auth service is running"));
    }

}
