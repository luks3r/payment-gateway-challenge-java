package com.checkout.payment.gateway.configuration;

import com.checkout.payment.gateway.domain.rules.SupportedCurrencies;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ConfiguredSupportedCurrencies implements SupportedCurrencies {
  private final Set<String> allowed;

  public ConfiguredSupportedCurrencies(PaymentConfigurationProperties properties) {
    this.allowed = properties.getSupportedCurrencies().stream()
        .map(value -> value.toUpperCase(Locale.ROOT))
        .collect(Collectors.toUnmodifiableSet());
  }

  @Override
  public boolean isSupported(String currency) {
    if (currency == null) {
      return false;
    }
    return allowed.contains(currency.toUpperCase(Locale.ROOT));
  }

  @Override
  public Set<String> allowed() {
    return allowed;
  }
}
