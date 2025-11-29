package com.example.hrms.mappers;

import com.example.hrms.dto.CreateEmployeeRequest;
import com.example.hrms.dto.EmployeeListResponse;
import com.example.hrms.dto.EmployeeResponse;
import com.example.hrms.dto.UpdateEmployeePersonalInfoRequest;
import com.example.hrms.models.Employee;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EmployeeMapper {

    /**
     * Convert Employee entity to EmployeeResponse DTO
     */
    public EmployeeResponse toResponse(Employee employee) {
        if (employee == null) {
            return null;
        }

        return EmployeeResponse.builder()
                .id(employee.getId())
                .employeeId(employee.getEmployeeId())
                .username(employee.getUsername())
                .email(employee.getEmail())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .dateOfBirth(employee.getDateOfBirth())
                .hireDate(employee.getHireDate())
                .role(employee.getRole())
                .manager(toManagerInfo(employee.getManager()))
                .isFirstLogin(employee.getIsFirstLogin())
                .isActive(employee.getIsActive())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();
    }

    /**
     * Convert Employee to ManagerInfo
     */
    public EmployeeResponse.ManagerInfo toManagerInfo(Employee manager) {
        if (manager == null) {
            return null;
        }

        return EmployeeResponse.ManagerInfo.builder()
                .id(manager.getId())
                .employeeId(manager.getEmployeeId())
                .name(manager.getFullName())
                .email(manager.getEmail())
                .build();
    }

    /**
     * Convert Employee entity to EmployeeListResponse DTO
     */
    public EmployeeListResponse toListResponse(Employee employee) {
        if (employee == null) {
            return null;
        }

        return EmployeeListResponse.builder()
                .id(employee.getId())
                .employeeId(employee.getEmployeeId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .role(employee.getRole())
                .hireDate(employee.getHireDate())
                .isActive(employee.getIsActive())
                .managerName(employee.getManager() != null ?
                        employee.getManager().getFullName() : null)
                .build();
    }

    /**
     * Convert list of Employee entities to list of EmployeeResponse DTOs
     */
    public List<EmployeeResponse> toResponseList(List<Employee> employees) {
        if (employees == null) {
            return null;
        }

        return employees.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert list of Employee entities to list of EmployeeListResponse DTOs
     */
    public List<EmployeeListResponse> toListResponseList(List<Employee> employees) {
        if (employees == null) {
            return null;
        }

        return employees.stream()
                .map(this::toListResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert CreateEmployeeRequest to Employee entity
     */
    public Employee toEntity(CreateEmployeeRequest request) {
        if (request == null) {
            return null;
        }

        return Employee.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .hireDate(request.getHireDate())
                .role(request.getRole())
                .isFirstLogin(true)
                .isActive(true)
                .build();
    }

    /**
     * Update Employee entity from UpdateEmployeePersonalInfoRequest
     */
    public void updateEntityFromRequest(Employee employee,
                                        UpdateEmployeePersonalInfoRequest request) {
        if (employee == null || request == null) {
            return;
        }

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setDateOfBirth(request.getDateOfBirth());
        employee.setEmail(request.getEmail());
        employee.setIsActive(request.getIsActive());
    }

    /**
     * Convert Page of Employee to List of EmployeeListResponse
     */
    public List<EmployeeListResponse> toListResponseFromPage(Page<Employee> page) {
        if (page == null) {
            return null;
        }

        return page.getContent().stream()
                .map(this::toListResponse)
                .collect(Collectors.toList());
    }
}