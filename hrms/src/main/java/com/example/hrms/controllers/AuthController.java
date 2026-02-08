package com.example.hrms.controllers;

import com.example.hrms.dto.*;
import com.example.hrms.services.AuthService;
import com.example.hrms.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@Tag(name = "Authentication", description = "User authentication and session management APIs")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "User login", description = "Authenticate user with username and password. Returns JWT tokens.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login request received for username: {}", loginRequest.getUsername());
        AuthResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Refresh access token", description = "Get a new access token using a valid refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request received");
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Logout", description = "Invalidate the current refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@Valid @RequestBody LogoutRequest request) {
        log.info("Logout request received");
        MessageResponse response = authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Logout from all devices", description = "Invalidate all refresh tokens for the current user")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logged out from all devices"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PostMapping("/logout-all")
    public ResponseEntity<MessageResponse> logoutFromAllDevices() {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));
        log.info("Logout from all devices request for user: {}", username);
        MessageResponse response = authService.logoutFromAllDevices(username);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Reset password", description = "Reset password for first-time login or password change")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successful"),
            @ApiResponse(responseCode = "400", description = "Invalid password format"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));
        log.info("Password reset request for user: {}", username);
        MessageResponse response = authService.resetPassword(username, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Check password reset requirement", description = "Check if the current user needs to reset their password")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns true if password reset is required"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/requires-password-reset")
    public ResponseEntity<Boolean> requiresPasswordReset() {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));
        boolean required = authService.requiresPasswordReset(username);
        return ResponseEntity.ok(required);
    }

    @Operation(summary = "Health check", description = "Check if the auth service is running")
    @ApiResponse(responseCode = "200", description = "Service is healthy")
    @GetMapping("/health")
    public ResponseEntity<MessageResponse> health() {
        return ResponseEntity.ok(new MessageResponse("Auth service is running"));
    }
}
