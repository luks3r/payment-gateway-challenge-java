package com.checkout.payment.gateway.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.YearMonth;
import java.util.Locale;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentGatewayE2ETest {
  private static final String BANK_HOST = "localhost";
  private static final int BANK_PORT = 8080;

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private ObjectMapper objectMapper;

  @BeforeAll
  static void ensureBankSimulatorAvailable() {
    assumeTrue(isBankSimulatorAvailable(), "Bank simulator not running on localhost:8080");
  }

  @Test
  void authorizedPaymentFlow() throws Exception {
    ResponseEntity<String> response = postPayment("4242424242424241");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode body = objectMapper.readTree(response.getBody());
    assertThat(body.get("status").asText()).isEqualTo("Authorized");
    assertThat(body.get("card_number_last_four").asText()).isEqualTo("4241");

    String id = body.get("id").asText();
    ResponseEntity<String> getResponse = restTemplate.getForEntity(url("/payments/" + id), String.class);

    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode getBody = objectMapper.readTree(getResponse.getBody());
    assertThat(getBody.get("status").asText()).isEqualTo("Authorized");
    assertThat(getBody.get("card_number_last_four").asText()).isEqualTo("4241");
    assertThat(getBody.get("currency").asText()).isEqualTo("USD");
  }

  @Test
  void declinedPaymentFlow() throws Exception {
    ResponseEntity<String> response = postPayment("4000000000000002");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode body = objectMapper.readTree(response.getBody());
    assertThat(body.get("status").asText()).isEqualTo("Declined");
    assertThat(body.get("card_number_last_four").asText()).isEqualTo("0002");
  }

  @Test
  void bankUnavailableReturns503() throws Exception {
    ResponseEntity<String> response = postPayment("4000000000000000");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    JsonNode body = objectMapper.readTree(response.getBody());
    assertThat(body.get("code").asText()).isEqualTo("BANK_UNAVAILABLE");
    assertThat(body.get("message").asText()).isEqualTo("Bank unavailable");
  }

  @Test
  void invalidRequestReturnsRejected() throws Exception {
    ResponseEntity<String> response = postPayment("123");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    JsonNode body = objectMapper.readTree(response.getBody());
    assertThat(body.get("code").asText()).isEqualTo("VALIDATION_FAILED");
    assertThat(body.get("message").asText()).isEqualTo("Rejected");
  }

  private ResponseEntity<String> postPayment(String cardNumber) {
    YearMonth expiry = YearMonth.now().plusYears(1);
    String payload = String.format(Locale.ROOT,
        "{\"card_number\":\"%s\",\"expiry_month\":%d,\"expiry_year\":%d,\"currency\":\"USD\"," +
            "\"amount\":100,\"cvv\":\"123\"}",
        cardNumber,
        expiry.getMonthValue(),
        expiry.getYear()
    );

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return restTemplate.postForEntity(url("/payments"), new HttpEntity<>(payload, headers), String.class);
  }

  private String url(String path) {
    return "http://localhost:" + port + path;
  }

  private static boolean isBankSimulatorAvailable() {
    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress(BANK_HOST, BANK_PORT), 2000);
      return true;
    } catch (IOException ex) {
      return false;
    }
  }
}
