package com.checkout.payment.gateway.domain.validation;

import com.checkout.payment.gateway.domain.model.PaymentRequest;
import com.checkout.payment.gateway.domain.rules.SupportedCurrencies;
import com.checkout.payment.gateway.exception.PaymentValidationException;
import java.time.YearMonth;
import java.util.regex.Pattern;

public class PaymentRequestValidator {
  private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile("\\d{14,19}");
  private static final Pattern CVV_PATTERN = Pattern.compile("\\d{3,4}");
  private final SupportedCurrencies supportedCurrencies;

  public PaymentRequestValidator(SupportedCurrencies supportedCurrencies) {
    this.supportedCurrencies = supportedCurrencies;
  }

  public void validate(PaymentRequest request) {
    if (request == null) {
      throw new PaymentValidationException("Rejected");
    }
    if (request.cardNumber() == null || !CARD_NUMBER_PATTERN.matcher(request.cardNumber()).matches()) {
      throw new PaymentValidationException("Rejected");
    }
    if (request.cvv() == null || !CVV_PATTERN.matcher(request.cvv()).matches()) {
      throw new PaymentValidationException("Rejected");
    }
    if (request.expiryMonth() < 1 || request.expiryMonth() > 12) {
      throw new PaymentValidationException("Rejected");
    }
    YearMonth expiry = YearMonth.of(request.expiryYear(), request.expiryMonth());
    if (!expiry.isAfter(YearMonth.now())) {
      throw new PaymentValidationException("Rejected");
    }
    if (request.currency() == null || request.currency().length() != 3
        || !supportedCurrencies.isSupported(request.currency())) {
      throw new PaymentValidationException("Rejected");
    }
    if (request.amount() <= 0) {
      throw new PaymentValidationException("Rejected");
    }
  }
}
