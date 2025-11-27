package com.prestek.davivienda.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prestek.davivienda.config.TestSecurityConfig;
import com.prestek.FinancialEntityCore.dto.QuoteDto;
import com.prestek.FinancialEntityCore.request.QuoteRequest;
import com.prestek.FinancialEntityCore.service.AbstractWeightedQuoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QuoteController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@DisplayName("QuoteController Integration Tests")
class QuoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AbstractWeightedQuoteService quoteService;

    private QuoteRequest testRequest;
    private QuoteDto testQuote;

    @BeforeEach
    void setUp() {
        testRequest = new QuoteRequest(
                10000000L,    // amount
                36,           // termMonths
                750,          // score
                5000000L,     // monthlyIncome
                1000000L      // monthlyExpenses
        );

        testQuote = new QuoteDto(
                "DAVIVIENDA",   // institution
                0.18,           // rateEAmin
                0.24,           // rateEAmax
                320000L,        // monthlyPaymentMin
                370000L,        // monthlyPaymentMax
                45000L,         // feesEstimated
                0.21,           // aprEAEstimated
                "2025-12-31"    // validUntil
        );
    }

    @Test
    @DisplayName("POST /api/quotes - Should generate quote successfully")
    void shouldGenerateQuote() throws Exception {
        // Given
        when(quoteService.quote(any(QuoteRequest.class))).thenReturn(testQuote);

        // When & Then
        mockMvc.perform(post("/api/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.institution").value("DAVIVIENDA"))
                .andExpect(jsonPath("$.rateEAmin").value(0.18))
                .andExpect(jsonPath("$.rateEAmax").value(0.24))
                .andExpect(jsonPath("$.monthlyPaymentMin").value(320000L))
                .andExpect(jsonPath("$.monthlyPaymentMax").value(370000L));

        verify(quoteService, times(1)).quote(any(QuoteRequest.class));
    }

    @Test
    @DisplayName("POST /api/quotes - Should handle request with different credit score")
    void shouldHandleRequestWithDifferentCreditScore() throws Exception {
        // Given
        QuoteRequest lowCreditRequest = new QuoteRequest(
                10000000L, 36, 600, 5000000L, 1000000L
        );

        QuoteDto lowCreditQuote = new QuoteDto(
                "DAVIVIENDA", 0.25, 0.31, 350000L, 400000L, 50000L, 0.28, "2025-12-31"
        );

        when(quoteService.quote(any(QuoteRequest.class))).thenReturn(lowCreditQuote);

        // When & Then
        mockMvc.perform(post("/api/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lowCreditRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aprEAEstimated").value(0.28));

        verify(quoteService, times(1)).quote(any(QuoteRequest.class));
    }

    @Test
    @DisplayName("POST /api/quotes - Should handle request with high DTI")
    void shouldHandleRequestWithHighDTI() throws Exception {
        // Given
        QuoteRequest highDTIRequest = new QuoteRequest(
                10000000L, 36, 750, 5000000L, 2500000L
        );

        when(quoteService.quote(any(QuoteRequest.class))).thenReturn(testQuote);

        // When & Then
        mockMvc.perform(post("/api/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(highDTIRequest)))
                .andExpect(status().isOk());

        verify(quoteService, times(1)).quote(any(QuoteRequest.class));
    }

    @Test
    @DisplayName("POST /api/quotes - Should handle short term loan")
    void shouldHandleShortTermLoan() throws Exception {
        // Given
        QuoteRequest shortTermRequest = new QuoteRequest(
                5000000L, 12, 750, 5000000L, 1000000L
        );

        QuoteDto shortTermQuote = new QuoteDto(
                "DAVIVIENDA", 0.16, 0.22, 430000L, 470000L, 30000L, 0.19, "2025-12-31"
        );

        when(quoteService.quote(any(QuoteRequest.class))).thenReturn(shortTermQuote);

        // When & Then
        mockMvc.perform(post("/api/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shortTermRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.institution").value("DAVIVIENDA"));

        verify(quoteService, times(1)).quote(any(QuoteRequest.class));
    }

    @Test
    @DisplayName("POST /api/quotes - Should handle long term loan")
    void shouldHandleLongTermLoan() throws Exception {
        // Given
        QuoteRequest longTermRequest = new QuoteRequest(
                20000000L, 60, 750, 8000000L, 1500000L
        );

        QuoteDto longTermQuote = new QuoteDto(
                "DAVIVIENDA", 0.20, 0.26, 420000L, 480000L, 60000L, 0.23, "2025-12-31"
        );

        when(quoteService.quote(any(QuoteRequest.class))).thenReturn(longTermQuote);

        // When & Then
        mockMvc.perform(post("/api/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(longTermRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.institution").value("DAVIVIENDA"));

        verify(quoteService, times(1)).quote(any(QuoteRequest.class));
    }
}
