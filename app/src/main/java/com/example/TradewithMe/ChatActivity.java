package com.example.TradewithMe;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    TextView name_chatact,time_chatact;
    String name_from_otheract;
    Toolbar ChatToolBar;
    ImageButton SendMessageButton;
    EditText MessageInputText;
    FirebaseAuth firebaseAuth;
    String messageSenderID,messageReceiverID;
    DatabaseReference rootRef;
    List<Messages> messagesList = new ArrayList<>();
    LinearLayoutManager linearLayoutManager;
    MessageAdapter messageAdapter;
    RecyclerView userMessageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
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
        rootRef = FirebaseDatabase.getInstance().getReference();

        messageAdapter = new MessageAdapter(messagesList);
        userMessageList = findViewById(R.id.private_message_list_user);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessageList.setLayoutManager(linearLayoutManager);
        userMessageList.setAdapter(messageAdapter);


        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });


    }

    private void sendMessage() {
        String messageText = MessageInputText.getText().toString();
        if (TextUtils.isEmpty(messageText))
        {

        }
        else {
            String messageSenderRef = "Messages/"+ messageSenderID + "/" + messageReceiverID;
            String messageReceiverRef = "Messages/"+ messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessagekeyRef = rootRef.child("Messages").child(messageSenderID).child(messageReceiverID).push();
            String messagePushID = userMessagekeyRef.getKey();

            Map mesageTextBody = new HashMap();
            mesageTextBody.put("message",messageText);
            mesageTextBody.put("type","text");
            mesageTextBody.put("from",messageSenderID);

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
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        rootRef.child("Messages").child(messageSenderID).child(messageReceiverID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Messages messages=  snapshot.getValue(Messages.class);

                messagesList.add(messages);
                messageAdapter.notifyDataSetChanged();
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
    }
}