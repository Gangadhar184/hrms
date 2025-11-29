package com.example.hrms.repositories;

import com.example.hrms.models.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {


     // Find refresh token by token string

    Optional<RefreshToken> findByToken(String token);


     // Find all valid tokens for an employee

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.employee.id = :employeeId " +
            "AND rt.revoked = false AND rt.expiryDate > :now")
    List<RefreshToken> findValidTokensByEmployeeId(
            @Param("employeeId") Long employeeId,
            @Param("now") LocalDateTime now);


     // Find all tokens for an employee

    List<RefreshToken> findByEmployeeId(Long employeeId);


     // Revoke all tokens for an employee (logout from all devices)

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.employee.id = :employeeId")
    void revokeAllTokensByEmployeeId(@Param("employeeId") Long employeeId);


     // Delete expired tokens

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);


     //  Delete revoked tokens
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.revoked = true")
    void deleteRevokedTokens();


     // Check if token exists and is valid

    @Query("SELECT CASE WHEN COUNT(rt) > 0 THEN true ELSE false END FROM RefreshToken rt " +
            "WHERE rt.token = :token AND rt.revoked = false AND rt.expiryDate > :now")
    boolean existsByTokenAndValid(@Param("token") String token, @Param("now") LocalDateTime now);


     // Count valid tokens for employee

    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.employee.id = :employeeId " +
            "AND rt.revoked = false AND rt.expiryDate > :now")
    long countValidTokensByEmployeeId(@Param("employeeId") Long employeeId,
                                      @Param("now") LocalDateTime now);

}
