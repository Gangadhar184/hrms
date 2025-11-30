package com.example.hrms.services;

import com.example.hrms.config.JwtTokenProvider;
import com.example.hrms.dto.*;
import com.example.hrms.exceptions.BadRequestException;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.mappers.AuthMapper;
import com.example.hrms.models.Employee;
import com.example.hrms.models.RefreshToken;
import com.example.hrms.repositories.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final EmployeeRepository employeeRepository;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;

    /**
     * Authenticates a user with the provided login credentials, verifies account status,
     * and generates JWT access and refresh tokens.
     *
     * <p>This method performs the following steps:</p>
     * <ul>
     *     <li>Authenticates the user using Spring Security</li>
     *     <li>Loads the corresponding employee record</li>
     *     <li>Validates that the employee account is active</li>
     *     <li>Generates an access token and refresh token</li>
     *     <li>Returns an authentication response containing tokens and employee info</li>
     * </ul>
     *
     * @param loginRequest the login request containing username and password
     * @return an {@link AuthResponse} containing JWT tokens and user details
     *
     * @throws ResourceNotFoundException if the employee does not exist
     * @throws BadRequestException if the employee account is inactive
     */
    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        log.info("Login attempt for username: {}", loginRequest.getUsername());

        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Get employee details
        Employee employee = employeeRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with username: " + loginRequest.getUsername()));

        // Check if employee is active
        if (!employee.getIsActive()) {
            throw new BadRequestException("Account is inactive. Please contact administrator.");
        }

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(employee.getId());

        log.info("User logged in successfully: {}", employee.getUsername());

        return authMapper.toAuthResponse(
                accessToken,
                refreshToken.getToken(),
                jwtTokenProvider.getAccessTokenExpirationInSeconds(),
                employee
        );
    }

    /**
     * Refreshes the access token using a valid refresh token.
     *
     * <p>Workflow:</p>
     * <ol>
     *     <li>Validate that the refresh token exists</li>
     *     <li>Verify expiration of refresh token</li>
     *     <li>Generate new access token for the user</li>
     *     <li>Return updated token response</li>
     * </ol>
     *
     * @param request wrapper containing the refresh token
     * @return {@link AuthResponse} with a new access token and existing refresh token
     *
     * @throws BadRequestException if token does not exist or is invalid
     */
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.debug("Refresh token request received");

        String requestRefreshToken = request.getRefreshToken();

        RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken)
                .orElseThrow(() -> new BadRequestException("Refresh token not found"));

        // Verify token is valid
        refreshToken = refreshTokenService.verifyExpiration(refreshToken);

        Employee employee = refreshToken.getEmployee();

        // Generate new access token
        String roles = "ROLE_" + employee.getRole().name();
        String newAccessToken = jwtTokenProvider.generateAccessTokenFromUsername(
                employee.getUsername(), roles);

        // Optionally rotate refresh token for better security
        // RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(requestRefreshToken);
        // String newRefreshTokenString = newRefreshToken.getToken();

        log.info("Token refreshed for user: {}", employee.getUsername());

        return authMapper.toRefreshResponse(
                newAccessToken,
                requestRefreshToken, // or newRefreshTokenString if rotating
                jwtTokenProvider.getAccessTokenExpirationInSeconds()
        );
    }
    /**
     * Logs out the user by revoking a specific refresh token.
     *
     * @param refreshToken the refresh token to revoke
     * @return a {@link MessageResponse} confirming logout
     */
    @Transactional
    public MessageResponse logout(String refreshToken) {
        log.debug("Logout request received");

        refreshTokenService.revokeToken(refreshToken);

        return new MessageResponse("Logged out successfully");
    }

    /**
     * Logs out a user from all devices by revoking all refresh tokens
     * associated with their account.
     *
     * @param username the username of the employee to logout
     * @return a {@link MessageResponse} indicating all sessions were terminated
     *
     * @throws ResourceNotFoundException if employee cannot be found
     */
    @Transactional
    public MessageResponse logoutFromAllDevices(String username) {
        log.debug("Logout from all devices for user: {}", username);

        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        refreshTokenService.revokeAllTokensForEmployee(employee.getId());

        return new MessageResponse("Logged out from all devices successfully");
    }

    /**
     * Resets the user's password after verifying their current password,
     * confirming the new password, and ensuring it is different from the old one.
     *
     * <p>This method is also used for first-time login password setup.</p>
     *
     * @param username the username of the employee
     * @param request contains current password, new password, and confirmation
     * @return a {@link MessageResponse} confirming successful reset
     *
     * @throws ResourceNotFoundException if the user cannot be found
     * @throws BadRequestException if:
     *      <ul>
     *          <li>Current password is invalid</li>
     *          <li>New password and confirm password do not match</li>
     *          <li>New password is the same as the old password</li>
     *      </ul>
     */
    @Transactional
    public MessageResponse resetPassword(String username, ResetPasswordRequest request) {
        log.info("Password reset request for user: {}", username);

        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), employee.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Verify new password and confirm password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }

        // Verify new password is different from current
        if (passwordEncoder.matches(request.getNewPassword(), employee.getPassword())) {
            throw new BadRequestException("New password must be different from current password");
        }

        // Update password
        employee.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // Mark first login as complete
        if (employee.getIsFirstLogin()) {
            employee.setIsFirstLogin(false);
        }

        employeeRepository.save(employee);

        log.info("Password reset successfully for user: {}", username);

        return new MessageResponse("Password reset successfully");
    }

    /**
     * Indicates whether the user is required to reset their password,
     * typically during first-time login.
     *
     * @param username the username of the employee
     * @return {@code true} if user must reset password, otherwise {@code false}
     *
     * @throws ResourceNotFoundException if the employee does not exist
     */
    @Transactional(readOnly = true)
    public boolean requiresPasswordReset(String username) {
        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        return employee.getIsFirstLogin();
    }


}
