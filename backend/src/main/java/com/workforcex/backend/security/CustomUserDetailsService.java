package com.workforcex.backend.security;

import com.workforcex.backend.entity.User;
import com.workforcex.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // The username from the JWT token is the full mobile number including country code
        String countryCode;
        String mobileNumber;

        if (username.startsWith("+91")) {
            countryCode = "+91";
            mobileNumber = username.substring(3);
        } else if (username.startsWith("+971")) {
            countryCode = "+971";
            mobileNumber = username.substring(4);
        } else {
            // Fallback for older tokens that might just have the 10-digit number
            countryCode = "+91";
            mobileNumber = username;
        }

        User user = userRepository.findByCountryCodeAndMobileNumber(countryCode, mobileNumber)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getCountryCode() + user.getMobileNumber(), // Use the full number as the username principal
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
