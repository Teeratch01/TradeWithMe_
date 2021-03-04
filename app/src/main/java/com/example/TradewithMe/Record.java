package com.example.TradewithMe;

public class Record {
    private String Status,Uid,Date;


    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public Record()
    {

    }

    public Record(String status,String uid,String date) {
        Status = status;
        Uid = uid;
        Date = date;
    }
}
