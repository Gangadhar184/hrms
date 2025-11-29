package com.example.hrms.repositories;

import com.example.hrms.models.ContactInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContactInfoRepository extends JpaRepository<ContactInfo, Long> {

    Optional<ContactInfo> findByEmployeeId(Long employeeId);

    boolean existsByEmployeeId(Long employeeId);

    void deleteByEmployeeId(Long employeeId);

}
