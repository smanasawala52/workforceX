package com.workforcex.backend.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PhoneNumbersTest {

    @Test
    void split_indiaNumber_returnsCountryCodeAndLocalNumber() {
        PhoneNumbers.Split result = PhoneNumbers.split("+919876543210");

        assertThat(result.countryCode()).isEqualTo("+91");
        assertThat(result.mobileNumber()).isEqualTo("9876543210");
    }

    @Test
    void split_uaeNumber_returnsCountryCodeAndLocalNumber() {
        PhoneNumbers.Split result = PhoneNumbers.split("+971501234567");

        assertThat(result.countryCode()).isEqualTo("+971");
        assertThat(result.mobileNumber()).isEqualTo("501234567");
    }

    @Test
    void split_numberWithoutKnownPrefix_fallsBackToIndiaWithFullNumberAsLocal() {
        PhoneNumbers.Split result = PhoneNumbers.split("9876543210");

        assertThat(result.countryCode()).isEqualTo("+91");
        assertThat(result.mobileNumber()).isEqualTo("9876543210");
    }

    @Test
    void split_recordEqualityAndAccessors() {
        PhoneNumbers.Split a = new PhoneNumbers.Split("+91", "9876543210");
        PhoneNumbers.Split b = new PhoneNumbers.Split("+91", "9876543210");

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a.toString()).contains("+91").contains("9876543210");
    }
}
