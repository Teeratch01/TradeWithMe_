package com.example.TradewithMe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class Signup extends AppCompatActivity {

    private TextView login_btn;
    private FirebaseAuth mAuth;
    private EditText firstname,lastname,email,password;
    private Button btnSignup;
    private TextInputLayout firstname_ly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        login_btn = findViewById(R.id.login_btn);
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
            }
        });

        btnSignup =findViewById(R.id.btnSignUp);
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();

            }
        });



    }

    private void registerUser(){
        firstname = findViewById(R.id.editFirstname);
        lastname = findViewById(R.id.editLastname);
        email = findViewById(R.id.editEmail);
        password = findViewById(R.id.editPassword);

        String firstname_st = firstname.getText().toString().trim();
        String lastname_st = lastname.getText().toString().trim();
        String email_st = email.getText().toString().trim();
        String password_st =password.getText().toString().trim();

        //Layout
//        firstname_ly = findViewById(R.id.tvFirstname);

        if (firstname_st.isEmpty())
        {
            firstname.setError("Firstname is required");
            firstname.requestFocus();
            return;
        }
        if (lastname_st.isEmpty())
        {
            lastname.setError("Lastname is required");
            lastname.requestFocus();
            return;
        }
        if (email_st.isEmpty())
        {
            email.setError("Email is required");
            email.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email_st).matches())
        {
            email.setError("Please provide valid email!");
            email.requestFocus();
            return;
        }
        if (password_st.isEmpty())
        {
            password.setError("Password is required");
            password.requestFocus();
            return;
        }
        if (password_st.length() <6)
        {
            password.setError("Please fill more password than 6 characters");
            password.requestFocus();
            return;
        }

        mAuth= FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(email_st,password_st).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful())
                {
                    User user_info = new User(
                            firstname_st,
                            lastname_st,
                            email_st
                    );

                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user_info).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){
                                Toast.makeText(Signup.this,"User has been registerd succesfully",Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(Signup.this,Navigation.class);
                                startActivity(intent);
                            }
                            else{
                                Toast.makeText(Signup.this,"Failed registered please try again",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }else{
                    Toast.makeText(Signup.this,"Failed registered please try again",Toast.LENGTH_LONG).show();
                }

            }
        });
    }

}