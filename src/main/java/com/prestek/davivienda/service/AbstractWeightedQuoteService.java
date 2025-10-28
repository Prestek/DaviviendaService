package com.prestek.davivienda.service;

import com.prestek.davivienda.base.QuoteProvider;
import com.prestek.davivienda.dto.QuoteDto;
import com.prestek.davivienda.request.QuoteRequest;
import com.prestek.davivienda.util.AmortizationUtils;

public abstract class AbstractWeightedQuoteService implements QuoteProvider {

    // Pesos (suman ~1, pero no es obligatorio)
    protected abstract double wScore();    // peso del score
    protected abstract double wDTI();      // peso del DTI (deuda/ingreso)
    protected abstract double wTerm();     // peso del plazo
    protected abstract double wIncome();   // peso del ingreso

    // Parámetros de negocio por FI
    protected abstract double baseEA();        // Tasa base EA (benchmark FI)
    protected abstract double floorEA();       // Piso EA
    protected abstract double ceilingEA();     // Techo EA
    protected abstract long   baseFees();      // Fees base (COP)
    protected abstract long   minFees();
    protected abstract long   maxFees();

    // Sensibilidades (impacto por factor)
    protected abstract double kScore();     // menor score => sube tasa
    protected abstract double kDTI();       // mayor DTI => sube tasa
    protected abstract double kTerm();      // mayor plazo => sube tasa
    protected abstract double kIncome();    // mayor ingreso => baja tasa

    // Penalización/recompensa adicional por tramos
    protected double dti(QuoteRequest r) {
        double inc = Math.max(r.monthlyIncome(), 1);
        return Math.min( r.monthlyExpenses()/inc, 2.0); // cap 200%
    }

    protected double normScore(QuoteRequest r) { // 0 = mal, 1 = excelente
        return Math.min(Math.max(r.score()/1000.0, 0), 1);
    }

    protected double normTerm(QuoteRequest r) { // normaliza 6-84 meses aprox
        int t = Math.max(6, Math.min(r.termMonths(), 84));
        return (t-6.0)/(84.0-6.0); // 0 en 6m, 1 en 84m
    }

    protected double normIncome(QuoteRequest r) { // ingresos relativos (cap)
        double ref = 4_000_000.0; // ingreso referencia
        return Math.min(r.monthlyIncome()/ref, 2.0); // cap 2
    }

    protected double computeRateEA(QuoteRequest r) {
        double scoreN = normScore(r);      // 0..1
        double dti = dti(r);               // 0..2
        double termN = normTerm(r);        // 0..1
        double incomeN = normIncome(r);    // 0..2

        // “Impactos” lineales por factor (signo en k* decide dirección)
        double delta =
                wScore()  * kScore()  * (1 - scoreN) +  // menor score => +tasa
                        wDTI()    * kDTI()    * (dti)        +  // mayor DTI => +tasa
                        wTerm()   * kTerm()   * (termN)      +  // mayor plazo => +tasa
                        wIncome() * kIncome() * (1 - Math.min(incomeN,1.0)); // mayor ingreso => -tasa (nota el (1 - income))

        double ea = baseEA() + delta;
        ea = Math.max(floorEA(), Math.min(ea, ceilingEA()));
        return ea;
    }

    protected long computeFees(QuoteRequest r, double ea) {
        // Fees base ajustados por riesgo (más riesgo => más fees)
        double scoreN = normScore(r);
        double dti = dti(r);
        double risk = (1 - scoreN) * 0.6 + Math.min(dti,1.5) * 0.4; // 0..~1.5
        long fees = Math.round(baseFees() * (1 + 0.3*risk));
        return Math.max(minFees(), Math.min(fees, maxFees()));
    }

    @Override
    public QuoteDto quote(QuoteRequest req) {
        double ea = computeRateEA(req);
        long fees = computeFees(req, ea);

        // opcional: generar rango (± spread por incertidumbre)
        double spread = 0.02; // 2pp EA
        double eaMin = Math.max(floorEA(), ea - spread/2);
        double eaMax = Math.min(ceilingEA(), ea + spread/2);

        long pmtMin = AmortizationUtils.estimateMonthlyPayment(req.amount(), req.termMonths(), eaMin);
        long pmtMax = AmortizationUtils.estimateMonthlyPayment(req.amount(), req.termMonths(), eaMax);

        double aprEst = AmortizationUtils.estimateAprEA(
                req.amount(), req.termMonths(), (pmtMin+pmtMax)/2.0, fees);

        return new QuoteDto(
                code(),
                eaMin, eaMax,
                pmtMin, pmtMax,
                fees,
                aprEst,
                java.time.LocalDate.now().plusDays(7).toString()
        );
    }
}