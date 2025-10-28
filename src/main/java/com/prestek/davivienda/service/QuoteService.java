package com.prestek.davivienda.service;

import org.springframework.stereotype.Component;

@Component
public class QuoteService extends AbstractWeightedQuoteService {

    @Override public String code() { return "DAVIVIENDA"; }

    @Override protected double wScore()  { return 0.35; }
    @Override protected double wDTI()    { return 0.40; }
    @Override protected double wTerm()   { return 0.10; }
    @Override protected double wIncome() { return 0.15; }

    @Override protected double kScore()  { return 0.09; }
    @Override protected double kDTI()    { return 0.07; } // muy sensibles a DTI
    @Override protected double kTerm()   { return 0.02; }
    @Override protected double kIncome() { return 0.03; }

    @Override protected double baseEA()   { return 0.205; }
    @Override protected double floorEA()  { return 0.16; }
    @Override protected double ceilingEA(){ return 0.34; }
    @Override protected long baseFees()   { return 35_000; } // fees más bajos
    @Override protected long minFees()    { return 25_000; }
    @Override protected long maxFees()    { return 80_000; }
}