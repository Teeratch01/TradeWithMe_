package com.example.TradewithMe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class Identity_verification_info extends AppCompatActivity {

    private EditText citizen_id,laser_code,date_of_birth,firstname,lastname,resident_current,resident_register,current_occupation,company_name;
    private CountryCodePicker nationality,country;
    private Calendar myCalendar;
    private Button verification_submit_btn;
    private DatabaseReference verification_ref,user_ref;
    private FirebaseAuth firebaseAuth;
    private String current_uid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identity_verification_info);

        citizen_id = findViewById(R.id.citizen_id);
        laser_code = findViewById(R.id.laser_code);
        date_of_birth = findViewById(R.id.date_of_birth);
        nationality = (CountryCodePicker) findViewById(R.id.nationality_picker);
        country = (CountryCodePicker) findViewById(R.id.country_picker);
        firstname = findViewById(R.id.firstname_iv);
        lastname = findViewById(R.id.lastname_iv);
        resident_current = findViewById(R.id.resident_add_current);
        resident_register = findViewById(R.id.resident_add_register);
        current_occupation = findViewById(R.id.current_occupation);
        company_name = findViewById(R.id.company_name);
        verification_submit_btn = findViewById(R.id.id_verification_submit);
        verification_ref = FirebaseDatabase.getInstance().getReference("Identity Verification");
        firebaseAuth = FirebaseAuth.getInstance();
        current_uid =firebaseAuth.getCurrentUser().getUid();
        user_ref = FirebaseDatabase.getInstance().getReference("Users");

        myCalendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };

        date_of_birth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new DatePickerDialog(Identity_verification_info.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();

            }
        });

        verification_submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String citizen_id_text = citizen_id.getText().toString().trim();
                String laser_code_text = laser_code.getText().toString().trim();

                String myFormat = "MM/dd/yy"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                String date_of_birth_text = sdf.format(myCalendar.getTime());

                String nationality_text = nationality.getSelectedCountryName();
                String country_text = country.getSelectedCountryName();
                String firstname_text = firstname.getText().toString().trim();
                String lastname_text = lastname.getText().toString().trim();
                String current_resident = resident_current.getText().toString().trim();
                String register_resident = resident_register.getText().toString().trim();
                String current_occupation_text = current_occupation.getText().toString().trim();
                String company_name_text = company_name.getText().toString().trim();

                if (citizen_id_text.isEmpty())
                {
                    citizen_id.setError("Please specify your citizen ID");
                    citizen_id.requestFocus();
                    return;
                }
                else if (laser_code_text.isEmpty())
                {
                    laser_code.setError("Please specify your laser code or passport number");
                    laser_code.requestFocus();
                    return;
                }
                else if (date_of_birth_text.isEmpty())
                {
                    date_of_birth.setError("Please specify your laser code or passport number");
                    date_of_birth.requestFocus();
                    return;
                }
                else if (firstname_text.isEmpty())
                {
                    firstname.setError("Please specify your firstname");
                    firstname.requestFocus();
                    return;
                }
                else if (lastname_text.isEmpty())
                {
                    lastname.setError("Please specify your lastname");
                    lastname.requestFocus();
                    return;
                }
                else if (current_resident.isEmpty())
                {
                    resident_current.setError("Please specify your current lasident");
                    resident_current.requestFocus();
                    return;
                }
                else if (register_resident.isEmpty())
                {
                    resident_register.setError("Please specify your register lasident");
                    resident_register.requestFocus();
                    return;
                }
                else if (current_occupation_text.isEmpty())
                {
                    current_occupation.setError("Please specify your current occupation");
                    current_occupation.requestFocus();
                    return;
                }
                else if (company_name_text.isEmpty())
                {
                    company_name.setError("Please specify your company name");
                    company_name.requestFocus();
                    return;
                }
                else {

                    HashMap map  = new HashMap();
                    map.put("citizen_id",citizen_id_text);
                    map.put("laser_code",laser_code_text);
                    map.put("date_of_birth",date_of_birth_text);
                    map.put("nationality",nationality_text);
                    map.put("country",country_text);
                    map.put("firstname_verification",firstname_text);
                    map.put("lastname_verification",lastname_text);
                    map.put("current_resident",current_resident);
                    map.put("register_resident",register_resident);
                    map.put("current_occupation",current_occupation_text);
                    map.put("company_name",company_name_text);



                    verification_ref.child(current_uid).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            HashMap map = new HashMap();
                            map.put("verification","");
                            user_ref.child(current_uid).updateChildren(map);

                            sendMail();

                            AlertDialog.Builder alert = new AlertDialog.Builder(Identity_verification_info.this);

                            alert.setCancelable(true);
                            alert.setTitle("Notification");
                            alert.setMessage("Please wait for 30 minutes,Our staff will verify your account, After that you can login by using your email and password, Thank you for your attention");

                            alert.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    FirebaseAuth.getInstance().signOut();
                                    LoginManager.getInstance().logOut();
                                    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
                                    GoogleSignInClient googleSignInClient= GoogleSignIn.getClient(getApplicationContext(),gso);
                                    googleSignInClient.signOut();

                                    dialog.cancel();
                                    Intent intent = new Intent(Identity_verification_info.this,Login.class);
                                    startActivity(intent);
                                }
                            });

                            alert.show();

                        }
                    });
                }

            }
        });

    }

    private void sendMail() {
        String mail = "tradewithmeb2@outlook.com";
        String message = current_uid+" was successfully verificate the account. Please visit to the firebase for checking";
        String subject = current_uid+" verification";

        JavaMailAPI javaMailAPI = new JavaMailAPI(this,mail,subject,message);
        javaMailAPI.execute();
    }

    private void updateLabel() {

        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        date_of_birth.setText(sdf.format(myCalendar.getTime()));
    }
}