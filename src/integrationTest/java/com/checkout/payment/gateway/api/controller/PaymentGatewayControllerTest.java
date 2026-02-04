package com.checkout.payment.gateway.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.checkout.payment.gateway.domain.port.BankClient;
import com.checkout.payment.gateway.exception.BankClientException;
import com.checkout.payment.gateway.exception.BankUnavailableException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentGatewayControllerTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private BankClient bankClient;

  @Test
  void whenPaymentAuthorizedThenCanRetrieveIt() throws Exception {
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
        .andExpect(jsonPath("$.status").value("Authorized"))
        .andExpect(jsonPath("$.card_number_last_four").value("4242"))
        .andReturn();

    Map<String, Object> body = objectMapper.readValue(
        result.getResponse().getContentAsString(),
        new TypeReference<>() {}
    );

    String id = body.get("id").toString();

    mvc.perform(get("/payments/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("Authorized"))
        .andExpect(jsonPath("$.card_number_last_four").value("4242"))
        .andExpect(jsonPath("$.expiry_month").value(12))
        .andExpect(jsonPath("$.expiry_year").value(2035))
        .andExpect(jsonPath("$.currency").value("USD"))
        .andExpect(jsonPath("$.amount").value(100));
  }

  @Test
  void whenPaymentDeclinedThenReturnsDeclinedStatus() throws Exception {
    when(bankClient.authorize(any())).thenReturn(false);

    String payload = "{" +
        "\"card_number\":\"4000000000000002\"," +
        "\"expiry_month\":10," +
        "\"expiry_year\":2033," +
        "\"currency\":\"GBP\"," +
        "\"amount\":2500," +
        "\"cvv\":\"999\"" +
        "}";

    mvc.perform(post("/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("Declined"))
        .andExpect(jsonPath("$.card_number_last_four").value("0002"));
  }

  @Test
  void whenRequestInvalidThenRejectedAndBankNotCalled() throws Exception {
    String payload = "{" +
        "\"card_number\":\"123\"," +
        "\"expiry_month\":12," +
        "\"expiry_year\":2035," +
        "\"currency\":\"USD\"," +
        "\"amount\":100," +
        "\"cvv\":\"123\"" +
        "}";

    mvc.perform(post("/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value("Rejected"));

    verifyNoInteractions(bankClient);
  }

  @Test
  void whenPaymentDoesNotExistThen404IsReturned() throws Exception {
    mvc.perform(get("/payments/{id}", UUID.randomUUID()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("PAYMENT_NOT_FOUND"))
        .andExpect(jsonPath("$.message").value("Payment not found"));
  }

  @Test
  void whenBankUnavailableThen503IsReturned() throws Exception {
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

    mvc.perform(post("/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.code").value("BANK_UNAVAILABLE"))
        .andExpect(jsonPath("$.message").value("Bank unavailable"));
  }

  @Test
  void whenBankErrorsThen502IsReturned() throws Exception {
    when(bankClient.authorize(any())).thenThrow(new BankClientException("Bank error"));

    String payload = "{" +
        "\"card_number\":\"4242424242424242\"," +
        "\"expiry_month\":12," +
        "\"expiry_year\":2035," +
        "\"currency\":\"USD\"," +
        "\"amount\":100," +
        "\"cvv\":\"123\"" +
        "}";

    mvc.perform(post("/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
        .andExpect(status().isBadGateway())
        .andExpect(jsonPath("$.code").value("BANK_ERROR"))
        .andExpect(jsonPath("$.message").value("Bank error"));
  }
}
