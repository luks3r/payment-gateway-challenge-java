package com.checkout.payment.gateway.api.validation;

import com.checkout.payment.gateway.api.model.CreatePaymentRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.YearMonth;

public class ValidExpiryDateValidator implements ConstraintValidator<ValidExpiryDate, CreatePaymentRequest> {
  @Override
  public boolean isValid(CreatePaymentRequest value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }
    Integer month = value.expiryMonth();
    Integer year = value.expiryYear();
    if (month == null || year == null) {
      return true;
    }
    if (month < 1 || month > 12) {
      return true;
    }
    YearMonth expiry = YearMonth.of(year, month);
    return expiry.isAfter(YearMonth.now());
  }
}
