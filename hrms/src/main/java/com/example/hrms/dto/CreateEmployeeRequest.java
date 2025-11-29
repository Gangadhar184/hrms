package com.example.hrms.dto;

import com.example.hrms.models.PayFrequency;
import com.example.hrms.models.PaymentMethod;
import com.example.hrms.models.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEmployeeRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    private String lastName;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotNull(message = "Hire date is required")
    @PastOrPresent(message = "Hire date cannot be in the future")
    private LocalDate hireDate;

    @NotNull(message = "Role is required")
    private Role role;

    private Long managerId;
    @Valid
    private PayInfoRequest payInfo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PayInfoRequest {

        @NotNull(message = "Salary is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Salary must be greater than 0")
        private BigDecimal salary;

        @DecimalMin(value = "0.0", inclusive = false, message = "Hourly rate must be greater than 0")
        private BigDecimal hourlyRate;

        private PayFrequency payFrequency;

        private PaymentMethod paymentMethod;

        private String bankName;

        private String accountNumber;

        private String routingNumber;

        private String taxId;
    }
}