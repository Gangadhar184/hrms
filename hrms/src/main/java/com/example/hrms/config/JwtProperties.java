package com.example.hrms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;

    private Long accessTokenExpiration;

    private Long refreshTokenExpiration;

    private String issuer;

    private String tokenType;

    private String headerName;

    private String tokenPrefix;
}
