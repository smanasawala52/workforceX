package com.workforcex.backend.util;

/**
 * The JWT/UserDetails principal (Authentication#getName()) is the FULL mobile
 * number, i.e. countryCode + local number (see CustomUserDetailsService).
 * Services expect countryCode and the local mobile number as separate
 * arguments, so controllers must split the principal rather than rely on a
 * client-supplied header for countryCode.
 */
public final class PhoneNumbers {

    private PhoneNumbers() {
    }

    public record Split(String countryCode, String mobileNumber) {
    }

    public static Split split(String fullMobileNumber) {
        if (fullMobileNumber.startsWith("+91")) {
            return new Split("+91", fullMobileNumber.substring(3));
        } else if (fullMobileNumber.startsWith("+971")) {
            return new Split("+971", fullMobileNumber.substring(4));
        }
        // Fallback for older tokens that might just have the local number
        return new Split("+91", fullMobileNumber);
    }
}
