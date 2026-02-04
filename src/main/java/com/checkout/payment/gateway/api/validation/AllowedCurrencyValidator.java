package com.checkout.payment.gateway.api.validation;

import com.checkout.payment.gateway.domain.rules.SupportedCurrencies;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;

public class AllowedCurrencyValidator implements ConstraintValidator<AllowedCurrency, String> {
  private Set<String> allowedCurrencies;
  private SupportedCurrencies supportedCurrencies;

  public AllowedCurrencyValidator() {
  }

  @Autowired
  public AllowedCurrencyValidator(SupportedCurrencies supportedCurrencies) {
    this.supportedCurrencies = supportedCurrencies;
  }

  @Override
  public void initialize(AllowedCurrency annotation) {
    if (annotation.value().length == 0 && supportedCurrencies != null) {
      allowedCurrencies = supportedCurrencies.allowed();
      return;
    }
    if (annotation.value().length == 0) {
      allowedCurrencies = Set.of("USD", "EUR", "GBP");
      return;
    }
    allowedCurrencies = Arrays.stream(annotation.value())
        .map(value -> value.toUpperCase(Locale.ROOT))
        .collect(Collectors.toUnmodifiableSet());
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isBlank()) {
      return true;
    }
    return allowedCurrencies.contains(value.toUpperCase(Locale.ROOT));
  }
}
