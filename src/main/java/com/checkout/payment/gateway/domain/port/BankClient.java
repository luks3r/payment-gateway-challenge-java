package com.checkout.payment.gateway.domain.port;

import com.checkout.payment.gateway.domain.model.PaymentRequest;

public interface BankClient {
  boolean authorize(PaymentRequest request);
}
