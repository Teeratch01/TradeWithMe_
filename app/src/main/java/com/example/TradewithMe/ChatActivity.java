package com.example.TradewithMe;

import android.Manifest;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    TextView name_chatact,time_chatact;
    String name_from_otheract,transaction_num;
    Toolbar ChatToolBar;
    ImageButton SendMessageButton,SendLocationButton,SendImageButton;
    EditText MessageInputText;
    FirebaseAuth firebaseAuth;
    String messageSenderID,messageReceiverID,last_message;
    DatabaseReference rootRef,matchuser_ref,record_ref,currency_ref,contact_ref;
    List<Messages> messagesList = new ArrayList<>();
    LinearLayoutManager linearLayoutManager;
    MessageAdapter messageAdapter;
    RecyclerView userMessageList;
    FusedLocationProviderClient fusedLocationProviderClient;
    String latitude,longitude;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    Uri imageuri;
    StorageReference firebaseStorage;
    StorageTask uploadTask;
    String myUri = "";
    Button confirm_btn;
    long maxIdsender,maxIdreceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().hide();


        //Setname
        name_from_otheract = getIntent().getExtras().get("name_chatact").toString();

        name_chatact = findViewById(R.id.name_viachat);
        name_chatact.setText(name_from_otheract);


        //Time
        time_chatact = findViewById(R.id.time_viachat);
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
                                time_chatact.setText(datastring);
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
        SendMessageButton = findViewById(R.id.sent_message);
        MessageInputText = findViewById(R.id.input_message);
        firebaseAuth = FirebaseAuth.getInstance();
        messageSenderID = firebaseAuth.getCurrentUser().getUid();
        messageReceiverID = getIntent().getExtras().get("other_uid_chatact").toString();
        transaction_num = getIntent().getExtras().get("transaction_number_ch").toString();


        Log.d("check_transac_1",transaction_num);
        rootRef = FirebaseDatabase.getInstance().getReference();
        SendLocationButton = findViewById(R.id.sent_location);
        SendImageButton=findViewById(R.id.sent_image);
        firebaseStorage = FirebaseStorage.getInstance().getReference().child("Message Pic");

        messageAdapter = new MessageAdapter(messagesList,getApplicationContext());
        userMessageList = findViewById(R.id.private_message_list_user);
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

        //Send location Button
        SendLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
                } else {
                    getCurrentLocation();

                }

            }
        });

        //Send Image
        SendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().start(ChatActivity.this);
//                FirebaseDatabase.getInstance().getReference("Users").child(messageSenderID).addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        if (snapshot.hasChild("image")){
//                            String image = snapshot.child("image").getValue().toString();
//                            String name = snapshot.child("firstname").getValue().toString()+" "+snapshot.child("lastname").getValue().toString();
//                            getToken("Sent Photo",messageSenderID,image,messageReceiverID,name);
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
            }
        });



        //Add Message in RecyclerView
        rootRef.child("Messages").child(messageSenderID).child(messageReceiverID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Messages messages=  snapshot.getValue(Messages.class);

                messagesList.add(messages);
                messageAdapter.notifyDataSetChanged();

                userMessageList.smoothScrollToPosition(userMessageList.getAdapter().getItemCount());

                last_message = messages.getMessage();

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

        SimpleDateFormat date = new SimpleDateFormat("dd MMM yyyy");
        String date_for = date.format(new Date());
        Log.d("check_date",date_for);

        //Match with ther user
        confirm_btn = findViewById(R.id.confirm_button_match);
        record_ref =  FirebaseDatabase.getInstance().getReference("Record");
        matchuser_ref = FirebaseDatabase.getInstance().getReference("Matched_user");
        currency_ref = FirebaseDatabase.getInstance().getReference("Currency");
        contact_ref = FirebaseDatabase.getInstance().getReference("Contacts");
        record_ref.child(messageSenderID).child("Matched").addValueEventListener(new ValueEventListener() {
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

        record_ref.child(messageReceiverID).child("Matched").addValueEventListener(new ValueEventListener() {
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
        confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String match = "yes";
                Matched_user setValue = new Matched_user(
                  match
                );

                if (transaction_num.equals("")||transaction_num ==null)
                {
                    AlertDialog.Builder alert = new AlertDialog.Builder(ChatActivity.this);

                    alert.setCancelable(false);
                    alert.setTitle("Notification");
                    alert.setMessage("Please select the currency that you want to exchange with this exchanger before match");
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


                    matchuser_ref.child(messageSenderID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (!snapshot.hasChild(messageReceiverID)) {
                                matchuser_ref.child(messageSenderID).child(messageReceiverID).setValue(setValue).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        AlertDialog.Builder alert = new AlertDialog.Builder(ChatActivity.this);

                                        alert.setCancelable(false);
                                        alert.setTitle("Congratulations");
                                        alert.setMessage(Html.fromHtml("You are already match<br><font color='#FF0000'>*Suggestion: You should meet each other in the public ex. supermarket, coffee shop</font>"));

                                        alert.setNegativeButton("Back", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                                FirebaseDatabase.getInstance().getReference("Users").child(messageSenderID).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if (snapshot.hasChild("image")) {
                                                            String image = snapshot.child("image").getValue().toString();
                                                            String name = snapshot.child("firstname").getValue().toString() + " " + snapshot.child("lastname").getValue().toString();
                                                            getToken("The exchanger want to match with you , Do you want to accept?", messageSenderID, image, messageReceiverID, name);
                                                        }
                                                        else if (!snapshot.hasChild("image")){
                                                            String image = "";
                                                            String name = snapshot.child("firstname").getValue().toString()+" "+snapshot.child("lastname").getValue().toString();
                                                            getToken("The exchanger want to match with you , Do you want to accept?",messageSenderID,image,messageReceiverID,name);
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                                Intent start_chatmatch = new Intent(getApplicationContext(), Chat_matchActivity.class);
                                                start_chatmatch.putExtra("name_chatact", name_from_otheract);
                                                start_chatmatch.putExtra("other_uid_chatact", messageReceiverID);
                                                start_chatmatch.putExtra("transaction_number_ch", transaction_num);
                                                startActivity(start_chatmatch);
                                            }
                                        });
                                        if (!isFinishing()) {
                                            // show popup
                                            alert.show();
                                        }
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


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE )
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageuri = result.getUri();

                String messageSenderRef = "Messages/"+ messageSenderID + "/" + messageReceiverID;
                String messageReceiverRef = "Messages/"+ messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessagekeyRef = rootRef.child("Messages").child(messageSenderID).child(messageReceiverID).push();
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
                            mesageTextBody.put("from",messageSenderID);
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
                                        Toast.makeText(ChatActivity.this,"Message Sent Succesfully ",Toast.LENGTH_SHORT).show();

                                        Calendar calendar = Calendar.getInstance();
                                        //Returns current time in millis
                                        long timeMilli = calendar.getTimeInMillis();
                                        HashMap map = new HashMap();
                                        map.put("Timestamp",timeMilli);
                                        contact_ref.child(messageSenderID).child(messageReceiverID).updateChildren(map);
                                        contact_ref.child(messageReceiverID).child(messageSenderID).updateChildren(map);

                                        FirebaseDatabase.getInstance().getReference("Users").child(messageSenderID).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.hasChild("image")){
                                                    String image = snapshot.child("image").getValue().toString();
                                                    String name = snapshot.child("firstname").getValue().toString()+" "+snapshot.child("lastname").getValue().toString();
                                                    getToken("Sent Photo",messageSenderID,image,messageReceiverID,name);
                                                }
                                                else if (!snapshot.hasChild("image")){
                                                    String image = "";
                                                    String name = snapshot.child("firstname").getValue().toString()+" "+snapshot.child("lastname").getValue().toString();
                                                    getToken("Sent Photo",messageSenderID,image,messageReceiverID,name);
                                                }

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                    else
                                    {
                                        Toast.makeText(ChatActivity.this,"Error",Toast.LENGTH_SHORT).show();
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
            LocationServices.getFusedLocationProviderClient(ChatActivity.this).requestLocationUpdates(locationRequest, new LocationCallback(){

                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    LocationServices.getFusedLocationProviderClient(ChatActivity.this).removeLocationUpdates(this);
                    if (locationResult!=null &&locationResult.getLocations().size()>0)
                    {
                        int latestLocationIndex = locationResult.getLocations().size()-1;
                        double latitude_value = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                        double longitude_value = locationResult.getLocations().get(latestLocationIndex).getLongitude();
                        latitude = String.valueOf(latitude_value);
                        longitude = String.valueOf(longitude_value);

                        String googlemap_link = "https://www.google.com/maps/search/?api=1&query="+latitude+","+longitude;
                        sendMessage(googlemap_link);

//                        FirebaseDatabase.getInstance().getReference("Users").child(messageSenderID).addValueEventListener(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                if (snapshot.hasChild("image")){
//                                    String image = snapshot.child("image").getValue().toString();
//                                    String name = snapshot.child("firstname").getValue().toString()+" "+snapshot.child("lastname").getValue().toString();
//                                    getToken(googlemap_link,messageSenderID,image,messageReceiverID,name);
//                                }
//                                else if (!snapshot.hasChild("image")){
//                                    String image = "";
//                                    String name = snapshot.child("firstname").getValue().toString()+" "+snapshot.child("lastname").getValue().toString();
//                                    getToken(googlemap_link,messageSenderID,image,messageReceiverID,name);
//                                }
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

    private void sendMessage(String messageText) {

        if (TextUtils.isEmpty(messageText))
        {

        }
        else {
            String messageSenderRef = "Messages/"+ messageSenderID + "/" + messageReceiverID;
            String messageReceiverRef = "Messages/"+ messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessagekeyRef = rootRef.child("Messages").child(messageSenderID).child(messageReceiverID).push();
            String messagePushID = userMessagekeyRef.getKey();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            String time = sdf.format(new Date());

            SimpleDateFormat date = new SimpleDateFormat("dd MMM yyyy");
            String date_for = date.format(new Date());

            Map mesageTextBody = new HashMap();
            mesageTextBody.put("message",messageText);
            mesageTextBody.put("type","text");
            mesageTextBody.put("from",messageSenderID);
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
                        Toast.makeText(ChatActivity.this,"Message Sent Succesfully ",Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(ChatActivity.this,"Error",Toast.LENGTH_SHORT).show();
                    }

                    MessageInputText.setText("");

                }
            });

            FirebaseDatabase.getInstance().getReference("Users").child(messageSenderID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.hasChild("image")){
                        String image = snapshot.child("image").getValue().toString();
                        String name = snapshot.child("firstname").getValue().toString()+" "+snapshot.child("lastname").getValue().toString();
                        getToken(messageText,messageSenderID,image,messageReceiverID,name);
                    }
                    else if (!snapshot.hasChild("image")){
                        String image = "";
                        String name = snapshot.child("firstname").getValue().toString()+" "+snapshot.child("lastname").getValue().toString();
                        getToken(messageText,messageSenderID,image,messageReceiverID,name);
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
            contact_ref.child(messageSenderID).child(messageReceiverID).updateChildren(map);
            contact_ref.child(messageReceiverID).child(messageSenderID).updateChildren(map);

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        String match = "yes";
        Matched_user setValue = new Matched_user(
                match
        );

        String status = "Ok";

        SimpleDateFormat date = new SimpleDateFormat("dd MMM yyyy");
        String date_for = date.format(new Date());

        Record setValuesender = new Record(status,messageReceiverID,date_for);
        Record setValuereceiver = new Record(status,messageSenderID,date_for);

        matchuser_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(messageReceiverID))
                {
                    if (snapshot.child(messageReceiverID).hasChild(messageSenderID))
                    {
                        if (!snapshot.child(messageSenderID).child(messageReceiverID).exists())
                        {
                            if (!snapshot.child(messageReceiverID).child(messageSenderID).hasChild("Decline request")) {

                                AlertDialog.Builder alert = new AlertDialog.Builder(ChatActivity.this, R.style.AlertDialogCustom);
                                alert.setCancelable(false);
                                alert.setTitle("Notifications");
                                alert.setMessage(Html.fromHtml("Do you want to match with this user?<br><font color='#FF0000'>*Suggestion: If you match with this user , You should meet each other in the public ex. supermarket, coffee shop</font>"));
                                alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        FirebaseDatabase.getInstance().getReference("Users").child(messageSenderID).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.hasChild("image")) {
                                                    String image = snapshot.child("image").getValue().toString();
                                                    String name = snapshot.child("firstname").getValue().toString() + " " + snapshot.child("lastname").getValue().toString();

                                                    HashMap map = new HashMap();
                                                    map.put("Decline request", "yes");
                                                    FirebaseDatabase.getInstance().getReference("Matched_user").child(messageReceiverID).child(messageSenderID).updateChildren(map);
                                                    getToken("The exchanger decline your request,please contact again.", messageSenderID, image, messageReceiverID, name);
                                                }
                                                else if (!snapshot.hasChild("image")){
                                                    String image = "";
                                                    String name = snapshot.child("firstname").getValue().toString()+" "+snapshot.child("lastname").getValue().toString();

                                                    HashMap map = new HashMap();
                                                    map.put("Decline request", "yes");
                                                    FirebaseDatabase.getInstance().getReference("Matched_user").child(messageReceiverID).child(messageSenderID).updateChildren(map);
                                                    getToken("The exchanger decline your request,please contact again.",messageSenderID,image,messageReceiverID,name);
                                                }

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                                        dialog.cancel();
                                    }
                                });

                                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        matchuser_ref.child(messageSenderID).child(messageReceiverID).setValue(setValue).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                record_ref.child(messageSenderID).child("Matched").child(String.valueOf(maxIdsender + 1)).setValue(setValuesender);
                                                record_ref.child(messageReceiverID).child("Matched").child(String.valueOf(maxIdreceiver + 1)).setValue(setValuereceiver);

                                                HashMap map = new HashMap();
                                                map.put("matched", "yes");

                                                currency_ref.child(transaction_num).updateChildren(map);

                                                Intent start_chatmatch = new Intent(getApplicationContext(), Chat_matchActivity.class);
                                                start_chatmatch.putExtra("name_chatact", name_from_otheract);
                                                start_chatmatch.putExtra("other_uid_chatact", messageReceiverID);
                                                start_chatmatch.putExtra("transaction_number_ch", transaction_num);
                                                startActivity(start_chatmatch);
                                                dialog.cancel();
                                            }
                                        });
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
                else if (snapshot.hasChild(messageSenderID))
                {
                    if (snapshot.child(messageSenderID).hasChild(messageReceiverID))
                    {
                        if (snapshot.child(messageSenderID).child(messageReceiverID).hasChild("Decline request"))
                        {
                            AlertDialog.Builder alert = new AlertDialog.Builder(ChatActivity.this,R.style.AlertDialogCustom);
                            alert.setCancelable(false);
                            alert.setTitle("Notifications");
                            alert.setMessage(name_from_otheract +" decline your request , Please contact this user again.");

                            alert.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    snapshot.child(messageSenderID).child(messageReceiverID).getRef().removeValue();
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

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void getToken(String message, String senderID, String senderImage, String receiverID, String sendername)
    {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(messageReceiverID);
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

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,"https://fcm.googleapis.com/fcm/send",to,response -> {

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

    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), Direct_Message.class);
        startActivityForResult(myIntent, 0);
        return true;
    }

}