package com.checkout.payment.gateway.application;

import com.checkout.payment.gateway.domain.model.Payment;
import com.checkout.payment.gateway.domain.model.PaymentRequest;
import com.checkout.payment.gateway.domain.model.PaymentStatus;
import com.checkout.payment.gateway.domain.port.BankClient;
import com.checkout.payment.gateway.domain.port.PaymentIdGenerator;
import com.checkout.payment.gateway.domain.repository.PaymentsRepository;
import com.checkout.payment.gateway.domain.validation.PaymentRequestValidator;
import com.checkout.payment.gateway.exception.PaymentNotFoundException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentGatewayService {

  private static final Logger LOG = LoggerFactory.getLogger(PaymentGatewayService.class);
  private final PaymentsRepository paymentsRepository;
  private final BankClient bankClient;
  private final PaymentIdGenerator paymentIdGenerator;
  private final PaymentRequestValidator paymentRequestValidator;

  public PaymentGatewayService(PaymentsRepository paymentsRepository,
                               BankClient bankClient,
                               PaymentIdGenerator paymentIdGenerator,
                               PaymentRequestValidator paymentRequestValidator) {
    this.paymentsRepository = paymentsRepository;
    this.bankClient = bankClient;
    this.paymentIdGenerator = paymentIdGenerator;
    this.paymentRequestValidator = paymentRequestValidator;
  }

  public Payment getPaymentById(UUID id) {
    LOG.debug("Requesting access to payment with ID {}", id);
    return paymentsRepository.findById(id)
        .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));
  }

  public Payment processPayment(PaymentRequest paymentRequest) {
    paymentRequestValidator.validate(paymentRequest);
    boolean authorized = bankClient.authorize(paymentRequest);
    PaymentStatus status = authorized ? PaymentStatus.AUTHORIZED : PaymentStatus.DECLINED;
    String lastFour = paymentRequest.cardNumber()
        .substring(paymentRequest.cardNumber().length() - 4);
    UUID id = paymentIdGenerator.nextId();

    Payment payment = new Payment(
        id,
        status,
        lastFour,
        paymentRequest.expiryMonth(),
        paymentRequest.expiryYear(),
        paymentRequest.currency(),
        paymentRequest.amount()
    );

    paymentsRepository.save(payment);
    LOG.info("Payment processed id={} status={} amount={} currency={} last4={}",
        payment.id(),
        payment.status().getDisplayName(),
        payment.amount(),
        payment.currency(),
        payment.cardNumberLastFour());
    return payment;
  }
}
