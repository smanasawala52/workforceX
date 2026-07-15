package com.workforcex.backend.repository;

import com.workforcex.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByCountryCodeAndMobileNumber(String countryCode, String mobileNumber);

    boolean existsByCountryCodeAndMobileNumber(String countryCode, String mobileNumber);
}
