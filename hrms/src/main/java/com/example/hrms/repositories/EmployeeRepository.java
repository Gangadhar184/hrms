package com.example.hrms.repositories;




import com.example.hrms.models.Employee;
import com.example.hrms.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    List<Employee> findByManagerId(Long managerId);

    List<Employee> findByEmployeeType(Role type);

    boolean existsByEmail(String email);
}

