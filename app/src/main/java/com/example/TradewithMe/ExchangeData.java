package com.example.TradewithMe;

public class ExchangeData {
    public String have_currency,amount,want_currency,rates,uid,latitude,longitude,combine_currency,matched;

    public String getHave_currency() {
        return have_currency;
    }

    public void setHave_currency(String have_currency) {
        this.have_currency = have_currency;
    }

    public String getAmount() {
        return amount;
    }


    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getWant_currency() {
        return want_currency;
    }

    public void setWant_currency(String want_currency) {
        this.want_currency = want_currency;
    }

    public String getRates() {
        return rates;
    }

    public void setRates(String rates) {
        this.rates = rates;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getCombine_currency() {
        return combine_currency;
    }

    public void setCombine_currency(String combine_currency) {
        this.combine_currency = combine_currency;
    }

    public String getMatched() {
        return matched;
    }

    public void setMatched(String matched) {
        this.matched = matched;
    }

    public ExchangeData()
    {

    }


    public ExchangeData(String have_currency, String amount, String want_currency, String rates, String uid, String latitude, String longitude,String combine_currency,String matched) {
        this.have_currency = have_currency;
        this.amount = amount;
        this.want_currency = want_currency;
        this.rates = rates;
        this.uid = uid;
        this.latitude= latitude;
        this.longitude = longitude;
        this.combine_currency = combine_currency;
        this.matched = matched;
    }
}
