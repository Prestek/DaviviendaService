package com.prestek.davivienda.base;

import com.prestek.davivienda.dto.QuoteDto;
import com.prestek.davivienda.request.QuoteRequest;

public interface QuoteProvider {
    String code();                    // Identificador del banco
    QuoteDto quote(QuoteRequest req);// Calcula la cotización
}