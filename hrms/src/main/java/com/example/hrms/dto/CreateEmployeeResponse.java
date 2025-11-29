package com.example.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEmployeeResponse {

    private String message;
    private String employeeId;
    private String temporaryPassword;
    private Long id;
}