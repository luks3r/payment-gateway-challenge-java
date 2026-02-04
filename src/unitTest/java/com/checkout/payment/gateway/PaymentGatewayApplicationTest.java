package com.checkout.payment.gateway;

import org.junit.jupiter.api.Test;

class PaymentGatewayApplicationTest {

  @Test
  void mainStartsApplication() {
    PaymentGatewayApplication.main(new String[] {
        "--spring.main.web-application-type=none",
        "--spring.main.banner-mode=off"
    });
  }
}
