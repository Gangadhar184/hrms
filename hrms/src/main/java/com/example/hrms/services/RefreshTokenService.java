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
    @Transactional
    public void revokeToken(String token) {
        log.debug("Revoking refresh token");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenRefreshException(token, "Refresh token not found"));

        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        log.info("Refresh token revoked for employee: {}",
                refreshToken.getEmployee().getUsername());
    }
    @Transactional
    public void revokeAllTokensForEmployee(Long employeeId) {
        log.debug("Revoking all tokens for employee ID: {}", employeeId);

        refreshTokenRepository.revokeAllTokensByEmployeeId(employeeId);

        log.info("All refresh tokens revoked for employee ID: {}", employeeId);
    }
    @Transactional
    public int deleteExpiredTokens() {
        log.debug("Deleting expired refresh tokens");

        int countBefore = (int) refreshTokenRepository.count();
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        int countAfter = (int) refreshTokenRepository.count();

        int deleted = countBefore - countAfter;
        log.info("Deleted {} expired refresh tokens", deleted);

        return deleted;
    }
    @Transactional
    public int deleteRevokedTokens() {
        log.debug("Deleting revoked refresh tokens");

        int countBefore = (int) refreshTokenRepository.count();
        refreshTokenRepository.deleteRevokedTokens();
        int countAfter = (int) refreshTokenRepository.count();

        int deleted = countBefore - countAfter;
        log.info("Deleted {} revoked refresh tokens", deleted);

        return deleted;
    }
    @Transactional(readOnly = true)
    public long countValidTokensForEmployee(Long employeeId) {
        return refreshTokenRepository.countValidTokensByEmployeeId(
                employeeId, LocalDateTime.now());
    }
    @Transactional
    public RefreshToken rotateRefreshToken(String oldToken) {
        log.debug("Rotating refresh token");

        RefreshToken oldRefreshToken = refreshTokenRepository.findByToken(oldToken)
                .orElseThrow(() -> new TokenRefreshException(oldToken, "Refresh token not found"));

        // Verify old token is valid
        verifyExpiration(oldRefreshToken);

        // Revoke old token
        oldRefreshToken.revoke();
        refreshTokenRepository.save(oldRefreshToken);

        //Create new token
        RefreshToken newToken = createRefreshToken(oldRefreshToken.getEmployee().getId());

        log.info("Refresh token rotated for employee: {}",
                oldRefreshToken.getEmployee().getUsername());

        return newToken;
    }
}
