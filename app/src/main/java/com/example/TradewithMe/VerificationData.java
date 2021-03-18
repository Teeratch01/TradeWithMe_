package com.example.TradewithMe;

public class VerificationData {

    public String citizen_id,laser_code,date_of_birth,nationality,country,firstname_verification,lastname_verification,current_resident,register_resident,current_occupation,company_name;



    public VerificationData()
    {

    }

    public VerificationData(String citizen_id, String laser_code, String date_of_birth, String nationality, String country, String firstname_verification, String lastname_verification, String current_resident, String register_resident, String current_occupation, String company_name) {
        this.citizen_id = citizen_id;
        this.laser_code = laser_code;
        this.date_of_birth = date_of_birth;
        this.nationality = nationality;
        this.country = country;
        this.firstname_verification = firstname_verification;
        this.lastname_verification = lastname_verification;
        this.current_resident = current_resident;
        this.register_resident = register_resident;
        this.current_occupation = current_occupation;
        this.company_name = company_name;
    }
}
