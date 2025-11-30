package com.example.hrms.services;

import com.example.hrms.dto.*;
import com.example.hrms.exceptions.BadRequestException;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.mappers.ContactInfoMapper;
import com.example.hrms.mappers.EmployeeMapper;
import com.example.hrms.mappers.PageMapper;
import com.example.hrms.mappers.PayInfoMapper;
import com.example.hrms.models.ContactInfo;
import com.example.hrms.models.Employee;
import com.example.hrms.models.PayInfo;
import com.example.hrms.models.Role;
import com.example.hrms.repositories.ContactInfoRepository;
import com.example.hrms.repositories.EmployeeRepository;
import com.example.hrms.repositories.PayInfoRepository;
import com.example.hrms.utils.EmployeeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final ContactInfoRepository contactInfoRepository;
    private final PayInfoRepository payInfoRepository;
    private final EmployeeMapper employeeMapper;
    private final ContactInfoMapper contactInfoMapper;
    private final PayInfoMapper payInfoMapper;
    private final PageMapper pageMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Retrieves an employee by their username.
     *
     * @param username the username of the employee
     * @return a DTO containing employee details
     * @throws ResourceNotFoundException if no employee exists with the given username
     */
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeByUsername(String username) {
        log.debug("Fetching employee by username: {}", username);

        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        return employeeMapper.toResponse(employee);
    }

    /**
     * Retrieves an employee by their unique ID.
     *
     * @param id the employee's ID
     * @return a DTO containing the employee details
     * @throws ResourceNotFoundException if no employee exists with the given ID
     */
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        log.debug("Fetching employee by ID: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));

        return employeeMapper.toResponse(employee);
    }

    /**
     * Retrieves a paginated list of all employees.
     *
     * @param pageable pagination and sorting parameters
     * @return a PageResponse containing a paginated list of employees
     */
    @Transactional(readOnly = true)
    public PageResponse<EmployeeListResponse> getAllEmployees(Pageable pageable) {
        log.debug("Fetching all employees with pagination");

        Page<Employee> employeePage = employeeRepository.findAll(pageable);
        return pageMapper.toPageResponse(employeePage, employeeMapper::toListResponse);
    }

    /**
     * Searches employees based on a search query that may match multiple fields.
     *
     * @param search   the search term
     * @param pageable pagination and sorting parameters
     * @return a page of employees that match the search term
     */
    @Transactional(readOnly = true)
    public PageResponse<EmployeeListResponse> searchEmployees(String search, Pageable pageable) {
        log.debug("Searching employees with term: {}", search);

        Page<Employee> employeePage = employeeRepository.searchEmployees(search, pageable);
        return pageMapper.toPageResponse(employeePage, employeeMapper::toListResponse);
    }

    /**
     * Retrieves employees filtered by their role.
     *
     * @param role     the role to filter by
     * @param pageable pagination and sorting parameters
     * @return a paginated list of employees with the given role
     */
    @Transactional(readOnly = true)
    public PageResponse<EmployeeListResponse> getEmployeesByRole(Role role, Pageable pageable) {
        log.debug("Fetching employees by role: {}", role);

        Page<Employee> employeePage = employeeRepository.findByRole(role, pageable);
        return pageMapper.toPageResponse(employeePage, employeeMapper::toListResponse);
    }

    /**
     * Retrieves employees who directly report to a specific manager.
     *
     * @param managerId the manager's ID
     * @return a list of direct reports
     */
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getDirectReports(Long managerId) {
        log.debug("Fetching direct reports for manager ID: {}", managerId);

        List<Employee> employees = employeeRepository.findActiveEmployeesByManagerId(managerId);
        return employeeMapper.toResponseList(employees);
    }

    /**
     * Creates a new employee with autogenerated employee ID and temporary password.
     * Also initializes contact info and optionally creates pay info.
     *
     * @param request the employee creation request DTO
     * @return a response containing the new employee ID and temporary password
     * @throws BadRequestException if username or email already exists
     * @throws ResourceNotFoundException if provided manager ID does not exist
     */
    @Transactional
    public CreateEmployeeResponse createEmployee(CreateEmployeeRequest request) {
        log.info("Creating new employee: {}", request.getUsername());

        // Validate uniqueness
        if (employeeRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists: " + request.getUsername());
        }

        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists: " + request.getEmail());
        }

        // Generate employee ID
        String employeeId = generateUniqueEmployeeId();

        // Generate temporary password
        String temporaryPassword = EmployeeUtils.generateTemporaryPassword();

        // Create employee entity
        Employee employee = employeeMapper.toEntity(request);
        employee.setEmployeeId(employeeId);
        employee.setPassword(passwordEncoder.encode(temporaryPassword));
        employee.setIsFirstLogin(true);
        employee.setIsActive(true);

        // Set manager if provided
        if (request.getManagerId() != null) {
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Manager not found with ID: " + request.getManagerId()));

            if (manager.getRole() != Role.MANAGER && manager.getRole() != Role.ADMIN) {
                throw new BadRequestException("Selected manager does not have manager role");
            }

            employee.setManager(manager);
        }

        // Save employee
        Employee savedEmployee = employeeRepository.save(employee);

        // Create pay info if provided
        if (request.getPayInfo() != null) {
            PayInfo payInfo = payInfoMapper.toEntity(request.getPayInfo());
            payInfo.setEmployee(savedEmployee);
            payInfoRepository.save(payInfo);
        }

        // Create empty contact info
        ContactInfo contactInfo = ContactInfo.builder()
                .employee(savedEmployee)
                .build();
        contactInfoRepository.save(contactInfo);

        log.info("Employee created successfully: {}", savedEmployee.getEmployeeId());

        return CreateEmployeeResponse.builder()
                .message("Employee created successfully")
                .employeeId(savedEmployee.getEmployeeId())
                .temporaryPassword(temporaryPassword)
                .id(savedEmployee.getId())
                .build();
    }

    /**
     * Updates the personal details of an existing employee, including manager assignment.
     *
     * @param employeeId the ID of the employee being updated
     * @param request    the DTO containing updated personal info
     * @return a confirmation message
     * @throws ResourceNotFoundException if the employee or new manager does not exist
     * @throws BadRequestException if updated email already belongs to another employee
     */
    @Transactional
    public MessageResponse updateEmployeePersonalInfo(Long employeeId,
                                                      UpdateEmployeePersonalInfoRequest request) {
        log.info("Updating personal info for employee ID: {}", employeeId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        // Check email uniqueness if changed
        if (!employee.getEmail().equals(request.getEmail()) &&
                employeeRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists: " + request.getEmail());
        }

        // Update manager if changed
        if (request.getManagerId() != null &&
                !request.getManagerId().equals(employee.getManager() != null ?
                        employee.getManager().getId() : null)) {
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Manager not found with ID: " + request.getManagerId()));
            employee.setManager(manager);
        }

        // Update fields
        employeeMapper.updateEntityFromRequest(employee, request);
        employeeRepository.save(employee);

        log.info("Personal info updated for employee: {}", employee.getUsername());

        return new MessageResponse("Employee information updated successfully");
    }

    /**
     * Retrieves the contact information for an employee specified by username.
     *
     * @param username the username of the employee
     * @return the contact info DTO
     * @throws ResourceNotFoundException if the employee cannot be found
     */
    @Transactional(readOnly = true)
    public ContactInfoResponse getContactInfo(String username) {
        log.debug("Fetching contact info for user: {}", username);

        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        ContactInfo contactInfo = contactInfoRepository.findByEmployeeId(employee.getId())
                .orElse(ContactInfo.builder().employee(employee).build());

        return contactInfoMapper.toResponse(contactInfo);
    }

    /**
     * Updates the contact information for an employee.
     *
     * @param username the username of the employee
     * @param request  the DTO containing updated contact details
     * @return a confirmation message
     * @throws ResourceNotFoundException if the employee does not exist
     */
    @Transactional
    public MessageResponse updateContactInfo(String username, UpdateContactInfoRequest request) {
        log.info("Updating contact info for user: {}", username);

        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        ContactInfo contactInfo = contactInfoRepository.findByEmployeeId(employee.getId())
                .orElse(ContactInfo.builder().employee(employee).build());

        contactInfoMapper.updateEntityFromRequest(contactInfo, request);
        contactInfoRepository.save(contactInfo);

        log.info("Contact info updated for user: {}", username);

        return new MessageResponse("Contact information updated successfully");
    }

    /**
     * Retrieves pay information for an employee based on their username.
     *
     * @param username the username of the employee
     * @return the pay information DTO
     * @throws ResourceNotFoundException if employee or pay info does not exist
     */
    @Transactional(readOnly = true)
    public PayInfoResponse getPayInfo(String username) {
        log.debug("Fetching pay info for user: {}", username);

        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        PayInfo payInfo = payInfoRepository.findByEmployeeId(employee.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Pay information not found"));

        return payInfoMapper.toResponse(payInfo);
    }

    /**
     * Retrieves a dashboard summary for the logged-in user,
     * including personal information and role-based statistics.
     *
     * @param username the username of the employee
     * @return a dashboard response object
     * @throws ResourceNotFoundException if employee does not exist
     */
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(String username) {
        log.debug("Fetching dashboard for user: {}", username);

        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        EmployeeResponse personalInfo = employeeMapper.toResponse(employee);

        // Build stats based on role
        DashboardResponse.DashboardStats stats = DashboardResponse.DashboardStats.builder()
                .directReportsCount(employee.getRole() == Role.MANAGER || employee.getRole() == Role.ADMIN ?
                        employeeRepository.findByManagerId(employee.getId()).size() : 0)
                .build();

        return DashboardResponse.builder()
                .personalInfo(personalInfo)
                .stats(stats)
                .build();
    }

    /**
     * Generates a unique employee ID.
     *
     * @return a newly generated unique employee ID
     */
    private String generateUniqueEmployeeId() {
        String employeeId;
        do {
            employeeId = EmployeeUtils.generateEmployeeId();
        } while (employeeRepository.existsByEmployeeId(employeeId));

        return employeeId;
    }

    /**
     * Retrieves all active employees who have a manager or admin role.
     *
     * @return a list of active managers
     */
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllActiveManagers() {
        log.debug("Fetching all active managers");

        List<Employee> managers = employeeRepository.findAllActiveManagers();
        return employeeMapper.toResponseList(managers);
    }

}
