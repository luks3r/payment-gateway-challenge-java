package com.checkout.payment.gateway.api.error;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.checkout.payment.gateway.domain.port.BankClient;
import com.checkout.payment.gateway.domain.validation.PaymentRequestValidator;
import com.checkout.payment.gateway.exception.PaymentValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CommonExceptionHandlerTest {

  @Autowired
  private MockMvc mvc;

  @MockBean
  private BankClient bankClient;

  @MockBean
  private PaymentRequestValidator paymentRequestValidator;

  @Test
  void domainValidationExceptionReturnsRejected() throws Exception {
    doThrow(new PaymentValidationException("card_number", "Card number must be 14-19 digits"))
        .when(paymentRequestValidator)
        .validate(any());
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
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value("Rejected"))
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.errors[0].field").value("card_number"))
        .andExpect(jsonPath("$.errors[0].message").value("Card number must be 14-19 digits"));

    verifyNoInteractions(bankClient);
  }
}
