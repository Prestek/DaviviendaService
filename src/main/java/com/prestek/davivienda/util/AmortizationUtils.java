package com.prestek.davivienda.util;

public final class AmortizationUtils {
    // EA -> tasa mensual equivalente
    public static double eaToMonthly(double ea) {
        return Math.pow(1 + ea, 1.0/12) - 1;
    }
    public static long estimateMonthlyPayment(long amount, int termMonths, double rateEA) {
        double i = eaToMonthly(rateEA);
        if (i == 0) return Math.round((double) amount / termMonths);
        double pmt = amount * (i / (1 - Math.pow(1 + i, -termMonths)));
        return Math.round(pmt);
    }
    // APR estimado simple incorporando fees up-front
    public static double estimateAprEA(long amount, int termMonths, double payment, long feesUpfront) {
        double financed = amount - feesUpfront;
        if (financed <= 0) financed = amount * 0.98; // fallback
        double guessMonthly = payment * termMonths / financed / termMonths; // arranque tosco
        double i = Math.max(guessMonthly, 0.005);
        for (int k=0;k<30;k++) {
            double npv=0, d=1;
            for (int t=1;t<=termMonths;t++) { d*= (1+i); npv += payment/d; }
            double f = npv - financed;
            if (Math.abs(f) < 1e-6) break;
            // derivada aproximada
            double npv2=0; d=1; double h=1e-5;
            for (int t=1;t<=termMonths;t++) { d*= (1+i+h); npv2 += payment/d; }
            double df = (npv2 - npv)/h;
            i -= f/df;
            i = Math.max(i, 1e-6);
        }
        return Math.pow(1+i,12)-1;
    }
}
