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
     * Creates and stores a new refresh token for the specified employee.
     *
     * <p>Steps:</p>
     * <ol>
     *     <li>Verify employee exists</li>
     *     <li>Generate a random UUID token string</li>
     *     <li>Calculate expiration date using configuration</li>
     *     <li>Persist token in database</li>
     * </ol>
     *
     * @param employeeId the ID of the employee for whom the token will be created
     * @return the newly created {@link RefreshToken}
     *
     * @throws IllegalArgumentException if employee does not exist
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

    /**
     * Retrieves a refresh token entity by its token string.
     *
     * @param token the token value to search for
     * @return an {@link Optional} containing the refresh token if found
     */
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Validates a refresh token by checking:
     * <ul>
     *     <li>If the token has expired</li>
     *     <li>If the token has been revoked</li>
     * </ul>
     *
     * <p>Expired tokens are automatically deleted.</p>
     *
     * @param token the refresh token to validate
     * @return the valid refresh token
     *
     * @throws TokenRefreshException if token is expired or revoked
     */
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
    /**
     * Revokes a refresh token, marking it as invalid for future use.
     *
     * @param token the token string to revoke
     * @throws TokenRefreshException if token does not exist
     */
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
    /**
     * Revokes all refresh tokens associated with a specific employee.
     *
     * @param employeeId the employee ID whose tokens will be revoked
     */
    @Transactional
    public void revokeAllTokensForEmployee(Long employeeId) {
        log.debug("Revoking all tokens for employee ID: {}", employeeId);

        refreshTokenRepository.revokeAllTokensByEmployeeId(employeeId);

        log.info("All refresh tokens revoked for employee ID: {}", employeeId);
    }
    /**
     * Deletes all refresh tokens that are expired as of the current timestamp.
     *
     * @return the number of tokens deleted
     */
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
    /**
     * Deletes all refresh tokens that were previously revoked.
     *
     * @return the number of revoked tokens removed
     */
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
    /**
     * Counts the number of valid (non-expired and non-revoked) refresh tokens
     * belonging to a specific employee.
     *
     * @param employeeId the employee ID
     * @return the count of valid tokens
     */
    @Transactional(readOnly = true)
    public long countValidTokensForEmployee(Long employeeId) {
        return refreshTokenRepository.countValidTokensByEmployeeId(
                employeeId, LocalDateTime.now());
    }
    /**
     * Rotates an existing refresh token by:
     * <ol>
     *     <li>Validating the old token</li>
     *     <li>Revoking the old token</li>
     *     <li>Creating a new refresh token</li>
     * </ol>
     *
     * <p>Used for implementing refresh token rotation for improved security.</p>
     *
     * @param oldToken the token to rotate
     * @return the newly generated refresh token
     *
     * @throws TokenRefreshException if the old token is invalid
     */
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
