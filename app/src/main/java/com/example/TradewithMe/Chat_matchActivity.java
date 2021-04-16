package com.example.TradewithMe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class Chat_matchActivity extends AppCompatActivity {

    String name_fromchat,messagereceiverID,messagesenderID,latitude,longitude,myUri = "",transaction_num;
    TextView name_chatmatch,time_chatmatch,rating_name;
    ImageButton SendMessageButton,SendLocationButton,SendImageButton;
    EditText MessageInputText;
    FirebaseAuth firebaseAuth;
    DatabaseReference rootRef,record_ref,success_ref,matched_ref,feedback_ref,contact_ref,currency_ref;
    StorageReference firebaseStorage;
    MessageAdapter messageAdapter;
    RecyclerView userMessageList;
    LinearLayoutManager linearLayoutManager;
    List<Messages> messagesList = new ArrayList<>();
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    Uri imageuri;
    StorageTask uploadTask;
    Button success_btn,Update_rank_btn;
    Dialog rankDailog;
    RatingBar ratingBar;
    long maxIdsender,maxIdreceiver,maxIdfeedback;
    TextInputLayout rating_comment;

    //Encryption
    private byte encryptionKey[] = {9,115,51,86,105,4,-31,-23,-68,88,17,20,3,-105,119,-53};
    private Cipher cipher,deciper;
    private SecretKeySpec secretKeySpec;

    //cancle notification
    String ns = Context.NOTIFICATION_SERVICE;
    NotificationManager mNotificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_match);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Name
        name_fromchat = getIntent().getExtras().get("name_chatact").toString();
        name_chatmatch = findViewById(R.id.name_viachat_match);
        name_chatmatch.setText(name_fromchat);

        //Time
        time_chatmatch = findViewById(R.id.time_viachat_match);
        Thread thread = new Thread(){
            public void run(){
                try {
                    while (!this.isInterrupted())
                    {
                        sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Long date =System.currentTimeMillis();
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd HH:mm");
                                String datastring =  simpleDateFormat.format(date);
                                time_chatmatch.setText(datastring);
                            }
                        });

                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();

        //Send Message
        SendMessageButton = findViewById(R.id.sent_message_match);
        MessageInputText = findViewById(R.id.input_message_match);
        firebaseAuth = FirebaseAuth.getInstance();
        messagesenderID = firebaseAuth.getCurrentUser().getUid();
        messagereceiverID = getIntent().getExtras().get("other_uid_chatact").toString();
        rootRef = FirebaseDatabase.getInstance().getReference();
        SendLocationButton = findViewById(R.id.sent_location_match);
        SendImageButton = findViewById(R.id.sent_image_match);
        firebaseStorage = FirebaseStorage.getInstance().getReference().child("Message Pic");
        transaction_num = getIntent().getExtras().get("transaction_number_ch").toString();


        messageAdapter = new MessageAdapter(messagesList,getApplicationContext());
        userMessageList = findViewById(R.id.private_message_list_user_match);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessageList.setLayoutManager(linearLayoutManager);
        userMessageList.setAdapter(messageAdapter);

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = MessageInputText.getText().toString();
                sendMessage(messageText);

            }
        });

        //Send Loccation Button
        SendLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(Chat_matchActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
                }
                else
                {
                    getCurrentLocation();
                }

            }
        });

        //Send Image
        SendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().start(Chat_matchActivity.this);
            }
        });

        //Add Message in RecyclerView
        rootRef.child("Messages").child(messagesenderID).child(messagereceiverID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Messages messages = snapshot.getValue(Messages.class);

                messagesList.add(messages);
                messageAdapter.notifyDataSetChanged();

                userMessageList.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

                        if ( bottom < oldBottom) {
                            userMessageList.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    userMessageList.smoothScrollToPosition(userMessageList.getAdapter().getItemCount());
                                }
                            }, 0);
                        }
                    }
                });

                userMessageList.scrollToPosition(userMessageList.getAdapter().getItemCount()-1);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //Add rating
        success_btn = findViewById(R.id.success_button_match);
        record_ref = FirebaseDatabase.getInstance().getReference("Record");
        success_ref = FirebaseDatabase.getInstance().getReference("Success_user");
        matched_ref = FirebaseDatabase.getInstance().getReference("Matched_user");
        feedback_ref = FirebaseDatabase.getInstance().getReference("Feedback");
        contact_ref = FirebaseDatabase.getInstance().getReference("Contacts");
        currency_ref = FirebaseDatabase.getInstance().getReference("Currency");
        record_ref.child(messagesenderID).child("Success").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    maxIdsender = snapshot.getChildrenCount();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Check the number of success record in the firebase
        record_ref.child(messagereceiverID).child("Success").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    maxIdreceiver = snapshot.getChildrenCount();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Check the number of feedback in the firebase
        feedback_ref.child(messagereceiverID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    maxIdfeedback = snapshot.getChildrenCount();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Success section
        success_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String success = "yes";
                String status = "Ok";
                SimpleDateFormat date = new SimpleDateFormat("dd MMM yyyy");
                String date_for = date.format(new Date());
                Record setValuesender = new Record(status,messagereceiverID,date_for);
                Record setValuereceiver = new Record(status,messagesenderID,date_for);
                Success_user set_success = new Success_user(success);
                rankDailog = new Dialog(Chat_matchActivity.this);
                rankDailog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                rankDailog.setContentView(R.layout.rank_dialog);
                rankDailog.setCancelable(false);

                ratingBar = rankDailog.findViewById(R.id.dialog_ratingbar);
                ratingBar.setMax(5);
                ratingBar.setRating(0);

                rating_name = rankDailog.findViewById(R.id.rating_name);
                rating_name.setText("Rate "+name_fromchat+"!");

                Update_rank_btn = rankDailog.findViewById(R.id.rank_dialog_button);
                Update_rank_btn.setBackgroundColor(ResourcesCompat.getColor(getResources(),R.color.color_rating,null));

                rating_comment = rankDailog.findViewById(R.id.rating_comment);

                matched_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists())
                        {
                            //Check the other exchanger mathc with you or not
                            if (!snapshot.child(messagereceiverID).hasChild(messagesenderID))
                            {
                                AlertDialog.Builder alert = new AlertDialog.Builder(Chat_matchActivity.this);

                                alert.setCancelable(false);
                                alert.setTitle("Notification");
                                alert.setMessage("You are not match with the other exchanger yet. Wait for the other exchanger to match with you");
                                alert.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                                if (!isFinishing()) {
                                    // show popup
                                    alert.show();
                                }

                            }
                            else {
//                                record_ref.child(messagesenderID).child("Success").child(String.valueOf(maxIdsender+1)).setValue(setValuesender);
                                //Add the success data to firebase to make the notify the other exchanger that the other side want to success the transaction
                                success_ref.child(messagereceiverID).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (!snapshot.hasChild(messagesenderID))
                                        {
                                            success_ref.child(messagereceiverID).child(messagesenderID).setValue(set_success);
//                                            record_ref.child(messagereceiverID).child("Success").child(String.valueOf(maxIdreceiver+1)).setValue(setValuereceiver);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                //When give a rating and feedback(optional) successfully and click on submit button
                                Update_rank_btn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

//                        rankDailog.dismiss();
                                        Log.d("check_rating", String.valueOf(ratingBar.getRating()));
                                        Float user_rating = ratingBar.getRating();

                                        //Check if the user does't rate yet
                                        if (user_rating == 0.0) {
                                            Toast.makeText(Chat_matchActivity.this, "Plesee rate to the other exchanger", Toast.LENGTH_SHORT).show();
                                        } else {

                                            FirebaseDatabase.getInstance().getReference("Users").child(messagesenderID).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                                    success_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            if (snapshot.hasChild(messagereceiverID))
                                                            {
                                                                if (snapshot.child(messagereceiverID).hasChild(messagesenderID))
                                                                {
                                                                    if (snapshot1.hasChild("image"))
                                                                    {
                                                                        String image = snapshot1.child("image").getValue().toString();
                                                                        String name = snapshot1.child("firstname").getValue().toString()+" "+snapshot1.child("lastname").getValue().toString();
                                                                        getToken("The transaction is successful,Please rate to the other exchanger",messagesenderID,image,messagereceiverID,name);
                                                                    }
                                                                    else if (!snapshot1.hasChild("image")){
                                                                        String image = "";
                                                                        String name = snapshot1.child("firstname").getValue().toString()+" "+snapshot1.child("lastname").getValue().toString();
                                                                        Log.d("check_name ",name);

                                                                        getToken("The transaction is successful,Please rate to the other exchanger",messagesenderID,image,messagereceiverID,name);
                                                                    }
                                                                }
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });

                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });

                                            FirebaseDatabase.getInstance().getReference("Users").child(messagereceiverID).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (snapshot.hasChild("ratings")) {
                                                        Float current_user_rating = Float.valueOf(snapshot.child("ratings").getValue().toString());
                                                        Float update_user_rating = (user_rating + current_user_rating) / 2;

                                                        HashMap map = new HashMap();
                                                        map.put("ratings", String.format("%.02f", update_user_rating));

                                                        String comment = rating_comment.getEditText().getText().toString().trim();

                                                        if (comment.equals(""))
                                                        {
                                                            Record_success update_feedback = new Record_success(String.valueOf(user_rating),messagesenderID,date_for,"No comment");
                                                            feedback_ref.child(messagereceiverID).child(String.valueOf(maxIdfeedback+1)).setValue(update_feedback);
                                                        }
                                                        else
                                                        {
                                                            Record_success update_feedback = new Record_success(String.valueOf(user_rating),messagesenderID,date_for,comment);
                                                            feedback_ref.child(messagereceiverID).child(String.valueOf(maxIdfeedback+1)).setValue(update_feedback);
                                                        }

                                                        FirebaseDatabase.getInstance().getReference("Users").child(messagereceiverID).updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                                                            @Override
                                                            public void onComplete(@NonNull Task task) {
                                                                FirebaseDatabase.getInstance().getReference("Currency").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                        if (snapshot.hasChild(transaction_num)) {
                                                                            FirebaseDatabase.getInstance().getReference("Currency").child(transaction_num).getRef().removeValue();
                                                                            FirebaseDatabase.getInstance().getReference("Matched_user").child(messagesenderID).child(messagereceiverID).getRef().removeValue();
                                                                            rankDailog.dismiss();
                                                                            Intent backto_firstpage = new Intent(getApplicationContext(), Navigation.class);
                                                                            startActivity(backto_firstpage);
                                                                        } else {
                                                                            FirebaseDatabase.getInstance().getReference("Matched_user").child(messagesenderID).child(messagereceiverID).getRef().removeValue();
                                                                            rankDailog.dismiss();
                                                                            Intent backto_firstpage = new Intent(getApplicationContext(), Navigation.class);
                                                                            startActivity(backto_firstpage);
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                                    }
                                                                });
                                                            }
                                                        });
                                                    } else {
                                                        HashMap map = new HashMap();
                                                        map.put("ratings", ratingBar.getRating());

                                                        String comment = rating_comment.getEditText().getText().toString().trim();

                                                        if (comment.equals(""))
                                                        {
                                                            Record_success update_feedback = new Record_success(String.valueOf(user_rating),messagesenderID,date_for,"No comment");
                                                            feedback_ref.child(messagereceiverID).child(String.valueOf(maxIdfeedback+1)).setValue(update_feedback);
                                                        }
                                                        else
                                                        {
                                                            Record_success update_feedback = new Record_success(String.valueOf(user_rating),messagesenderID,date_for,comment);
                                                            feedback_ref.child(messagereceiverID).child(String.valueOf(maxIdfeedback+1)).setValue(update_feedback);
                                                        }

                                                        FirebaseDatabase.getInstance().getReference("Users").child(messagereceiverID).updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                                                            @Override
                                                            public void onComplete(@NonNull Task task) {
                                                                FirebaseDatabase.getInstance().getReference("Currency").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                        if (snapshot.hasChild(transaction_num)) {
                                                                            FirebaseDatabase.getInstance().getReference("Currency").child(transaction_num).getRef().removeValue();
                                                                            FirebaseDatabase.getInstance().getReference("Matched_user").child(messagesenderID).child(messagereceiverID).getRef().removeValue();
                                                                            rankDailog.dismiss();
                                                                            Intent backto_firstpage = new Intent(getApplicationContext(), Navigation.class);
                                                                            startActivity(backto_firstpage);

                                                                        } else {
                                                                            FirebaseDatabase.getInstance().getReference("Matched_user").child(messagesenderID).child(messagereceiverID).getRef().removeValue();
                                                                            rankDailog.dismiss();
                                                                            Intent backto_firstpage = new Intent(getApplicationContext(), Navigation.class);
                                                                            startActivity(backto_firstpage);
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                                    }
                                                                });
                                                            }
                                                        });

                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        }
                                    }
                                });

                                if (!isFinishing()) {
                                    // show popup
                                    rankDailog.show();
                                }



                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });

        mNotificationManager = (NotificationManager) getSystemService(ns);
        mNotificationManager.cancelAll();

    }

    @Override
    protected void onStart() {
        super.onStart();
        success_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(messagesenderID)) {
                    if (snapshot.child(messagesenderID).hasChild(messagereceiverID)) {

                        String status = "Ok";
                        SimpleDateFormat date = new SimpleDateFormat("dd MMM yyyy");
                        String date_for = date.format(new Date());

                        Record setValuesender = new Record(status,messagereceiverID,date_for);
                        Record setValuereceiver = new Record(status,messagesenderID,date_for);

                        rankDailog = new Dialog(Chat_matchActivity.this);
                        rankDailog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        rankDailog.setContentView(R.layout.rank_dialog);
                        rankDailog.setCancelable(false);

                        Log.d("check_exist","check");
                        ratingBar = rankDailog.findViewById(R.id.dialog_ratingbar);
                        ratingBar.setMax(5);
                        ratingBar.setRating(0);

                        rating_name = rankDailog.findViewById(R.id.rating_name);
                        rating_name.setText("Rate " + name_fromchat + "!");

                        rating_comment = rankDailog.findViewById(R.id.rating_comment);

                        Update_rank_btn = rankDailog.findViewById(R.id.rank_dialog_button);
                        Update_rank_btn.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.color_rating, null));

                        Update_rank_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
//                                rankDailog.dismiss();
                                Log.d("check_rating", String.valueOf(ratingBar.getRating()));
                                Float user_rating = ratingBar.getRating();
                                if (user_rating == 0.0) {
                                    Toast.makeText(Chat_matchActivity.this, "Plesee rate to the other exchanger", Toast.LENGTH_SHORT).show();
                                }
                                else {

                                    record_ref.child(messagesenderID).child("Success").child(String.valueOf(maxIdsender+1)).setValue(setValuesender);
                                    record_ref.child(messagereceiverID).child("Success").child(String.valueOf(maxIdreceiver+1)).setValue(setValuereceiver);

                                    FirebaseDatabase.getInstance().getReference("Users").child(messagereceiverID).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.hasChild("ratings")) {
                                                Float current_user_rating = Float.valueOf(snapshot.child("ratings").getValue().toString());
                                                Float update_user_rating = (user_rating + current_user_rating) / 2;

                                                HashMap map = new HashMap();
                                                map.put("ratings", String.format("%.02f", update_user_rating));

                                                String comment = rating_comment.getEditText().getText().toString().trim();

                                                if (comment.equals(""))
                                                {
                                                    Record_success update_feedback = new Record_success(String.valueOf(user_rating),messagesenderID,date_for,"No comment");
                                                    feedback_ref.child(messagereceiverID).child(String.valueOf(maxIdfeedback+1)).setValue(update_feedback);
                                                }
                                                else
                                                {
                                                    Record_success update_feedback = new Record_success(String.valueOf(user_rating),messagesenderID,date_for,comment);
                                                    feedback_ref.child(messagereceiverID).child(String.valueOf(maxIdfeedback+1)).setValue(update_feedback);
                                                }

                                                FirebaseDatabase.getInstance().getReference("Users").child(messagereceiverID).updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                                                    @Override
                                                    public void onComplete(@NonNull Task task) {
                                                        FirebaseDatabase.getInstance().getReference("Currency").addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                if (snapshot.hasChild(transaction_num)) {
                                                                    FirebaseDatabase.getInstance().getReference("Currency").child(transaction_num).getRef().removeValue();

                                                                    contact_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                            HashMap map = new HashMap();
                                                                            map.put("Transaction_num","");
                                                                            if (!snapshot.child(messagesenderID).child(messagereceiverID).child("Transaction_num").equals(""))
                                                                            {
                                                                                contact_ref.child(messagesenderID).child(messagereceiverID).updateChildren(map);
                                                                            }
                                                                            if (!snapshot.child(messagereceiverID).child(messagesenderID).child("Transaction_num").equals(""))
                                                                            {
                                                                                contact_ref.child(messagereceiverID).child(messagesenderID).updateChildren(map);
                                                                            }

                                                                        }

                                                                        @Override
                                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                                        }
                                                                    });


                                                                    FirebaseDatabase.getInstance().getReference("Matched_user").child(messagesenderID).child(messagereceiverID).getRef().removeValue();
                                                                    FirebaseDatabase.getInstance().getReference("Success_user").child(messagesenderID).child(messagereceiverID).getRef().removeValue();
                                                                    rankDailog.dismiss();
                                                                    Intent backto_firstpage = new Intent(getApplicationContext(), Navigation.class);
                                                                    startActivity(backto_firstpage);
                                                                } else {

                                                                    contact_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                            HashMap map = new HashMap();
                                                                            map.put("Transaction_num","");
                                                                            if (!snapshot.child(messagesenderID).child(messagereceiverID).child("Transaction_num").equals(""))
                                                                            {
                                                                                contact_ref.child(messagesenderID).child(messagereceiverID).updateChildren(map);
                                                                            }
                                                                            if (!snapshot.child(messagereceiverID).child(messagesenderID).child("Transaction_num").equals(""))
                                                                            {
                                                                                contact_ref.child(messagereceiverID).child(messagesenderID).updateChildren(map);
                                                                            }

                                                                        }

                                                                        @Override
                                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                                        }
                                                                    });

                                                                    FirebaseDatabase.getInstance().getReference("Matched_user").child(messagesenderID).child(messagereceiverID).getRef().removeValue();
                                                                    FirebaseDatabase.getInstance().getReference("Success_user").child(messagesenderID).child(messagereceiverID).getRef().removeValue();
                                                                    rankDailog.dismiss();
                                                                    Intent backto_firstpage = new Intent(getApplicationContext(), Navigation.class);
                                                                    startActivity(backto_firstpage);
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {

                                                            }
                                                        });

                                                    }
                                                });
                                            } else {
                                                HashMap map = new HashMap();
                                                map.put("ratings", ratingBar.getRating());

                                                String comment = rating_comment.getEditText().getText().toString().trim();

                                                if (comment.equals(""))
                                                {
                                                    Record_success update_feedback = new Record_success(String.valueOf(user_rating),messagesenderID,date_for,"No comment");
                                                    feedback_ref.child(messagereceiverID).child(String.valueOf(maxIdfeedback+1)).setValue(update_feedback);
                                                }
                                                else
                                                {
                                                    Record_success update_feedback = new Record_success(String.valueOf(user_rating),messagesenderID,date_for,comment);
                                                    feedback_ref.child(messagereceiverID).child(String.valueOf(maxIdfeedback+1)).setValue(update_feedback);
                                                }

                                                FirebaseDatabase.getInstance().getReference("Users").child(messagereceiverID).updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                                                    @Override
                                                    public void onComplete(@NonNull Task task) {

                                                        FirebaseDatabase.getInstance().getReference("Currency").addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                if (snapshot.hasChild(transaction_num)) {
                                                                    FirebaseDatabase.getInstance().getReference("Currency").child(transaction_num).getRef().removeValue();

                                                                    contact_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                            HashMap map = new HashMap();
                                                                            map.put("Transaction_num","");
                                                                            if (!snapshot.child(messagesenderID).child(messagereceiverID).child("Transaction_num").equals(""))
                                                                            {
                                                                                contact_ref.child(messagesenderID).child(messagereceiverID).updateChildren(map);
                                                                            }
                                                                            if (!snapshot.child(messagereceiverID).child(messagesenderID).child("Transaction_num").equals(""))
                                                                            {
                                                                                contact_ref.child(messagereceiverID).child(messagesenderID).updateChildren(map);
                                                                            }

                                                                        }

                                                                        @Override
                                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                                        }
                                                                    });

                                                                    FirebaseDatabase.getInstance().getReference("Matched_user").child(messagesenderID).child(messagereceiverID).getRef().removeValue();
                                                                    FirebaseDatabase.getInstance().getReference("Success_user").child(messagesenderID).child(messagereceiverID).getRef().removeValue();
                                                                    rankDailog.dismiss();
                                                                    Intent backto_firstpage = new Intent(getApplicationContext(), Navigation.class);
                                                                    startActivity(backto_firstpage);
                                                                } else {
                                                                    contact_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                            HashMap map = new HashMap();
                                                                            map.put("Transaction_num","");
                                                                            if (!snapshot.child(messagesenderID).child(messagereceiverID).child("Transaction_num").equals(""))
                                                                            {
                                                                                contact_ref.child(messagesenderID).child(messagereceiverID).updateChildren(map);
                                                                            }
                                                                            if (!snapshot.child(messagereceiverID).child(messagesenderID).child("Transaction_num").equals(""))
                                                                            {
                                                                                contact_ref.child(messagereceiverID).child(messagesenderID).updateChildren(map);
                                                                            }

                                                                        }

                                                                        @Override
                                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                                        }
                                                                    });
                                                                    FirebaseDatabase.getInstance().getReference("Matched_user").child(messagesenderID).child(messagereceiverID).getRef().removeValue();
                                                                    FirebaseDatabase.getInstance().getReference("Success_user").child(messagesenderID).child(messagereceiverID).getRef().removeValue();
                                                                    rankDailog.dismiss();
                                                                    Intent backto_firstpage = new Intent(getApplicationContext(), Navigation.class);
                                                                    startActivity(backto_firstpage);
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {

                                                            }
                                                        });

                                                    }
                                                });


                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                                mNotificationManager.cancelAll();
                            }


                        });
//
//                        ArrayList<String> runningactivities = new ArrayList<String>();
//
//                        ActivityManager activityManager = (ActivityManager)getBaseContext().getSystemService (Context.ACTIVITY_SERVICE);
//
//                        List<ActivityManager.RunningTaskInfo> services = activityManager.getRunningTasks(Integer.MAX_VALUE);
//
//                        for (int i1 = 0; i1 < services.size(); i1++) {
//                            runningactivities.add(0,services.get(i1).topActivity.toString());
//                        }
//                        if(runningactivities.contains("ComponentInfo{com.app/com.app.main.MyActivity}")==true){
//
//                            rankDailog.show();
//                        }

                        if (!((Activity) Chat_matchActivity.this).isFinishing()) {
//                             show popup
                            rankDailog.show();
                        }

                    }

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        matched_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(messagesenderID).child(messagereceiverID).hasChild("Decline request"))
                {
                    Log.d("check_currency_exist11","cehck_exist");
                    AlertDialog.Builder alert = new AlertDialog.Builder(Chat_matchActivity.this,R.style.AlertDialogCustom);
                    alert.setCancelable(false);
                    alert.setTitle("Notifications");
                    alert.setMessage(name_fromchat +" decline your request , Please contact this user again.");

                    alert.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Chat_matchActivity.this,ChatActivity.class);
                            intent.putExtra("name_chatact",name_fromchat);
                            intent.putExtra("other_uid_chatact",messagereceiverID);
                            intent.putExtra("transaction_number_ch",transaction_num);
                            startActivity(intent);
                            snapshot.child(messagesenderID).child(messagereceiverID).getRef().removeValue();
                            dialog.cancel();
                        }
                    });

                    if (!isFinishing()) {
                        // show popup
                        alert.show();
                    }
                }
                else {
                    if (snapshot.child(messagereceiverID).child(messagesenderID).exists()) {

//                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
                    currency_ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot1) {
                            if (snapshot1.exists()) {
                                if (snapshot1.child(transaction_num).exists()) {
                                    if (snapshot1.child(transaction_num).child("matched").getValue().equals("no")) {
                                        if (!snapshot.child(messagesenderID).child(messagereceiverID).hasChild("Decline request")) {
                                            if (snapshot1.child(transaction_num).child("uid").getValue().equals(messagesenderID)) {
                                                Log.d("check_currency_exist", snapshot1.child(transaction_num).child("uid").getValue().toString());
                                                AlertDialog.Builder alert = new AlertDialog.Builder(Chat_matchActivity.this);
                                                alert.setCancelable(false);
                                                alert.setTitle("Notification");
                                                alert.setMessage("You edit the transaction. Please contact to the user that you are already match again");

                                                alert.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        Intent intent = new Intent(Chat_matchActivity.this, ChatActivity.class);
                                                        intent.putExtra("name_chatact", name_fromchat);
                                                        intent.putExtra("other_uid_chatact", messagereceiverID);
                                                        intent.putExtra("transaction_number_ch", transaction_num);
                                                        startActivity(intent);

                                                        FirebaseDatabase.getInstance().getReference("Users").child(messagesenderID).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                if (snapshot.hasChild("image")) {
                                                                    String image = snapshot.child("image").getValue().toString();
                                                                    String name = snapshot.child("firstname").getValue().toString() + " " + snapshot.child("lastname").getValue().toString();
                                                                    getToken(name + " has edit the transaction.Please contact the user again.", messagesenderID, image, messagereceiverID, name);
                                                                } else if (!snapshot.hasChild("image")) {
                                                                    String image = "";
                                                                    String name = snapshot.child("firstname").getValue().toString() + " " + snapshot.child("lastname").getValue().toString();
                                                                    getToken(name + " has edit the transaction.Please contact the user again.", messagesenderID, image, messagereceiverID, name);
                                                                }

                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {

                                                            }
                                                        });

                                                        FirebaseDatabase.getInstance().getReference("Matched_user").child(messagesenderID).child(messagereceiverID).getRef().removeValue();
                                                        FirebaseDatabase.getInstance().getReference("Matched_user").child(messagereceiverID).child(messagesenderID).getRef().removeValue();

                                                        HashMap map = new HashMap();
                                                        map.put("transaction_edit", "yes");
                                                        contact_ref.child(messagereceiverID).child(messagesenderID).updateChildren(map);
                                                        dialog.cancel();
                                                    }
                                                });

                                                if (!isFinishing()) {
                                                    // show popup
                                                    alert.show();
                                                }
                                            } else {
                                                AlertDialog.Builder alert = new AlertDialog.Builder(Chat_matchActivity.this);
                                                alert.setCancelable(false);
                                                alert.setTitle("Notification");
                                                alert.setMessage(name_fromchat + " has already edit the transaction. Please contact to the user that you are already match again");

                                                alert.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        Intent intent = new Intent(Chat_matchActivity.this, ChatActivity.class);
                                                        intent.putExtra("name_chatact", name_fromchat);
                                                        intent.putExtra("other_uid_chatact", messagereceiverID);
                                                        intent.putExtra("transaction_number_ch", transaction_num);
                                                        startActivity(intent);

                                                        FirebaseDatabase.getInstance().getReference("Matched_user").child(messagesenderID).child(messagereceiverID).getRef().removeValue();
                                                        FirebaseDatabase.getInstance().getReference("Matched_user").child(messagereceiverID).child(messagesenderID).getRef().removeValue();
                                                        dialog.cancel();
                                                    }
                                                });

                                                if (!isFinishing()) {
                                                    // show popup
                                                    alert.show();
                                                }
                                            }

                                        }
                                    }
                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
//                        }
//                    },3000);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE )
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageuri = result.getUri();

                String messageSenderRef = "Messages/"+ messagesenderID + "/" + messagereceiverID;
                String messageReceiverRef = "Messages/"+ messagereceiverID + "/" + messagesenderID;

                DatabaseReference userMessagekeyRef = rootRef.child("Messages").child(messagesenderID).child(messagereceiverID).push();
                String messagePushID = userMessagekeyRef.getKey();
                final StorageReference fileRef = firebaseStorage.child(messagePushID+"."+"jpg");

                uploadTask = fileRef.putFile(imageuri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful())
                        {
                            throw task.getException();
                        }
                        return fileRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        if (task.isSuccessful())
                        {
                            Uri downloadUri = task.getResult();
                            myUri = downloadUri.toString();

                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                            String time = sdf.format(new Date());

                            SimpleDateFormat date = new SimpleDateFormat("dd MMM yyyy");
                            String date_for = date.format(new Date());

                            Map mesageTextBody = new HashMap();
                            mesageTextBody.put("message",myUri);
                            mesageTextBody.put("name",imageuri.getLastPathSegment());
                            mesageTextBody.put("type","image");
                            mesageTextBody.put("from",messagesenderID);
                            mesageTextBody.put("time",time);
                            mesageTextBody.put("date",date_for);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID,mesageTextBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID,mesageTextBody);

                            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful())
                                    {
                                        Toast.makeText(Chat_matchActivity.this,"Message Sent Succesfully ",Toast.LENGTH_SHORT).show();

                                        Calendar calendar = Calendar.getInstance();
                                        //Returns current time in millis
                                        long timeMilli = calendar.getTimeInMillis();
                                        HashMap map = new HashMap();
                                        map.put("Timestamp",timeMilli);
                                        contact_ref.child(messagesenderID).child(messagereceiverID).updateChildren(map);
                                        contact_ref.child(messagereceiverID).child(messagesenderID).updateChildren(map);

                                        FirebaseDatabase.getInstance().getReference("Users").child(messagesenderID).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.hasChild("image")){
                                                    String image = snapshot.child("image").getValue().toString();
                                                    String name = snapshot.child("firstname").getValue().toString()+" "+snapshot.child("lastname").getValue().toString();
                                                    getToken("Sent Photo",messagesenderID,image,messagereceiverID,name);
                                                }
                                                else if (!snapshot.hasChild("image")){
                                                    String image = "";
                                                    String name = snapshot.child("firstname").getValue().toString()+" "+snapshot.child("lastname").getValue().toString();

                                                    getToken("Sent Photo",messagesenderID,image,messagereceiverID,name);
                                                }

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                    else
                                    {
                                        Toast.makeText(Chat_matchActivity.this,"Error",Toast.LENGTH_SHORT).show();
                                    }

                                    MessageInputText.setText("");

                                }
                            });
                        }
                    }
                });

            }

        }
        else
        {
            Toast.makeText(this,"Error, Try again",Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(getApplicationContext(), "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getCurrentLocation() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        Log.d("latitude","checck");

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            LocationServices.getFusedLocationProviderClient(Chat_matchActivity.this).requestLocationUpdates(locationRequest, new LocationCallback(){

                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    LocationServices.getFusedLocationProviderClient(Chat_matchActivity.this).removeLocationUpdates(this);
                    if (locationResult!=null &&locationResult.getLocations().size()>0)
                    {
                        int latestLocationIndex = locationResult.getLocations().size()-1;
                        double latitude_value = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                        double longitude_value = locationResult.getLocations().get(latestLocationIndex).getLongitude();
                        latitude = String.valueOf(latitude_value);
                        longitude = String.valueOf(longitude_value);

                        String googlemap_link = "https://www.google.com/maps/search/?api=1&query="+latitude+","+longitude;
                        sendMessage(googlemap_link);


//                        FirebaseDatabase.getInstance().getReference("Users").child(messagesenderID).addValueEventListener(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                if (snapshot.hasChild("image")){
//                                    String image = snapshot.child("image").getValue().toString();
//                                    String name = snapshot.child("firstname").getValue().toString()+" "+snapshot.child("lastname").getValue().toString();
//                                    getToken(googlemap_link,messagesenderID,image,messagereceiverID,name);
//                                }
//                                else if (!snapshot.hasChild("image")){
//                                    String image = "";
//                                    String name = snapshot.child("firstname").getValue().toString()+" "+snapshot.child("lastname").getValue().toString();
//                                    getToken(googlemap_link,messagesenderID,image,messagereceiverID,name);
//                                }
//
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError error) {
//
//                            }
//                        });
                    }
                }

            }, Looper.getMainLooper());
        }
    }

    private void getToken(String message, String senderID, String senderImage, String receiverID, String sendername) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(messagereceiverID);
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
        request.setRetryPolicy(new DefaultRetryPolicy(100,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(request);
    }

    private void sendMessage(String messageText) {

        //Encryption message part
        try {
            cipher = Cipher.getInstance("AES");
            deciper = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        secretKeySpec  = new SecretKeySpec(encryptionKey,"AES");

        if (TextUtils.isEmpty(messageText))
        {

        }
        else
        {
            String messageSenderRef = "Messages/"+ messagesenderID + "/" + messagereceiverID;
            String messageReceiverRef = "Messages/"+ messagereceiverID + "/" + messagesenderID;

            DatabaseReference userMessagekeyRef = rootRef.child("Messages").child(messagesenderID).child(messagereceiverID).push();
            String messagePushID = userMessagekeyRef.getKey();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            String time = sdf.format(new Date());

            SimpleDateFormat date = new SimpleDateFormat("dd MMM yyyy");
            String date_for = date.format(new Date());

            Map mesageTextBody = new HashMap();
            mesageTextBody.put("message",AESEncryptionMethod(messageText));
            mesageTextBody.put("type","text");
            mesageTextBody.put("from",messagesenderID);
            mesageTextBody.put("time",time);
            mesageTextBody.put("date",date_for);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID,mesageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID,mesageTextBody);

            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful())
                    {
                        Toast.makeText(Chat_matchActivity.this,"Message Sent Succesfully ",Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(Chat_matchActivity.this,"Error",Toast.LENGTH_SHORT).show();
                    }

                    MessageInputText.setText("");

                }
            });

            FirebaseDatabase.getInstance().getReference("Users").child(messagesenderID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.hasChild("image"))
                    {
                        String image = snapshot.child("image").getValue().toString();
                        String name = snapshot.child("firstname").getValue().toString()+" "+snapshot.child("lastname").getValue().toString();
                        getToken(messageText,messagesenderID,image,messagereceiverID,name);
                    }
                    else if (!snapshot.hasChild("image")){
                        String image = "";
                        String name = snapshot.child("firstname").getValue().toString()+" "+snapshot.child("lastname").getValue().toString();

                        getToken(messageText,messagesenderID,image,messagereceiverID,name);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            Calendar calendar = Calendar.getInstance();
            //Returns current time in millis
            long timeMilli = calendar.getTimeInMillis();
            HashMap map = new HashMap();
            map.put("Timestamp",timeMilli);
            contact_ref.child(messagesenderID).child(messagereceiverID).updateChildren(map);
            contact_ref.child(messagereceiverID).child(messagesenderID).updateChildren(map);
        }
    }

    private String AESEncryptionMethod(String string)
    {
        byte[] stringBytes = string.getBytes();
        byte[] encryptionByte = new byte[stringBytes.length];

        try {
            cipher.init(Cipher.ENCRYPT_MODE,secretKeySpec);
            encryptionByte = cipher.doFinal(stringBytes);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        String returnString = null;
        try {
            returnString = new String(encryptionByte,"ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return  returnString;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), Direct_Message.class);
        startActivityForResult(myIntent, 0);
        return true;
    }

}