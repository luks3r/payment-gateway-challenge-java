package com.checkout.payment.gateway.domain.rules;

import java.util.Set;

public interface SupportedCurrencies {
  boolean isSupported(String currency);

  Set<String> allowed();
}
