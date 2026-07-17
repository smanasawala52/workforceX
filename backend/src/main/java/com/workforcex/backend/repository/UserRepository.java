package com.workforcex.backend.repository;

import com.workforcex.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByCountryCodeAndMobileNumber(String countryCode, String mobileNumber);

    boolean existsByCountryCodeAndMobileNumber(String countryCode, String mobileNumber);

    @Query("SELECT u FROM User u WHERE CONCAT(u.countryCode, u.mobileNumber) = :fullMobileNumber")
    Optional<User> findByFullMobileNumber(@Param("fullMobileNumber") String fullMobileNumber);
}
