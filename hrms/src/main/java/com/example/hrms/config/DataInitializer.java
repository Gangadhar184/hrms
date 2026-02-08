package com.example.hrms.config;

import com.example.hrms.models.*;
import com.example.hrms.repositories.ContactInfoRepository;
import com.example.hrms.repositories.EmployeeRepository;
import com.example.hrms.repositories.PayInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final EmployeeRepository employeeRepository;
    private final ContactInfoRepository contactInfoRepository;
    private final PayInfoRepository payInfoRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initDatabase() {
        return args -> {
            // Check if admin already exists
            if (employeeRepository.findByUsername("admin").isPresent()) {
                log.info("Database already initialized. Skipping data initialization.");
                return;
            }

            log.info("Initializing database with default users...");

            // create admin
            Employee admin = Employee.builder()
                    .employeeId("EMP-20240101-0001")
                    .username("admin")
                    .email("admin@company.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .firstName("System")
                    .lastName("Administrator")
                    .dateOfBirth(LocalDate.of(1985, 1, 1))
                    .hireDate(LocalDate.of(2024, 1, 1))
                    .role(Role.ADMIN)
                    .isFirstLogin(false)
                    .isActive(true)
                    .build();
            admin = employeeRepository.save(admin);

            // Admin contact info
            ContactInfo adminContact = ContactInfo.builder()
                    .employee(admin)
                    .phoneNumber("+1-555-0001")
                    .mobileNumber("+1-555-0001")
                    .emergencyContactName("Emergency Contact")
                    .emergencyContactPhone("+1-555-0002")
                    .addressLine1("123 Admin Street")
                    .city("New York")
                    .state("NY")
                    .postalCode("10001")
                    .country("USA")
                    .build();
            contactInfoRepository.save(adminContact);

            // Admin pay info
            PayInfo adminPay = PayInfo.builder()
                    .employee(admin)
                    .salary(new BigDecimal("120000.00"))
                    .hourlyRate(new BigDecimal("57.69"))
                    .payFrequency(PayFrequency.MONTHLY)
                    .paymentMethod(PaymentMethod.BANK_TRANSFER)
                    .bankName("Chase Bank")
                    .accountNumber("1111222233334444")
                    .routingNumber("123456789")
                    .taxId("123-45-6789")
                    .build();
            payInfoRepository.save(adminPay);

            log.info(" ADMIN created:");
            log.info("   Username: admin");
            log.info("   Password: Admin@123");
            log.info("   Role: ADMIN");

            // create manager
            Employee manager = Employee.builder()
                    .employeeId("EMP-20240101-0002")
                    .username("manager")
                    .email("manager@company.com")
                    .password(passwordEncoder.encode("Manager@123"))
                    .firstName("John")
                    .lastName("Manager")
                    .dateOfBirth(LocalDate.of(1988, 3, 15))
                    .hireDate(LocalDate.of(2024, 2, 1))
                    .role(Role.MANAGER)
                    .manager(admin) // Reports to admin
                    .isFirstLogin(false)
                    .isActive(true)
                    .build();
            manager = employeeRepository.save(manager);

            // Manager contact info
            ContactInfo managerContact = ContactInfo.builder()
                    .employee(manager)
                    .phoneNumber("+1-555-1001")
                    .mobileNumber("+1-555-1001")
                    .emergencyContactName("Jane Manager")
                    .emergencyContactPhone("+1-555-1002")
                    .addressLine1("456 Manager Ave")
                    .city("Los Angeles")
                    .state("CA")
                    .postalCode("90001")
                    .country("USA")
                    .build();
            contactInfoRepository.save(managerContact);

            // Manager pay info
            PayInfo managerPay = PayInfo.builder()
                    .employee(manager)
                    .salary(new BigDecimal("90000.00"))
                    .hourlyRate(new BigDecimal("43.27"))
                    .payFrequency(PayFrequency.MONTHLY)
                    .paymentMethod(PaymentMethod.BANK_TRANSFER)
                    .bankName("Bank of America")
                    .accountNumber("2222333344445555")
                    .routingNumber("987654321")
                    .taxId("234-56-7890")
                    .build();
            payInfoRepository.save(managerPay);

            log.info("   MANAGER created:");
            log.info("   Username: manager");
            log.info("   Password: Manager@123");
            log.info("   Role: MANAGER");

            // create employee
            Employee employee = Employee.builder()
                    .employeeId("EMP-20240101-0003")
                    .username("employee")
                    .email("employee@company.com")
                    .password(passwordEncoder.encode("Employee@123"))
                    .firstName("Jane")
                    .lastName("Employee")
                    .dateOfBirth(LocalDate.of(1992, 7, 20))
                    .hireDate(LocalDate.of(2024, 3, 1))
                    .role(Role.EMPLOYEE)
                    .manager(manager) // Reports to manager
                    .isFirstLogin(false)
                    .isActive(true)
                    .build();
            employee = employeeRepository.save(employee);

            // Employee contact info
            ContactInfo employeeContact = ContactInfo.builder()
                    .employee(employee)
                    .phoneNumber("+1-555-2001")
                    .mobileNumber("+1-555-2001")
                    .emergencyContactName("John Employee")
                    .emergencyContactPhone("+1-555-2002")
                    .addressLine1("789 Employee Blvd")
                    .city("Chicago")
                    .state("IL")
                    .postalCode("60601")
                    .country("USA")
                    .build();
            contactInfoRepository.save(employeeContact);

            // Employee pay info
            PayInfo employeePay = PayInfo.builder()
                    .employee(employee)
                    .salary(new BigDecimal("65000.00"))
                    .hourlyRate(new BigDecimal("31.25"))
                    .payFrequency(PayFrequency.MONTHLY)
                    .paymentMethod(PaymentMethod.BANK_TRANSFER)
                    .bankName("Wells Fargo")
                    .accountNumber("3333444455556666")
                    .routingNumber("111222333")
                    .taxId("345-67-8901")
                    .build();
            payInfoRepository.save(employeePay);

            log.info("   EMPLOYEE created:");
            log.info("   Username: employee");
            log.info("   Password: Employee@123");
            log.info("   Role: EMPLOYEE");

            log.info("========================================");
            log.info("Database initialization completed!");
            log.info("========================================");
            log.info("");
            log.info("TEST CREDENTIALS:");
            log.info("========================================");
            log.info("ADMIN:");
            log.info("  Username: admin");
            log.info("  Password: Admin@123");
            log.info("");
            log.info("MANAGER:");
            log.info("  Username: manager");
            log.info("  Password: Manager@123");
            log.info("");
            log.info("EMPLOYEE:");
            log.info("  Username: employee");
            log.info("  Password: Employee@123");
            log.info("========================================");
        };
    }
}