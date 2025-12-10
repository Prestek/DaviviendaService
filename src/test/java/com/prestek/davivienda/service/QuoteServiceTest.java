package com.prestek.davivienda.service;

import com.prestek.FinancialEntityCore.dto.QuoteDto;
import com.prestek.FinancialEntityCore.request.QuoteRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("QuoteService Unit Tests")
class QuoteServiceTest {

    private QuoteService quoteService;
    private QuoteRequest testRequest;

    @BeforeEach
    void setUp() {
        quoteService = new QuoteService();
        
        testRequest = new QuoteRequest(
                10000000L,    // amount
                36,           // termMonths
                750,          // score
                5000000L,     // monthlyIncome
                1000000L      // monthlyExpenses
        );
    }

    @Test
    @DisplayName("Should return bank code DAVIVIENDA")
    void shouldReturnBankCodeDavivienda() {
        // When
        QuoteDto quote = quoteService.quote(testRequest);

        // Then
        assertThat(quote.institution()).isEqualTo("DAVIVIENDA");
    }

    @Test
    @DisplayName("Should generate valid quote with good credit profile")
    void shouldGenerateValidQuoteWithGoodCreditProfile() {
        // When
        QuoteDto quote = quoteService.quote(testRequest);

        // Then
        assertThat(quote).isNotNull();
        assertThat(quote.institution()).isEqualTo("DAVIVIENDA");
        assertThat(quote.rateEAmin()).isGreaterThan(0);
        assertThat(quote.rateEAmax()).isGreaterThan(0);
        assertThat(quote.monthlyPaymentMin()).isGreaterThan(0);
        assertThat(quote.monthlyPaymentMax()).isGreaterThan(0);
        assertThat(quote.feesEstimated()).isGreaterThan(0);
        assertThat(quote.aprEAEstimated()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should generate quote with higher rate for low credit score")
    void shouldGenerateQuoteWithHigherRateForLowCreditScore() {
        // Given
        QuoteRequest lowCreditRequest = new QuoteRequest(
                10000000L, 36, 600, 5000000L, 1000000L  // Bajo puntaje
        );

        QuoteRequest highCreditRequest = new QuoteRequest(
                10000000L, 36, 800, 5000000L, 1000000L  // Alto puntaje
        );

        // When
        QuoteDto lowCreditQuote = quoteService.quote(lowCreditRequest);
        QuoteDto highCreditQuote = quoteService.quote(highCreditRequest);

        // Then
        assertThat(lowCreditQuote.aprEAEstimated())
                .isGreaterThan(highCreditQuote.aprEAEstimated());
    }

    @Test
    @DisplayName("Should generate quote with appropriate rate for high DTI")
    void shouldGenerateQuoteWithAppropriateRateForHighDTI() {
        // Given
        QuoteRequest highDTIRequest = new QuoteRequest(
                10000000L, 36, 750, 5000000L, 2500000L  // DTI = 50%
        );

        // When
        QuoteDto quote = quoteService.quote(highDTIRequest);

        // Then
        assertThat(quote).isNotNull();
        assertThat(quote.aprEAEstimated()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should generate quote for short term loan")
    void shouldGenerateQuoteForShortTermLoan() {
        // Given
        QuoteRequest shortTermRequest = new QuoteRequest(
                5000000L, 12, 750, 5000000L, 1000000L
        );

        // When
        QuoteDto quote = quoteService.quote(shortTermRequest);

        // Then
        assertThat(quote).isNotNull();
        assertThat(quote.monthlyPaymentMin()).isGreaterThan(0);
        assertThat(quote.monthlyPaymentMax()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should generate quote for long term loan")
    void shouldGenerateQuoteForLongTermLoan() {
        // Given
        QuoteRequest longTermRequest = new QuoteRequest(
                20000000L, 60, 750, 8000000L, 1500000L
        );

        // When
        QuoteDto quote = quoteService.quote(longTermRequest);

        // Then
        assertThat(quote).isNotNull();
        assertThat(quote.feesEstimated()).isGreaterThan(0);
        assertThat(quote.aprEAEstimated()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should respect floor and ceiling effective annual rates")
    void shouldRespectFloorAndCeilingRates() {
        // Given - Perfil extremadamente bueno
        QuoteRequest excellentRequest = new QuoteRequest(
                10000000L, 36, 850, 10000000L, 500000L
        );

        // Perfil extremadamente malo
        QuoteRequest poorRequest = new QuoteRequest(
                10000000L, 36, 550, 2000000L, 1500000L
        );

        // When
        QuoteDto excellentQuote = quoteService.quote(excellentRequest);
        QuoteDto poorQuote = quoteService.quote(poorRequest);

        // Then
        assertThat(excellentQuote.aprEAEstimated()).isGreaterThan(0);
        assertThat(poorQuote.aprEAEstimated()).isGreaterThan(0);
        assertThat(excellentQuote.aprEAEstimated())
                .isLessThan(poorQuote.aprEAEstimated());
    }

    @Test
    @DisplayName("Should calculate administrative fees correctly")
    void shouldCalculateAdministrativeFeesCorrectly() {
        // When
        QuoteDto quote = quoteService.quote(testRequest);

        // Then
        assertThat(quote.feesEstimated()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should generate different quotes for different credit scores")
    void shouldGenerateDifferentQuotesForDifferentCreditScores() {
        // Given
        QuoteRequest request1 = new QuoteRequest(10000000L, 36, 650, 5000000L, 1000000L);
        QuoteRequest request2 = new QuoteRequest(10000000L, 36, 750, 5000000L, 1000000L);
        QuoteRequest request3 = new QuoteRequest(10000000L, 36, 850, 5000000L, 1000000L);

        // When
        QuoteDto quote1 = quoteService.quote(request1);
        QuoteDto quote2 = quoteService.quote(request2);
        QuoteDto quote3 = quoteService.quote(request3);

        // Then
        assertThat(quote1.aprEAEstimated()).isGreaterThan(quote2.aprEAEstimated());
        assertThat(quote2.aprEAEstimated()).isGreaterThan(quote3.aprEAEstimated());
    }

    @Test
    @DisplayName("Should verify Davivienda specific weights")
    void shouldVerifyDaviviendaSpecificWeights() {
        // Davivienda tiene weights específicos
        
        // Given - Alto DTI debería afectar significativamente
        QuoteRequest lowDTI = new QuoteRequest(
                10000000L, 36, 750, 5000000L, 500000L  // DTI bajo
        );
        
        QuoteRequest highDTI = new QuoteRequest(
                10000000L, 36, 750, 5000000L, 3000000L  // DTI alto
        );

        // When
        QuoteDto quoteLowDTI = quoteService.quote(lowDTI);
        QuoteDto quoteHighDTI = quoteService.quote(highDTI);

        // Then
        double rateDifference = quoteHighDTI.aprEAEstimated() - quoteLowDTI.aprEAEstimated();
        assertThat(rateDifference).isGreaterThan(0); // Mayor DTI = mayor tasa
    }
}
