package com.checkout.payment.gateway.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.checkout.payment.gateway.api.model.CreatePaymentRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.YearMonth;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CreatePaymentRequestValidationTest {
  private static ValidatorFactory validatorFactory;
  private static Validator validator;

  @BeforeAll
  static void setUp() {
    validatorFactory = Validation.buildDefaultValidatorFactory();
    validator = validatorFactory.getValidator();
  }

  @AfterAll
  static void tearDown() {
    validatorFactory.close();
  }

  @Test
  void validRequestHasNoViolations() {
    CreatePaymentRequest request = validRequest();

    Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

    assertThat(violations).isEmpty();
  }

  @Test
  void invalidCardNumberFailsValidation() {
    CreatePaymentRequest request = new CreatePaymentRequest(
        "12345",
        12,
        YearMonth.now().plusYears(1).getYear(),
        "USD",
        100,
        "123"
    );

    Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

    assertThat(violations).anyMatch(violation ->
        violation.getPropertyPath().toString().contains("cardNumber"));
  }

  @Test
  void invalidCvvFailsValidation() {
    CreatePaymentRequest request = new CreatePaymentRequest(
        "4242424242424242",
        12,
        YearMonth.now().plusYears(1).getYear(),
        "USD",
        100,
        "12"
    );

    Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

    assertThat(violations).anyMatch(violation ->
        violation.getPropertyPath().toString().contains("cvv"));
  }

  @Test
  void pastExpiryFailsValidation() {
    YearMonth past = YearMonth.now().minusMonths(1);
    CreatePaymentRequest request = new CreatePaymentRequest(
        "4242424242424242",
        past.getMonthValue(),
        past.getYear(),
        "USD",
        100,
        "123"
    );

    Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

    assertThat(violations).anyMatch(violation ->
        violation.getMessage().contains("Expiry date must be in the future"));
  }

  @Test
  void invalidCurrencyFailsValidation() {
    CreatePaymentRequest request = new CreatePaymentRequest(
        "4242424242424242",
        12,
        YearMonth.now().plusYears(1).getYear(),
        "AUD",
        100,
        "123"
    );

    Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

    assertThat(violations).anyMatch(violation ->
        violation.getPropertyPath().toString().contains("currency"));
  }

  @Test
  void nonPositiveAmountFailsValidation() {
    CreatePaymentRequest request = new CreatePaymentRequest(
        "4242424242424242",
        12,
        YearMonth.now().plusYears(1).getYear(),
        "USD",
        0,
        "123"
    );

    Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

    assertThat(violations).anyMatch(violation ->
        violation.getPropertyPath().toString().contains("amount"));
  }

  private static CreatePaymentRequest validRequest() {
    int year = YearMonth.now().plusYears(1).getYear();
    return new CreatePaymentRequest(
        "4242424242424242",
        12,
        year,
        "USD",
        100,
        "123"
    );
  }
}
