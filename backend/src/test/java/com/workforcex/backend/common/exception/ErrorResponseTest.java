package com.workforcex.backend.common.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest {

    @Test
    void of_populatesStatusMessageAndTimestamp() {
        ErrorResponse response = ErrorResponse.of(404, "Not found");

        assertThat(response.status()).isEqualTo(404);
        assertThat(response.message()).isEqualTo("Not found");
        assertThat(response.timestamp()).isNotNull();
    }
}
