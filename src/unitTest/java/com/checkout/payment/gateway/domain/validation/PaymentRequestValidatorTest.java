package com.checkout.payment.gateway.domain.validation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.checkout.payment.gateway.domain.model.PaymentRequest;
import com.checkout.payment.gateway.domain.rules.SupportedCurrencies;
import com.checkout.payment.gateway.exception.PaymentValidationException;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;

class PaymentRequestValidatorTest {
  private final SupportedCurrencies supportedCurrencies = new SupportedCurrencies() {
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
  private final PaymentRequestValidator validator = new PaymentRequestValidator(supportedCurrencies);

  @Test
  void validRequestPasses() {
    PaymentRequest request = new PaymentRequest(
        "4242424242424242",
        12,
        YearMonth.now().plusYears(1).getYear(),
        "USD",
        100,
        "123"
    );

    validator.validate(request);
  }

  @Test
  void nullRequestFails() {
    assertThatThrownBy(() -> validator.validate(null))
        .isInstanceOf(PaymentValidationException.class);
  }

  @Test
  void nullCardNumberFails() {
    PaymentRequest request = new PaymentRequest(
        null,
        12,
        YearMonth.now().plusYears(1).getYear(),
        "USD",
        100,
        "123"
    );

    assertThatThrownBy(() -> validator.validate(request))
        .isInstanceOf(PaymentValidationException.class);
  }

  @Test
  void invalidCardNumberFails() {
    PaymentRequest request = new PaymentRequest(
        "123",
        12,
        YearMonth.now().plusYears(1).getYear(),
        "USD",
        100,
        "123"
    );

    assertThatThrownBy(() -> validator.validate(request))
        .isInstanceOf(PaymentValidationException.class);
  }

  @Test
  void invalidCvvFails() {
    PaymentRequest request = new PaymentRequest(
        "4242424242424242",
        12,
        YearMonth.now().plusYears(1).getYear(),
        "USD",
        100,
        "12"
    );

    assertThatThrownBy(() -> validator.validate(request))
        .isInstanceOf(PaymentValidationException.class);
  }

  @Test
  void nullCvvFails() {
    PaymentRequest request = new PaymentRequest(
        "4242424242424242",
        12,
        YearMonth.now().plusYears(1).getYear(),
        "USD",
        100,
        null
    );

    assertThatThrownBy(() -> validator.validate(request))
        .isInstanceOf(PaymentValidationException.class);
  }

  @Test
  void invalidExpiryMonthFails() {
    PaymentRequest request = new PaymentRequest(
        "4242424242424242",
        0,
        YearMonth.now().plusYears(1).getYear(),
        "USD",
        100,
        "123"
    );

    assertThatThrownBy(() -> validator.validate(request))
        .isInstanceOf(PaymentValidationException.class);
  }

  @Test
  void invalidExpiryMonthAboveRangeFails() {
    PaymentRequest request = new PaymentRequest(
        "4242424242424242",
        13,
        YearMonth.now().plusYears(1).getYear(),
        "USD",
        100,
        "123"
    );

    assertThatThrownBy(() -> validator.validate(request))
        .isInstanceOf(PaymentValidationException.class);
  }

  @Test
  void pastExpiryFails() {
    YearMonth past = YearMonth.now().minusMonths(1);
    PaymentRequest request = new PaymentRequest(
        "4242424242424242",
        past.getMonthValue(),
        past.getYear(),
        "USD",
        100,
        "123"
    );

    assertThatThrownBy(() -> validator.validate(request))
        .isInstanceOf(PaymentValidationException.class);
  }

  @Test
  void unsupportedCurrencyFails() {
    PaymentRequest request = new PaymentRequest(
        "4242424242424242",
        12,
        YearMonth.now().plusYears(1).getYear(),
        "AUD",
        100,
        "123"
    );

    assertThatThrownBy(() -> validator.validate(request))
        .isInstanceOf(PaymentValidationException.class);
  }

  @Test
  void invalidCurrencyLengthFails() {
    PaymentRequest request = new PaymentRequest(
        "4242424242424242",
        12,
        YearMonth.now().plusYears(1).getYear(),
        "US",
        100,
        "123"
    );

    assertThatThrownBy(() -> validator.validate(request))
        .isInstanceOf(PaymentValidationException.class);
  }

  @Test
  void nullCurrencyFails() {
    PaymentRequest request = new PaymentRequest(
        "4242424242424242",
        12,
        YearMonth.now().plusYears(1).getYear(),
        null,
        100,
        "123"
    );

    assertThatThrownBy(() -> validator.validate(request))
        .isInstanceOf(PaymentValidationException.class);
  }

  @Test
  void nonPositiveAmountFails() {
    PaymentRequest request = new PaymentRequest(
        "4242424242424242",
        12,
        YearMonth.now().plusYears(1).getYear(),
        "USD",
        0,
        "123"
    );

    assertThatThrownBy(() -> validator.validate(request))
        .isInstanceOf(PaymentValidationException.class);
  }
}
