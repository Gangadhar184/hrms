package com.example.hrms.dto;

import com.example.hrms.models.PayFrequency;
import com.example.hrms.models.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayInfoResponse {

    private Long id;
    private BigDecimal salary;
    private BigDecimal hourlyRate;
    private PayFrequency payFrequency;
    private PaymentMethod paymentMethod;
    private String bankName;
    private String maskedAccountNumber; // Last 4 digits only
    private LocalDate lastPayDate;
    private LocalDate nextPayDate;
    private LocalDateTime updatedAt;
}
