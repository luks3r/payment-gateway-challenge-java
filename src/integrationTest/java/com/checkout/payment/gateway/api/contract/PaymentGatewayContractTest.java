package com.checkout.payment.gateway.api.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.checkout.payment.gateway.domain.port.BankClient;
import com.checkout.payment.gateway.domain.port.PaymentIdGenerator;
import com.checkout.payment.gateway.exception.BankClientException;
import com.checkout.payment.gateway.exception.BankUnavailableException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentGatewayContractTest {
  private static final UUID FIXED_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

  @Autowired
  private MockMvc mvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private BankClient bankClient;

  @Test
  void authorizedPaymentMatchesContract() throws Exception {
    when(bankClient.authorize(any())).thenReturn(true);

    String payload = "{" +
        "\"card_number\":\"4242424242424242\"," +
        "\"expiry_month\":12," +
        "\"expiry_year\":2035," +
        "\"currency\":\"USD\"," +
        "\"amount\":100," +
        "\"cvv\":\"123\"" +
        "}";

    MvcResult result = mvc.perform(post("/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
        .andExpect(status().isOk())
        .andReturn();

    JsonNode actual = objectMapper.readTree(result.getResponse().getContentAsString());
    JsonNode expected = objectMapper.readTree(readResource("contracts/payment-authorized.json"));

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void declinedPaymentMatchesContract() throws Exception {
    when(bankClient.authorize(any())).thenReturn(false);

    String payload = "{" +
        "\"card_number\":\"4000000000000002\"," +
        "\"expiry_month\":12," +
        "\"expiry_year\":2035," +
        "\"currency\":\"USD\"," +
        "\"amount\":100," +
        "\"cvv\":\"123\"" +
        "}";

    MvcResult result = mvc.perform(post("/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
        .andExpect(status().isOk())
        .andReturn();

    JsonNode actual = objectMapper.readTree(result.getResponse().getContentAsString());
    JsonNode expected = objectMapper.readTree(readResource("contracts/payment-declined.json"));

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void rejectedPaymentMatchesContract() throws Exception {
    String payload = "{" +
        "\"card_number\":\"123\"," +
        "\"expiry_month\":12," +
        "\"expiry_year\":2035," +
        "\"currency\":\"USD\"," +
        "\"amount\":100," +
        "\"cvv\":\"123\"" +
        "}";

    MvcResult result = mvc.perform(post("/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
        .andExpect(status().isBadRequest())
        .andReturn();

    JsonNode actual = objectMapper.readTree(result.getResponse().getContentAsString());
    JsonNode expected = objectMapper.readTree(readResource("contracts/payment-rejected.json"));

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void bankUnavailableMatchesContract() throws Exception {
    when(bankClient.authorize(any())).thenThrow(
        new BankUnavailableException("Bank unavailable", new RuntimeException("down"))
    );

    String payload = "{" +
        "\"card_number\":\"4242424242424242\"," +
        "\"expiry_month\":12," +
        "\"expiry_year\":2035," +
        "\"currency\":\"USD\"," +
        "\"amount\":100," +
        "\"cvv\":\"123\"" +
        "}";

    MvcResult result = mvc.perform(post("/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
        .andExpect(status().isServiceUnavailable())
        .andReturn();

    JsonNode actual = objectMapper.readTree(result.getResponse().getContentAsString());
    JsonNode expected = objectMapper.readTree(readResource("contracts/bank-unavailable.json"));

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void bankErrorMatchesContract() throws Exception {
    when(bankClient.authorize(any())).thenThrow(new BankClientException("Bank error"));

    String payload = "{" +
        "\"card_number\":\"4242424242424242\"," +
        "\"expiry_month\":12," +
        "\"expiry_year\":2035," +
        "\"currency\":\"USD\"," +
        "\"amount\":100," +
        "\"cvv\":\"123\"" +
        "}";

    MvcResult result = mvc.perform(post("/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
        .andExpect(status().isBadGateway())
        .andReturn();

    JsonNode actual = objectMapper.readTree(result.getResponse().getContentAsString());
    JsonNode expected = objectMapper.readTree(readResource("contracts/bank-error.json"));

    assertThat(actual).isEqualTo(expected);
  }

  private String readResource(String path) throws IOException {
    ClassPathResource resource = new ClassPathResource(path);
    return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
  }

  @TestConfiguration
  static class FixedIdConfiguration {
    @Bean
    @Primary
    PaymentIdGenerator paymentIdGenerator() {
      return () -> FIXED_ID;
    }
  }
}
