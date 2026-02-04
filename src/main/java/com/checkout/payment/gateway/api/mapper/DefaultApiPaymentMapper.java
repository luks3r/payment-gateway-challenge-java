package com.checkout.payment.gateway.api.mapper;

import com.checkout.payment.gateway.api.model.CreatePaymentRequest;
import com.checkout.payment.gateway.api.model.PaymentResponse;
import com.checkout.payment.gateway.domain.model.Payment;
import com.checkout.payment.gateway.domain.model.PaymentRequest;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class DefaultApiPaymentMapper implements ApiPaymentMapper {
  @Override
  public PaymentRequest toDomain(CreatePaymentRequest request) {
    return new PaymentRequest(
        request.cardNumber(),
        request.expiryMonth(),
        request.expiryYear(),
        request.currency().toUpperCase(Locale.ROOT),
        request.amount(),
        request.cvv()
    );
  }

  @Override
  public PaymentResponse toResponse(Payment payment) {
    return new PaymentResponse(
        payment.id(),
        payment.status().getDisplayName(),
        payment.cardNumberLastFour(),
        payment.expiryMonth(),
        payment.expiryYear(),
        payment.currency(),
        payment.amount()
    );
  }
}
