package com.example.hrms.services;

import com.example.hrms.config.JwtTokenProvider;
import com.example.hrms.dto.AuthResponse;
import com.example.hrms.dto.LoginRequest;
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
}
