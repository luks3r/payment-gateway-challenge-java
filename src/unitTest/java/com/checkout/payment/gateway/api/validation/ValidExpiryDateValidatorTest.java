package com.checkout.payment.gateway.api.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.checkout.payment.gateway.api.model.CreatePaymentRequest;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;

class ValidExpiryDateValidatorTest {
  private final ValidExpiryDateValidator validator = new ValidExpiryDateValidator();

  @Test
  void nullValueIsValid() {
    assertThat(validator.isValid(null, null)).isTrue();
  }

  @Test
  void nullMonthIsValid() {
    CreatePaymentRequest request = new CreatePaymentRequest(
        "4242424242424242",
        null,
        2030,
        "USD",
        100,
        "123"
    );

    assertThat(validator.isValid(request, null)).isTrue();
  }

  @Test
  void nullYearIsValid() {
    CreatePaymentRequest request = new CreatePaymentRequest(
        "4242424242424242",
        12,
        null,
        "USD",
        100,
        "123"
    );

    assertThat(validator.isValid(request, null)).isTrue();
  }

  @Test
  void invalidMonthIsValidForClassLevelConstraint() {
    CreatePaymentRequest request = new CreatePaymentRequest(
        "4242424242424242",
        13,
        2030,
        "USD",
        100,
        "123"
    );

    assertThat(validator.isValid(request, null)).isTrue();
  }

  @Test
  void invalidMonthBelowRangeIsValidForClassLevelConstraint() {
    CreatePaymentRequest request = new CreatePaymentRequest(
        "4242424242424242",
        0,
        2030,
        "USD",
        100,
        "123"
    );

    assertThat(validator.isValid(request, null)).isTrue();
  }

  @Test
  void pastExpiryIsInvalid() {
    YearMonth past = YearMonth.now().minusMonths(1);
    CreatePaymentRequest request = new CreatePaymentRequest(
        "4242424242424242",
        past.getMonthValue(),
        past.getYear(),
        "USD",
        100,
        "123"
    );

    assertThat(validator.isValid(request, null)).isFalse();
  }

  @Test
  void futureExpiryIsValid() {
    YearMonth future = YearMonth.now().plusMonths(1);
    CreatePaymentRequest request = new CreatePaymentRequest(
        "4242424242424242",
        future.getMonthValue(),
        future.getYear(),
        "USD",
        100,
        "123"
    );

    assertThat(validator.isValid(request, null)).isTrue();
  }
}
