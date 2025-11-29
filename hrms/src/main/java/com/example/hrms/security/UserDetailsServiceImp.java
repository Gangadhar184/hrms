package com.example.hrms.security;

import com.example.hrms.models.Employee;
import com.example.hrms.repositories.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public  class UserDetailsServiceImp implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);

        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username: " + username));

        if (!employee.getIsActive()) {
            throw new UsernameNotFoundException("User account is inactive: " + username);
        }

        return User.builder()
                .username(employee.getUsername())
                .password(employee.getPassword())
                .authorities(getAuthorities(employee))
                .accountExpired(false)
                .accountLocked(!employee.getIsActive())
                .credentialsExpired(false)
                .disabled(!employee.getIsActive())
                .build();
    }


     //Get authorities (roles) for employee

    private Collection<? extends GrantedAuthority> getAuthorities(Employee employee) {
        // Spring Security requires roles to be prefixed with "ROLE_"
        String role = "ROLE_" + employee.getRole().name();
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }


     // Load user by employee ID

    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        log.debug("Loading user by ID: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with id: " + id));

        if (!employee.getIsActive()) {
            throw new UsernameNotFoundException("User account is inactive with id: " + id);
        }

        return User.builder()
                .username(employee.getUsername())
                .password(employee.getPassword())
                .authorities(getAuthorities(employee))
                .accountExpired(false)
                .accountLocked(!employee.getIsActive())
                .credentialsExpired(false)
                .disabled(!employee.getIsActive())
                .build();
    }
}
