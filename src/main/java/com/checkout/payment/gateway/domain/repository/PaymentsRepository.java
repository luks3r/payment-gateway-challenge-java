package com.checkout.payment.gateway.domain.repository;

import com.checkout.payment.gateway.domain.model.Payment;
import java.util.Optional;
import java.util.UUID;

public interface PaymentsRepository {
  void save(Payment payment);

  Optional<Payment> findById(UUID id);
}
