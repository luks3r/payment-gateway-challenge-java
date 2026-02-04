package com.checkout.payment.gateway.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class ConfiguredSupportedCurrenciesTest {

  @Test
  void supportsConfiguredCurrencies() {
    PaymentConfigurationProperties properties = new PaymentConfigurationProperties();
    properties.setSupportedCurrencies(List.of("usd", "eur"));
    ConfiguredSupportedCurrencies supportedCurrencies = new ConfiguredSupportedCurrencies(properties);

    assertThat(supportedCurrencies.isSupported("USD")).isTrue();
    assertThat(supportedCurrencies.isSupported("EUR")).isTrue();
    assertThat(supportedCurrencies.isSupported("GBP")).isFalse();
  }

  @Test
  void nullCurrencyIsNotSupported() {
    PaymentConfigurationProperties properties = new PaymentConfigurationProperties();
    ConfiguredSupportedCurrencies supportedCurrencies = new ConfiguredSupportedCurrencies(properties);

    assertThat(supportedCurrencies.isSupported(null)).isFalse();
  }
}
