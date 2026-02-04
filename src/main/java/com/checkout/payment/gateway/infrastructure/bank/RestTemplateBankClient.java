package com.checkout.payment.gateway.infrastructure.bank;

import com.checkout.payment.gateway.domain.model.PaymentRequest;
import com.checkout.payment.gateway.domain.port.BankClient;
import com.checkout.payment.gateway.exception.BankClientException;
import com.checkout.payment.gateway.exception.BankUnavailableException;
import com.checkout.payment.gateway.infrastructure.bank.model.BankPaymentRequest;
import com.checkout.payment.gateway.infrastructure.bank.model.BankPaymentResponse;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class RestTemplateBankClient implements BankClient {
  private static final DateTimeFormatter EXPIRY_FORMATTER = DateTimeFormatter.ofPattern("MM/yyyy");

  private final RestTemplate restTemplate;
  private final String paymentsUrl;

  public RestTemplateBankClient(RestTemplate restTemplate,
                                @Value("${bank.base-url:http://localhost:8080}") String baseUrl) {
    this.restTemplate = restTemplate;
    this.paymentsUrl = baseUrl + "/payments";
  }

  @Override
  public boolean authorize(PaymentRequest request) {
    try {
      ResponseEntity<BankPaymentResponse> response = restTemplate.postForEntity(
          paymentsUrl,
          toBankRequest(request),
          BankPaymentResponse.class
      );
      if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
        throw new BankClientException("Unexpected response from bank");
      }
      return response.getBody().authorized();
    } catch (HttpServerErrorException.ServiceUnavailable ex) {
      throw new BankUnavailableException("Bank unavailable", ex);
    } catch (HttpStatusCodeException ex) {
      throw new BankClientException("Bank error", ex);
    } catch (RestClientException ex) {
      throw new BankUnavailableException("Bank unavailable", ex);
    }
  }

  private BankPaymentRequest toBankRequest(PaymentRequest request) {
    String expiry = YearMonth.of(request.expiryYear(), request.expiryMonth())
        .format(EXPIRY_FORMATTER);
    return new BankPaymentRequest(
        request.cardNumber(),
        expiry,
        request.currency().toUpperCase(Locale.ROOT),
        request.amount(),
        request.cvv()
    );
  }
}
