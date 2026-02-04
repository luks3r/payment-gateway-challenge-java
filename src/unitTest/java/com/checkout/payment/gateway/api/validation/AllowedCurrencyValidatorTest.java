package com.checkout.payment.gateway.api.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.checkout.payment.gateway.api.model.CreatePaymentRequest;
import com.checkout.payment.gateway.domain.rules.SupportedCurrencies;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AllowedCurrencyValidatorTest {
  private AllowedCurrencyValidator validator;

  @BeforeEach
  void setUp() throws Exception {
    SupportedCurrencies supportedCurrencies = new SupportedCurrencies() {
      @Override
      public boolean isSupported(String currency) {
        return "USD".equalsIgnoreCase(currency)
            || "EUR".equalsIgnoreCase(currency)
            || "GBP".equalsIgnoreCase(currency);
      }

      @Override
      public java.util.Set<String> allowed() {
        return java.util.Set.of("USD", "EUR", "GBP");
      }
    };
    validator = new AllowedCurrencyValidator(supportedCurrencies);
    Field currencyField = CreatePaymentRequest.class.getDeclaredField("currency");
    AllowedCurrency annotation = currencyField.getAnnotation(AllowedCurrency.class);
    validator.initialize(annotation);
  }

  @Test
  void nullCurrencyIsValid() {
    assertThat(validator.isValid(null, null)).isTrue();
  }

  @Test
  void blankCurrencyIsValid() {
    assertThat(validator.isValid(" ", null)).isTrue();
  }

  @Test
  void allowedCurrencyIsValid() {
    assertThat(validator.isValid("USD", null)).isTrue();
  }

  @Test
  void unsupportedCurrencyIsInvalid() {
    assertThat(validator.isValid("AUD", null)).isFalse();
  }

  @Test
  void fallsBackWhenSupportedCurrenciesMissing() throws Exception {
    AllowedCurrencyValidator fallbackValidator = new AllowedCurrencyValidator();
    Field currencyField = CreatePaymentRequest.class.getDeclaredField("currency");
    AllowedCurrency annotation = currencyField.getAnnotation(AllowedCurrency.class);
    fallbackValidator.initialize(annotation);

    assertThat(fallbackValidator.isValid("USD", null)).isTrue();
    assertThat(fallbackValidator.isValid("AUD", null)).isFalse();
  }

  @Test
  void usesAnnotationValuesWhenProvided() throws Exception {
    AllowedCurrencyValidator customValidator = new AllowedCurrencyValidator();
    Field currencyField = CustomCurrencyRequest.class.getDeclaredField("currency");
    AllowedCurrency annotation = currencyField.getAnnotation(AllowedCurrency.class);
    customValidator.initialize(annotation);

    assertThat(customValidator.isValid("JPY", null)).isTrue();
    assertThat(customValidator.isValid("USD", null)).isFalse();
  }

  private static class CustomCurrencyRequest {
    @AllowedCurrency({"JPY"})
    private String currency;
  }
}
