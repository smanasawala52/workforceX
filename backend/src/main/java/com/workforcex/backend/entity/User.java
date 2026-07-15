package com.workforcex.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"countryCode", "mobileNumber"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Country code stored separately for future internationalization
    // Defaults to +91 (India)
    @Column(nullable = false)
    private String countryCode = "+91";

    @Column(nullable = false)
    private String mobileNumber; // 10-digit local number, no country code

    @Column(nullable = false)
    private String password; // Bcrypt hash

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /** Returns the full international number, e.g. +919876543210 */
    public String getFullMobileNumber() {
        return countryCode + mobileNumber;
    }
}
