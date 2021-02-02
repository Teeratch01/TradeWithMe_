package com.example.TradewithMe;

public class User {

    public String firstname,lastname,email,phone_number;

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public User()
    {

    }

    public User(String firstname,String lastname,String email,String phone_number)
    {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.phone_number= phone_number;

    }

}
