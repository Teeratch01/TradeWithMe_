package com.example.TradewithMe;

import java.sql.Time;

public class Contacts {
    public String Contacts,Transaction_num;
    public long Timestamp;

    public Contacts() {

    }

    public Contacts(String contacts,String transaction_num,long timestamp) {
        Contacts = contacts;
        Transaction_num = transaction_num;
        Timestamp = timestamp;
    }
}
