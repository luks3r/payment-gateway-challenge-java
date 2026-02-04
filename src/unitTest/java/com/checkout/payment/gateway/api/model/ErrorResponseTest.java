package com.checkout.payment.gateway.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ErrorResponseTest {

  @Test
  void exposesMessageAndToString() {
    ErrorResponse response = new ErrorResponse("VALIDATION_FAILED", "Rejected");

    assertThat(response.getCode()).isEqualTo("VALIDATION_FAILED");
    assertThat(response.getMessage()).isEqualTo("Rejected");
    assertThat(response.toString()).contains("Rejected");
  }
}
