package com.example.hrms.services;

import com.example.hrms.config.JwtTokenProvider;
import com.example.hrms.exceptions.TokenRefreshException;
import com.example.hrms.models.Employee;
import com.example.hrms.models.RefreshToken;
import com.example.hrms.repositories.EmployeeRepository;
import com.example.hrms.repositories.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final EmployeeRepository employeeRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     *
     * @param employeeId
     * @return
     */
    @Transactional
    public RefreshToken createRefreshToken(Long employeeId) {
        log.debug("Creating refresh token for employee Id: {}", employeeId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(()->new IllegalArgumentException("Employee not found") );

        //generate token
        String tokenString = UUID.randomUUID().toString();

        Long expirationMs = jwtTokenProvider.getRefreshTokenExpirationInSeconds() * 1000;
        LocalDateTime expiryDate = LocalDateTime.now().plusSeconds(expirationMs / 1000);

        RefreshToken refreshToken = RefreshToken.builder()
                .employee(employee)
                .token(tokenString)
                .expiryDate(expiryDate)
                .revoked(false)
                .build();

        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        log.info("Refresh token created for employee: {}", employee.getUsername());

        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(),
                    "Refresh token has expired. Please login again");
        }

        if (token.getRevoked()) {
            throw new TokenRefreshException(token.getToken(),
                    "Refresh token has been revoked. Please login again");
        }

        return token;
    }




}
