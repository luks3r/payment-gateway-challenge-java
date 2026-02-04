package com.checkout.payment.gateway.domain.port;

import java.util.UUID;

@FunctionalInterface
public interface PaymentIdGenerator {
  UUID nextId();
}
