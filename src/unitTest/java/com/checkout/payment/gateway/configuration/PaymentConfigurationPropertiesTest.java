package com.checkout.payment.gateway.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class PaymentConfigurationPropertiesTest {

  @Test
  void setterUpdatesSupportedCurrencies() {
    PaymentConfigurationProperties properties = new PaymentConfigurationProperties();

    properties.setSupportedCurrencies(List.of("USD", "JPY"));

    assertThat(properties.getSupportedCurrencies()).containsExactly("USD", "JPY");
  }
}
