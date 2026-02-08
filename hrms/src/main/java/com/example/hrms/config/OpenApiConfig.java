package com.example.hrms.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .info(new Info()
                        .title("HRMS - Human Resource Management System API")
                        .version("1.0.0")
                        .description("""
                                ## Overview
                                A comprehensive Human Resource Management System API providing endpoints for:
                                - **Authentication** - Login, logout, token refresh, password reset
                                - **Employee Management** - Profile, personal info, contact info, pay info
                                - **Timesheet Management** - Create, update, submit timesheets
                                - **Manager Operations** - Review team timesheets, approve/deny
                                - **Admin Operations** - Employee CRUD, payroll processing
                                - **Payroll** - View payroll history, run payroll
                                
                                ## Authentication
                                This API uses JWT Bearer token authentication. To access protected endpoints:
                                1. Call `/api/auth/login` with valid credentials
                                2. Use the returned `accessToken` in the Authorization header
                                3. Format: `Authorization: Bearer <accessToken>`
                                
                                ## Roles
                                - **EMPLOYEE** - Basic access to own data
                                - **MANAGER** - Employee access + team management
                                - **ADMIN** - Full system access
                                """)
                        .contact(new Contact()
                                .name("HRMS Support")
                                .email("support@hrms.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Development Server")))
                .tags(List.of(
                        new Tag().name("Authentication").description("User authentication and session management"),
                        new Tag().name("Employee").description("Employee profile and personal information"),
                        new Tag().name("Timesheet").description("Employee timesheet management"),
                        new Tag().name("Manager").description("Manager operations for team management"),
                        new Tag().name("Admin - Employees").description("Admin employee management operations"),
                        new Tag().name("Admin - Payroll").description("Admin payroll processing operations"),
                        new Tag().name("Payroll").description("Employee payroll history")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT Bearer token")));
    }
}

