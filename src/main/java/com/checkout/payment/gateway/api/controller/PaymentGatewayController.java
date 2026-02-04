package com.checkout.payment.gateway.api.controller;

import com.checkout.payment.gateway.api.mapper.ApiPaymentMapper;
import com.checkout.payment.gateway.api.model.CreatePaymentRequest;
import com.checkout.payment.gateway.api.model.PaymentResponse;
import com.checkout.payment.gateway.application.PaymentGatewayService;
import com.checkout.payment.gateway.domain.model.Payment;
import java.util.UUID;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
public class PaymentGatewayController {

  private final PaymentGatewayService paymentGatewayService;
  private final ApiPaymentMapper paymentMapper;

  public PaymentGatewayController(PaymentGatewayService paymentGatewayService,
                                  ApiPaymentMapper paymentMapper) {
    this.paymentGatewayService = paymentGatewayService;
    this.paymentMapper = paymentMapper;
  }

  @PostMapping
  public ResponseEntity<PaymentResponse> processPayment(
      @Valid @RequestBody CreatePaymentRequest request) {
    Payment payment = paymentGatewayService.processPayment(paymentMapper.toDomain(request));
    return new ResponseEntity<>(paymentMapper.toResponse(payment), HttpStatus.OK);
  }

  @GetMapping("/{id}")
  public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable UUID id) {
    Payment payment = paymentGatewayService.getPaymentById(id);
    return new ResponseEntity<>(paymentMapper.toResponse(payment), HttpStatus.OK);
  }
}
