package com.checkout.payment.gateway.infrastructure.id;

import com.checkout.payment.gateway.domain.port.PaymentIdGenerator;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UuidPaymentIdGenerator implements PaymentIdGenerator {
  @Override
  public UUID nextId() {
    return UUID.randomUUID();
  }
}
