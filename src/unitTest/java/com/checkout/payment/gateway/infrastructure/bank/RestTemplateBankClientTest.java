package com.checkout.payment.gateway.infrastructure.bank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.checkout.payment.gateway.domain.model.PaymentRequest;
import com.checkout.payment.gateway.exception.BankClientException;
import com.checkout.payment.gateway.exception.BankUnavailableException;
import com.checkout.payment.gateway.infrastructure.bank.model.BankPaymentResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

class RestTemplateBankClientTest {

  @Test
  void authorizeReturnsResponseWhenOk() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
    RestTemplateBankClient client = new RestTemplateBankClient(restTemplate, "http://bank.test");

    server.expect(once(), requestTo("http://bank.test/payments"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess(
            "{\"authorized\":true,\"authorization_code\":\"abc\"}",
            MediaType.APPLICATION_JSON
        ));

    boolean authorized = client.authorize(new PaymentRequest(
        "4242424242424242",
        12,
        2035,
        "USD",
        100,
        "123"
    ));

    assertThat(authorized).isTrue();
    server.verify();
  }

  @Test
  void authorizeThrowsUnavailableOn503() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
    RestTemplateBankClient client = new RestTemplateBankClient(restTemplate, "http://bank.test");

    server.expect(once(), requestTo("http://bank.test/payments"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

    assertThatThrownBy(() -> client.authorize(new PaymentRequest(
        "4242424242424242",
        12,
        2035,
        "USD",
        100,
        "123"
    ))).isInstanceOf(BankUnavailableException.class);
  }

  @Test
  void authorizeThrowsClientExceptionOn400() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
    RestTemplateBankClient client = new RestTemplateBankClient(restTemplate, "http://bank.test");

    server.expect(once(), requestTo("http://bank.test/payments"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withStatus(HttpStatus.BAD_REQUEST));

    assertThatThrownBy(() -> client.authorize(new PaymentRequest(
        "4242424242424242",
        12,
        2035,
        "USD",
        100,
        "123"
    ))).isInstanceOf(BankClientException.class);
  }

  @Test
  void authorizeThrowsClientExceptionOnEmptyBody() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
    RestTemplateBankClient client = new RestTemplateBankClient(restTemplate, "http://bank.test");

    server.expect(once(), requestTo("http://bank.test/payments"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withStatus(HttpStatus.NO_CONTENT));

    assertThatThrownBy(() -> client.authorize(new PaymentRequest(
        "4242424242424242",
        12,
        2035,
        "USD",
        100,
        "123"
    ))).isInstanceOf(BankClientException.class);
  }

  @Test
  void authorizeThrowsUnavailableOnRestClientException() {
    RestTemplate restTemplate = mock(RestTemplate.class);
    when(restTemplate.postForEntity(
        eq("http://bank.test/payments"),
        any(Object.class),
        eq(BankPaymentResponse.class))
    ).thenThrow(new RestClientException("boom"));

    RestTemplateBankClient client = new RestTemplateBankClient(restTemplate, "http://bank.test");

    assertThatThrownBy(() -> client.authorize(new PaymentRequest(
        "4242424242424242",
        12,
        2035,
        "USD",
        100,
        "123"
    ))).isInstanceOf(BankUnavailableException.class);
  }

  @Test
  void authorizeThrowsClientExceptionOnNon2xxResponse() {
    RestTemplate restTemplate = mock(RestTemplate.class);
    ResponseEntity<BankPaymentResponse> response = ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(new BankPaymentResponse(false, null));
    when(restTemplate.postForEntity(
        eq("http://bank.test/payments"),
        any(Object.class),
        eq(BankPaymentResponse.class))
    ).thenReturn(response);

    RestTemplateBankClient client = new RestTemplateBankClient(restTemplate, "http://bank.test");

    assertThatThrownBy(() -> client.authorize(new PaymentRequest(
        "4242424242424242",
        12,
        2035,
        "USD",
        100,
        "123"
    ))).isInstanceOf(BankClientException.class);
  }
}
