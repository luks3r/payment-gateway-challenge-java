package com.checkout.payment.gateway.infrastructure.bank.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record BankPaymentRequest(
    String cardNumber,
    String expiryDate,
    String currency,
    Integer amount,
    String cvv
) {
}
