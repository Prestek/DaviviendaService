package com.prestek.davivienda.controller;


import com.prestek.FinancialEntityCore.dto.QuoteDto;
import com.prestek.FinancialEntityCore.request.QuoteRequest;
import com.prestek.FinancialEntityCore.service.AbstractWeightedQuoteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class QuoteController {
    private final AbstractWeightedQuoteService service;


    @PostMapping
    public QuoteDto quote(@RequestBody QuoteRequest req) {
        return service.quote(req);
    }
}
