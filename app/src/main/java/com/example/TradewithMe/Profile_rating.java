package com.example.TradewithMe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import javax.xml.transform.sax.TemplatesHandler;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile_rating extends AppCompatActivity {

    String name,email,phone_number,image,uid_exchanger,uid_current_user;
    TextView name_ill,email_ill,phone_number_ill;
    CircleImageView profile_iamge_ill;
    Button Close,start_chat;
    DatabaseReference reference;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_rating);

        name = getIntent().getExtras().get("name_bf_chat").toString();
        email = getIntent().getExtras().get("email_for_chat").toString();
        phone_number = getIntent().getExtras().get("phnumber_for_chat").toString();
        image = getIntent().getExtras().get("image_for_chat").toString();
        uid_exchanger=getIntent().getExtras().get("userID_exchanger").toString();


        Log.d("check_uid",uid_exchanger);

        name_ill = findViewById(R.id.name_bfchat);
        email_ill = findViewById(R.id.email_bfchat);
        phone_number_ill = findViewById(R.id.phnumber_bfchat);
        profile_iamge_ill = findViewById(R.id.profile_image_beforechat);

        name_ill.setText("Name : "+name);
        email_ill.setText("Email : "+email);
        phone_number_ill.setText("Phone number : "+phone_number);

        Picasso.get().load(image).into(profile_iamge_ill);

        Close = findViewById(R.id.close_back);
        Close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("Contacts");
        uid_current_user = firebaseAuth.getUid();

        start_chat = findViewById(R.id.start_chat);
        start_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Contact = "yes";

                Contacts setValue = new Contacts(
                        Contact
                );

                reference.child(uid_current_user).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.hasChild(uid_exchanger))
                        {
                            reference.child(uid_current_user).child(uid_exchanger).setValue(setValue);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                reference.child(uid_exchanger).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                       if (!snapshot.hasChild(uid_current_user))
                       {
                           reference.child(uid_exchanger).child(uid_current_user).setValue(setValue);
                       }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                Intent chat_act_intent = new Intent(getApplicationContext(),ChatActivity.class);
                chat_act_intent.putExtra("name_chatact",name);
                chat_act_intent.putExtra("other_uid_chatact",uid_exchanger);
                startActivity(chat_act_intent);
            }
        });




    }
    @Override
    public void onBackPressed() {
        finish();
    }


}