package com.example.hrms.services;

import com.example.hrms.dto.MessageResponse;
import com.example.hrms.dto.PayInfoResponse;
import com.example.hrms.dto.UpdatePayInfoRequest;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.mappers.PayInfoMapper;
import com.example.hrms.models.Employee;
import com.example.hrms.models.PayInfo;
import com.example.hrms.repositories.EmployeeRepository;
import com.example.hrms.repositories.PayInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayInfoService {
    private final PayInfoRepository payInfoRepository;
    private final EmployeeRepository employeeRepository;
    private final PayInfoMapper payInfoMapper;

    /**
     * Retrieves the pay information associated with the specified employee ID.
     *
     * <p>This method performs a read-only transactional operation. If no pay
     * information is found, a {@link ResourceNotFoundException} is thrown.</p>
     *
     * @param employeeId the ID of the employee whose pay information is being retrieved
     * @return a {@link PayInfoResponse} containing the employee’s pay information
     * @throws ResourceNotFoundException if no pay information exists for the employee
     */
    @Transactional(readOnly = true)
    public PayInfoResponse getPayInfoByEmployeeId(Long employeeId) {
        log.debug("Fetching pay info for employee ID: {}", employeeId);

        PayInfo payInfo = payInfoRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pay information not found for employee ID: " + employeeId));

        return payInfoMapper.toResponse(payInfo);
    }

    /**
     * Updates or creates pay information for an existing employee.
     *
     * <p>If the employee exists but has no associated pay information, a new
     * {@link PayInfo} entity is created and associated with the employee.</p>
     *
     * @param employeeId the ID of the employee whose pay information is being updated
     * @param request the request body containing updated pay information fields
     * @return a {@link MessageResponse} confirming the update
     * @throws ResourceNotFoundException if the employee is not found
     */
    @Transactional
    public MessageResponse updatePayInfo(Long employeeId, UpdatePayInfoRequest request) {
        log.info("Updating pay info for employee ID: {}", employeeId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with ID: " + employeeId));

        PayInfo payInfo = payInfoRepository.findByEmployeeId(employeeId)
                .orElse(PayInfo.builder().employee(employee).build());

        payInfoMapper.updateEntityFromRequest(payInfo, request);
        payInfoRepository.save(payInfo);

        log.info("Pay info updated for employee ID: {}", employeeId);

        return new MessageResponse("Pay information updated successfully");
    }

    /**
     * Creates and saves new pay information for the specified employee.
     *
     * <p>This method ensures that the employee exists and that no pay
     * information has already been created for the employee. If pay information
     * already exists, an {@link IllegalStateException} is thrown.</p>
     *
     * @param employeeId the ID of the employee for whom pay information is to be created
     * @param request the request object containing the pay information details
     * @return a {@link PayInfoResponse} with the created pay information
     * @throws ResourceNotFoundException if the employee is not found
     * @throws IllegalStateException if pay information already exists for the employee
     */
    @Transactional
    public PayInfoResponse createPayInfo(Long employeeId, UpdatePayInfoRequest request) {
        log.info("Creating pay info for employee ID: {}", employeeId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with ID: " + employeeId));

        if (payInfoRepository.existsByEmployeeId(employeeId)) {
            throw new IllegalStateException("Pay information already exists for employee");
        }

        PayInfo payInfo = payInfoMapper.toEntity(request);
        payInfo.setEmployee(employee);

        PayInfo saved = payInfoRepository.save(payInfo);

        log.info("Pay info created for employee ID: {}", employeeId);

        return payInfoMapper.toResponse(saved);
    }
}
