package com.workforcex.backend.security;

import com.workforcex.backend.entity.User;
import com.workforcex.backend.repository.UserRepository;
import com.workforcex.backend.util.PhoneNumbers;
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
        PhoneNumbers.Split split = PhoneNumbers.split(username);

        User user = userRepository.findByCountryCodeAndMobileNumber(split.countryCode(), split.mobileNumber())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getCountryCode() + user.getMobileNumber(), // Use the full number as the username principal
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
