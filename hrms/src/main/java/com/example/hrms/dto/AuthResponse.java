package com.example.hrms.dto;

import com.example.hrms.models.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private EmployeeInfo employee;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmployeeInfo {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private Role role;
        private Boolean isFirstLogin;
    }
}
