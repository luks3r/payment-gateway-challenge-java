package com.checkout.payment.gateway.configuration;

import com.checkout.payment.gateway.domain.rules.SupportedCurrencies;
import com.checkout.payment.gateway.domain.validation.PaymentRequestValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidationConfiguration {
  @Bean
  public PaymentRequestValidator paymentRequestValidator(SupportedCurrencies supportedCurrencies) {
    return new PaymentRequestValidator(supportedCurrencies);
  }
}
