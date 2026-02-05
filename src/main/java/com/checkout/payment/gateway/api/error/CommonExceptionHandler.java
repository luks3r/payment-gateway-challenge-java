package com.checkout.payment.gateway.api.error;

import com.checkout.payment.gateway.api.model.ErrorResponse;
import com.checkout.payment.gateway.api.model.RejectedPaymentResponse;
import com.checkout.payment.gateway.api.model.ValidationError;
import com.checkout.payment.gateway.exception.BankClientException;
import com.checkout.payment.gateway.exception.BankUnavailableException;
import com.checkout.payment.gateway.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.exception.PaymentValidationException;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

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
    List<ValidationError> errors = new ArrayList<>();
    if (ex instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
      errors.addAll(validationErrorsFrom(methodArgumentNotValidException));
    }
    if (ex instanceof ConstraintViolationException constraintViolationException) {
      errors.addAll(validationErrorsFrom(constraintViolationException));
    }
    return new ResponseEntity<>(new RejectedPaymentResponse("Rejected", "Validation failed", errors),
        HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<RejectedPaymentResponse> handleMalformedRequest(HttpMessageNotReadableException ex) {
    LOG.warn("Malformed request");
    List<ValidationError> errors = List.of(new ValidationError("body", "Malformed JSON"));
    return new ResponseEntity<>(new RejectedPaymentResponse("Rejected", "Malformed request", errors),
        HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    LOG.warn("Invalid request parameter");
    return new ResponseEntity<>(new ErrorResponse("INVALID_REQUEST", "Invalid request"),
        HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(PaymentValidationException.class)
  public ResponseEntity<RejectedPaymentResponse> handleDomainValidationException(PaymentValidationException ex) {
    LOG.warn("Domain validation failed");
    String field = Optional.ofNullable(ex.getField()).orElse("payment");
    List<ValidationError> errors = List.of(new ValidationError(field, ex.getMessage()));
    return new ResponseEntity<>(new RejectedPaymentResponse("Rejected", "Validation failed", errors),
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

  private List<ValidationError> validationErrorsFrom(MethodArgumentNotValidException ex) {
    Stream<ValidationError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
        .map(this::toValidationError);
    Stream<ValidationError> globalErrors = ex.getBindingResult().getGlobalErrors().stream()
        .map(this::toValidationError);
    return Stream.concat(fieldErrors, globalErrors)
        .filter(Objects::nonNull)
        .toList();
  }

  private ValidationError toValidationError(FieldError error) {
    String field = toSnakeCase(error.getField());
    return new ValidationError(field, error.getDefaultMessage());
  }

  private ValidationError toValidationError(ObjectError error) {
    String field = toSnakeCase(error.getObjectName());
    return new ValidationError(field, error.getDefaultMessage());
  }

  private List<ValidationError> validationErrorsFrom(ConstraintViolationException ex) {
    return ex.getConstraintViolations().stream()
        .map(violation -> {
          String path = violation.getPropertyPath() == null
              ? null
              : violation.getPropertyPath().toString();
          String field = toSnakeCase(extractFieldName(path));
          return new ValidationError(field, violation.getMessage());
        })
        .toList();
  }

  private String extractFieldName(String path) {
    if (path == null || path.isBlank()) {
      return "request";
    }
    int lastDot = path.lastIndexOf('.');
    if (lastDot >= 0 && lastDot + 1 < path.length()) {
      return path.substring(lastDot + 1);
    }
    return path;
  }

  private String toSnakeCase(String value) {
    if (value == null || value.isBlank()) {
      return "request";
    }
    return new PropertyNamingStrategies.SnakeCaseStrategy().translate(value);
  }
}
