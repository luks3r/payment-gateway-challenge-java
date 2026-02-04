package com.checkout.payment.gateway.configuration;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "payment")
public class PaymentConfigurationProperties {
  @Size(min = 1, max = 3)
  private List<@Pattern(regexp = "[A-Za-z]{3}") String> supportedCurrencies =
      List.of("USD", "EUR", "GBP");

  public List<String> getSupportedCurrencies() {
    return supportedCurrencies;
  }

  public void setSupportedCurrencies(List<String> supportedCurrencies) {
    this.supportedCurrencies = supportedCurrencies;
  }
}
