package com.example.TradewithMe;

public class ExchangeData {
    public String have_currency,amount,want_currency,rates;

    public ExchangeData()
    {

    }


    public ExchangeData(String have_currency, String amount, String want_currency, String rates) {
        this.have_currency = have_currency;
        this.amount = amount;
        this.want_currency = want_currency;
        this.rates = rates;
    }
}
