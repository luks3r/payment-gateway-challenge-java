package com.checkout.payment.gateway.api.error;

import com.checkout.payment.gateway.api.model.ErrorResponse;
import com.checkout.payment.gateway.api.model.RejectedPaymentResponse;
import com.checkout.payment.gateway.exception.BankClientException;
import com.checkout.payment.gateway.exception.BankUnavailableException;
import com.checkout.payment.gateway.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.exception.PaymentValidationException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CommonExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(CommonExceptionHandler.class);

  @ExceptionHandler(PaymentNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(PaymentNotFoundException ex) {
    LOG.info("Payment not found");
    return new ResponseEntity<>(new ErrorResponse("PAYMENT_NOT_FOUND", "Payment not found"),
        HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
  public ResponseEntity<RejectedPaymentResponse> handleValidationException(Exception ex) {
    LOG.warn("Validation failed");
    return new ResponseEntity<>(new RejectedPaymentResponse("Rejected"),
        HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(PaymentValidationException.class)
  public ResponseEntity<RejectedPaymentResponse> handleDomainValidationException(PaymentValidationException ex) {
    LOG.warn("Domain validation failed");
    return new ResponseEntity<>(new RejectedPaymentResponse("Rejected"),
        HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(BankUnavailableException.class)
  public ResponseEntity<ErrorResponse> handleBankUnavailable(BankUnavailableException ex) {
    LOG.warn("Bank unavailable");
    return new ResponseEntity<>(new ErrorResponse("BANK_UNAVAILABLE", "Bank unavailable"),
        HttpStatus.SERVICE_UNAVAILABLE);
  }

  @ExceptionHandler(BankClientException.class)
  public ResponseEntity<ErrorResponse> handleBankClient(BankClientException ex) {
    LOG.error("Bank error", ex);
    return new ResponseEntity<>(new ErrorResponse("BANK_ERROR", "Bank error"),
        HttpStatus.BAD_GATEWAY);
  }
}
