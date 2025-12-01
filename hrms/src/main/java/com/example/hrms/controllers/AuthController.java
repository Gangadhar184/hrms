package com.example.hrms.controllers;


import com.example.hrms.dto.*;
import com.example.hrms.services.AuthService;
import com.example.hrms.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT tokens")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login request received for username: {}", loginRequest.getUsername());

        AuthResponse response = authService.login(loginRequest);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Get new access token using refresh token")
    public ResponseEntity<AuthResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request received");

        AuthResponse response = authService.refreshToken(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Revoke refresh token and logout user")
    public ResponseEntity<MessageResponse> logout(@Valid @RequestBody LogoutRequest request) {
        log.info("Logout request received");

        MessageResponse response = authService.logout(request.getRefreshToken());

        return ResponseEntity.ok(response);
    }
    @PostMapping("/logout-all")
    @Operation(summary = "Logout from all devices",
            description = "Revoke all refresh tokens for current user")
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
    @Operation(summary = "Reset password",
            description = "Change password (required on first login)")
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
    @Operation(summary = "Check password reset requirement",
            description = "Check if user needs to reset password on first login")
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
    @Operation(summary = "Health check", description = "Check if auth service is available")
    public ResponseEntity<MessageResponse> health() {
        return ResponseEntity.ok(new MessageResponse("Auth service is running"));
    }

}
