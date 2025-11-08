package com.example.javaeloadasbeadando;

public class ExchangeRateData {
    private String date;
    private String currency;
    private String rate;

    public ExchangeRateData(String date, String currency, String rate) {
        this.date = date;
        this.currency = currency;
        this.rate = rate;
    }

    public String getDate() { return date; }
    public String getCurrency() { return currency; }
    public String getRate() { return rate; }
}
