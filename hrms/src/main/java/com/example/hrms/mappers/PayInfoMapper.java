package com.example.hrms.mappers;

import com.example.hrms.dto.CreateEmployeeRequest;
import com.example.hrms.dto.PayInfoResponse;
import com.example.hrms.dto.UpdatePayInfoRequest;
import com.example.hrms.models.PayInfo;
import org.springframework.stereotype.Component;

@Component
public class PayInfoMapper {

    /**
     * Convert PayInfo entity to PayInfoResponse DTO
     */
    public PayInfoResponse toResponse(PayInfo payInfo) {
        if (payInfo == null) {
            return null;
        }

        return PayInfoResponse.builder()
                .id(payInfo.getId())
                .salary(payInfo.getSalary())
                .hourlyRate(payInfo.getHourlyRate())
                .payFrequency(payInfo.getPayFrequency())
                .paymentMethod(payInfo.getPaymentMethod())
                .bankName(payInfo.getBankName())
                .maskedAccountNumber(maskAccountNumber(payInfo.getAccountNumber()))
                .updatedAt(payInfo.getUpdatedAt())
                .build();
    }

    /**
     * Mask account number - show only last 4 digits
     */
    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return null;
        }

        int length = accountNumber.length();
        String lastFour = accountNumber.substring(length - 4);
        String masked = "*".repeat(length - 4) + lastFour;

        return masked;
    }

    /**
     * Convert CreateEmployeeRequest.PayInfoRequest to PayInfo entity
     */
    public PayInfo toEntity(CreateEmployeeRequest.PayInfoRequest request) {
        if (request == null) {
            return null;
        }

        return PayInfo.builder()
                .salary(request.getSalary())
                .hourlyRate(request.getHourlyRate())
                .payFrequency(request.getPayFrequency())
                .paymentMethod(request.getPaymentMethod())
                .bankName(request.getBankName())
                .accountNumber(request.getAccountNumber())
                .routingNumber(request.getRoutingNumber())
                .taxId(request.getTaxId())
                .build();
    }

    /**
     * Convert UpdatePayInfoRequest to PayInfo entity
     */
    public PayInfo toEntity(UpdatePayInfoRequest request) {
        if (request == null) {
            return null;
        }

        return PayInfo.builder()
                .salary(request.getSalary())
                .hourlyRate(request.getHourlyRate())
                .payFrequency(request.getPayFrequency())
                .paymentMethod(request.getPaymentMethod())
                .bankName(request.getBankName())
                .accountNumber(request.getAccountNumber())
                .routingNumber(request.getRoutingNumber())
                .taxId(request.getTaxId())
                .build();
    }

    /**
     * Update PayInfo entity from UpdatePayInfoRequest
     */
    public void updateEntityFromRequest(PayInfo payInfo, UpdatePayInfoRequest request) {
        if (payInfo == null || request == null) {
            return;
        }

        payInfo.setSalary(request.getSalary());
        payInfo.setHourlyRate(request.getHourlyRate());
        payInfo.setPayFrequency(request.getPayFrequency());
        payInfo.setPaymentMethod(request.getPaymentMethod());
        payInfo.setBankName(request.getBankName());
        payInfo.setAccountNumber(request.getAccountNumber());
        payInfo.setRoutingNumber(request.getRoutingNumber());
        payInfo.setTaxId(request.getTaxId());
    }
}