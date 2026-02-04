package com.checkout.payment.gateway.api.mapper;

import com.checkout.payment.gateway.api.model.CreatePaymentRequest;
import com.checkout.payment.gateway.api.model.PaymentResponse;
import com.checkout.payment.gateway.domain.model.Payment;
import com.checkout.payment.gateway.domain.model.PaymentRequest;

public interface ApiPaymentMapper {
  PaymentRequest toDomain(CreatePaymentRequest request);

  PaymentResponse toResponse(Payment payment);
}
