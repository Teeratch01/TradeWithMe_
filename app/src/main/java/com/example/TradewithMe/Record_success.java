package com.example.TradewithMe;

public class Record_success {
    private String Rating,Uid,Date,Comment;

    public String getRating() {
        return Rating;
    }

    public void setRating(String rating) {
        Rating = rating;
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

    public String getComment() {
        return Comment;
    }

    public void setComment(String comment) {
        Comment = comment;
    }

    public Record_success()
    {

    }

    public Record_success(String rating, String uid, String date, String comment) {
        Rating = rating;
        Uid = uid;
        Date = date;
        Comment = comment;
    }
}
