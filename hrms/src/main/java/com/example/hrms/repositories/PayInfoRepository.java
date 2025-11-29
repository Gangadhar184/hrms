package com.example.hrms.repositories;

import com.example.hrms.models.PayInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PayInfoRepository extends JpaRepository<PayInfo, Long> {

    Optional<PayInfo> findByEmployeeId(Long employeeId);

    boolean existsByEmployeeId(Long employeeId);

    void  deleteByEmployeeId(Long employeeId);

}
