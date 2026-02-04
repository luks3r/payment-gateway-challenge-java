package com.checkout.payment.gateway.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.checkout.payment.gateway.domain.model.Payment;
import com.checkout.payment.gateway.domain.model.PaymentRequest;
import com.checkout.payment.gateway.domain.model.PaymentStatus;
import com.checkout.payment.gateway.domain.port.BankClient;
import com.checkout.payment.gateway.domain.port.PaymentIdGenerator;
import com.checkout.payment.gateway.domain.repository.PaymentsRepository;
import com.checkout.payment.gateway.domain.rules.SupportedCurrencies;
import com.checkout.payment.gateway.domain.validation.PaymentRequestValidator;
import com.checkout.payment.gateway.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.infrastructure.repository.InMemoryPaymentsRepository;
import java.time.YearMonth;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PaymentGatewayServiceTest {

  @Test
  void processPaymentStoresAuthorizedPayment() {
    PaymentsRepository repository = new InMemoryPaymentsRepository();
    BankClient bankClient = request -> true;
    UUID fixedId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    PaymentIdGenerator idGenerator = () -> fixedId;
    SupportedCurrencies supportedCurrencies = new FixedSupportedCurrencies();
    PaymentRequestValidator validator = new PaymentRequestValidator(supportedCurrencies);
    PaymentGatewayService service = new PaymentGatewayService(
        repository,
        bankClient,
        idGenerator,
        validator
    );

    PaymentRequest request = new PaymentRequest(
        "4242424242424242",
        12,
        YearMonth.now().plusYears(1).getYear(),
        "USD",
        100,
        "123"
    );

    Payment response = service.processPayment(request);

    assertThat(response.id()).isEqualTo(fixedId);
    assertThat(response.status()).isEqualTo(PaymentStatus.AUTHORIZED);
    assertThat(response.cardNumberLastFour()).isEqualTo("4242");
    assertThat(response.expiryMonth()).isEqualTo(12);
    assertThat(response.expiryYear()).isEqualTo(request.expiryYear());
    assertThat(response.currency()).isEqualTo("USD");
    assertThat(response.amount()).isEqualTo(100);

    Payment stored = repository.findById(response.id()).orElseThrow();
    assertThat(stored.status()).isEqualTo(PaymentStatus.AUTHORIZED);
  }

  @Test
  void processPaymentStoresDeclinedPayment() {
    PaymentsRepository repository = new InMemoryPaymentsRepository();
    BankClient bankClient = request -> false;
    PaymentIdGenerator idGenerator = () -> UUID.randomUUID();
    SupportedCurrencies supportedCurrencies = new FixedSupportedCurrencies();
    PaymentRequestValidator validator = new PaymentRequestValidator(supportedCurrencies);
    PaymentGatewayService service = new PaymentGatewayService(
        repository,
        bankClient,
        idGenerator,
        validator
    );

    PaymentRequest request = new PaymentRequest(
        "4000000000000002",
        1,
        YearMonth.now().plusYears(1).getYear(),
        "EUR",
        2500,
        "999"
    );

    Payment response = service.processPayment(request);

    assertThat(response.status()).isEqualTo(PaymentStatus.DECLINED);
    assertThat(response.cardNumberLastFour()).isEqualTo("0002");
  }

  @Test
  void getPaymentByIdThrowsWhenMissing() {
    PaymentsRepository repository = new InMemoryPaymentsRepository();
    BankClient bankClient = request -> true;
    PaymentIdGenerator idGenerator = () -> UUID.randomUUID();
    SupportedCurrencies supportedCurrencies = new FixedSupportedCurrencies();
    PaymentRequestValidator validator = new PaymentRequestValidator(supportedCurrencies);
    PaymentGatewayService service = new PaymentGatewayService(
        repository,
        bankClient,
        idGenerator,
        validator
    );

    assertThatThrownBy(() -> service.getPaymentById(UUID.randomUUID()))
        .isInstanceOf(PaymentNotFoundException.class);
  }

  @Test
  void getPaymentByIdReturnsStoredPayment() {
    PaymentsRepository repository = new InMemoryPaymentsRepository();
    BankClient bankClient = request -> true;
    PaymentIdGenerator idGenerator = () -> UUID.randomUUID();
    SupportedCurrencies supportedCurrencies = new FixedSupportedCurrencies();
    PaymentRequestValidator validator = new PaymentRequestValidator(supportedCurrencies);
    PaymentGatewayService service = new PaymentGatewayService(
        repository,
        bankClient,
        idGenerator,
        validator
    );
    UUID id = UUID.randomUUID();
    Payment payment = new Payment(
        id,
        PaymentStatus.AUTHORIZED,
        "4242",
        12,
        2030,
        "USD",
        100
    );
    repository.save(payment);

    Payment response = service.getPaymentById(id);

    assertThat(response.id()).isEqualTo(id);
    assertThat(response.status()).isEqualTo(PaymentStatus.AUTHORIZED);
    assertThat(response.cardNumberLastFour()).isEqualTo("4242");
  }

  private static class FixedSupportedCurrencies implements SupportedCurrencies {
    @Override
    public boolean isSupported(String currency) {
      return true;
    }

    @Override
    public java.util.Set<String> allowed() {
      return java.util.Set.of("USD", "EUR", "GBP");
    }
  }
}
