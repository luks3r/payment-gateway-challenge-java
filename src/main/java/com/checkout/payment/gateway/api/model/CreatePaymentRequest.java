package com.checkout.payment.gateway.api.model;

import com.checkout.payment.gateway.api.validation.AllowedCurrency;
import com.checkout.payment.gateway.api.validation.ValidExpiryDate;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

@ValidExpiryDate
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CreatePaymentRequest(
    @NotBlank
    @Pattern(regexp = "\\d{14,19}")
    String cardNumber,
    @NotNull
    @Min(1)
    @Max(12)
    Integer expiryMonth,
    @NotNull
    Integer expiryYear,
    @NotBlank
    @Pattern(regexp = "[A-Za-z]{3}")
    @AllowedCurrency
    String currency,
    @NotNull
    @Positive
    Integer amount,
    @NotBlank
    @Pattern(regexp = "\\d{3,4}")
    String cvv
) {
}
