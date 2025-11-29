package com.example.hrms.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {



    private String secret = "your-very-long-and-secure-secret-key-that-should-be-at-least-256-bits-long";


    private Long accessTokenExpiration = 1800000L; // 30 minutes


    private Long refreshTokenExpiration = 604800000L; // 7 days


    private String issuer = "employee-management-system";


    private String tokenType = "Bearer";


    private String headerName = "Authorization";


    private String tokenPrefix = "Bearer ";

}
