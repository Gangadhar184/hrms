package com.example.hrms.repositories;




import com.example.hrms.models.Employee;
import com.example.hrms.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByUsername(String username);
    Optional<Employee> findByEmail(String email);
    Optional<Employee> findByEmployeeId(String employeeId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByEmployeeId(String employeeId);
    List<Employee> findByRole(Role role);
    List<Employee> findByManagerId(Long managerId);

    //find active eimployee reporting to manager

    @Query("SELECT e from Employee e WHERE e.manager.id = :managerId AND e.isActive = true")
    List<Employee> findActiveEmployeesByManagerId(@Param("managerId") Long managerId);

    //find all active employees
    List<Employee> findByIsActiveTrue();

    //search employee by name or email
    @Query("SELECT e FROM Employee e WHERE " +
            "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.employeeId) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Employee> searchEmployees(@Param("search") String search, Pageable pageable);

    //find all employees
    Page<Employee> findAll(Pageable pageable);

    Page<Employee> findByRole(Role role, Pageable pageable);

    //find all managersss
    @Query("SELECT e FROM Employee e WHERE e.role = 'MANAGER' AND e.isActive = true")
    List<Employee> findAllActiveManagers();

    long countByRole(Role role);

    //count active employyess
    long countByIsActiveTrue();

    List<Employee> findByIsFirstLoginTrue();


}

