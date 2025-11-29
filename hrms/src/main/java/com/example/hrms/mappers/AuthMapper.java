package com.example.hrms.mappers;

import com.example.hrms.dto.AuthResponse;
import com.example.hrms.models.Employee;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    /*
     * Convert Employee to EmployeeInfo in AuthResponse
     */
    public AuthResponse.EmployeeInfo toEmployeeInfo(Employee employee) {
        if (employee == null) {
            return null;
        }

        return AuthResponse.EmployeeInfo.builder()
                .id(employee.getId())
                .username(employee.getUsername())
                .email(employee.getEmail())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .role(employee.getRole())
                .isFirstLogin(employee.getIsFirstLogin())
                .build();
    }

    /**
     * Create complete AuthResponse with tokens and employee info
     */
    public AuthResponse toAuthResponse(String accessToken,
                                       String refreshToken,
                                       Long expiresIn,
                                       Employee employee) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .employee(toEmployeeInfo(employee))
                .build();
    }

    /**
     * Create AuthResponse for token refresh
     */
    public AuthResponse toRefreshResponse(String accessToken,
                                          String refreshToken,
                                          Long expiresIn) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .build();
    }
}