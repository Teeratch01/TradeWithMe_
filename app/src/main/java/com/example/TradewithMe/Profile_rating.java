package com.example.TradewithMe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.sax.TemplatesHandler;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile_rating extends AppCompatActivity {

    String name,email,phone_number,image,uid_exchanger,uid_current_user,transaction_number;
    TextView name_ill,email_ill,phone_number_ill,rating_ill,feedback_detail;
    CircleImageView profile_iamge_ill;
    Button Close,start_chat;
    DatabaseReference reference,user_ref;
    FirebaseAuth firebaseAuth;
    RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_rating);

        name = getIntent().getExtras().get("name_bf_chat").toString();
        email = getIntent().getExtras().get("email_for_chat").toString();
        phone_number = getIntent().getExtras().get("phnumber_for_chat").toString();
        image = getIntent().getExtras().get("image_for_chat").toString();
        uid_exchanger=getIntent().getExtras().get("userID_exchanger").toString();
        transaction_number = getIntent().getExtras().get("Transaction_number").toString();

        Log.d("check_phonenumber",phone_number);


        Log.d("check_uid",uid_exchanger);

        name_ill = findViewById(R.id.name_bfchat);
        email_ill = findViewById(R.id.email_bfchat);
        phone_number_ill = findViewById(R.id.phnumber_bfchat);
        profile_iamge_ill = findViewById(R.id.profile_image_beforechat);
        rating_ill = findViewById(R.id.rating_bfchat_text);
        ratingBar = findViewById(R.id.rating_bfchat);



        name_ill.setText("Name : "+name);
        email_ill.setText("Email : "+email);

        if (phone_number.equals("The user have to edit first"))
        {
            phone_number_ill.setText(Html.fromHtml("Phone number : <font color='#FF0000'>"+phone_number+"</font>"));

        }
        else
        {
            phone_number_ill.setText("Phone number : "+phone_number);
        }





        if (!image.isEmpty())
        {
            Picasso.get().load(image).into(profile_iamge_ill);
        }


        FirebaseDatabase.getInstance().getReference("Users").child(uid_exchanger).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("ratings"))
                {
                    Float current_user_rating = Float.valueOf(snapshot.child("ratings").getValue().toString());
//                    ratingBar.setMax(5);
                    ratingBar.setRating(current_user_rating);
//                    ratingBar.setStepSize(current_user_rating);
                    rating_ill.setText("Host Rating : "+String.valueOf(current_user_rating));
                }
                else
                {
                    rating_ill.setText("Host Rating : No Rating Yet");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



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

        user_ref = FirebaseDatabase.getInstance().getReference("Users").child(uid_current_user);

        start_chat = findViewById(R.id.start_chat);
        start_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Contact = "yes";
                Calendar calendar = Calendar.getInstance();
                //Returns current time in millis
                long timeMilli = calendar.getTimeInMillis();

                Contacts setValue = new Contacts(
                        Contact,
                        transaction_number,
                        timeMilli
                );


                reference.child(uid_current_user).child(uid_exchanger).setValue(setValue);

                reference.child(uid_exchanger).child(uid_current_user).setValue(setValue);

                user_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild("image")) {
                            String image = snapshot.child("image").getValue().toString();
                            String name = snapshot.child("firstname").getValue().toString() + " " + snapshot.child("lastname").getValue().toString();

                            getToken(name+" want to contact with you",uid_current_user,image,uid_exchanger,name);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


                Intent chat_act_intent = new Intent(getApplicationContext(),ChatActivity.class);
                chat_act_intent.putExtra("name_chatact",name);
                chat_act_intent.putExtra("other_uid_chatact",uid_exchanger);
                chat_act_intent.putExtra("transaction_number_ch",transaction_number);
                startActivity(chat_act_intent);
            }
        });

        feedback_detail = findViewById(R.id.feedback_detail_rating);
        feedback_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),Feedback.class);
                intent.putExtra("feedback_id",uid_exchanger);
                startActivity(intent);
            }
        });




    }

    private void getToken(String message, String senderID, String senderImage, String receiverID, String sendername)
    {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(uid_exchanger);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String token = "";
                if (snapshot.hasChild("token"))
                {
                    token = snapshot.child("token").getValue().toString();
                }

                String firstname = snapshot.child("firstname").getValue().toString();
                String lastname = snapshot.child("lastname").getValue().toString();
                String name = firstname+" "+lastname;
                String image = "";
                if(snapshot.hasChild("image"))
                {
                    image = snapshot.child("image").getValue().toString();
                }

                JSONObject to = new JSONObject();
                JSONObject data =new JSONObject();
                try {
                    data.put("title",sendername);
                    data.put("message",message);
                    data.put("senderID",senderID);
                    data.put("senderImage",senderImage);
                    data.put("receiverID",receiverID);

                    to.put("to",token);
                    to.put("data",data);

                    sendNotification(to);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendNotification(JSONObject to) {

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,"https://fcm.googleapis.com/fcm/send",to, response -> {

            Log.d("notificaiton","sendNotificaiton: "+response);
        },error->{

            Log.d("notification","sendNotificaiton: "+error);

        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String,String> map=  new HashMap<>();
                map.put("Authorization","key=AAAAHGlCimw:APA91bFM7UOGYpmL41tHmUC_78ov17tBdOXP7xPSorl41evdOOj3tvzurkrzE02ayx5ZStXO8E888luxTr2G04c8njOw1kT6SdJv4WOms2BKq22Hq2jfpPf-sG46GG7kI0P6XsWpgmC4");
                map.put("Content-Type","application/json");
                return map;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        request.setRetryPolicy(new DefaultRetryPolicy(300,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(request);
    }

    @Override
    public void onBackPressed() {
        finish();
    }


}