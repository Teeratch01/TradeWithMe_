package com.example.TradewithMe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class Direct_Message extends AppCompatActivity {

    private RecyclerView chat_list;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference reference,user_reference;
    private String uid_current_user;
    private FirebaseRecyclerOptions options;
    private FirebaseRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direct__message);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        chat_list = findViewById(R.id.chat_list);
        chat_list.setLayoutManager(new LinearLayoutManager(this));

        firebaseAuth = FirebaseAuth.getInstance();
        uid_current_user = firebaseAuth.getCurrentUser().getUid();

        reference = FirebaseDatabase.getInstance().getReference("Contacts").child(uid_current_user);
        user_reference = FirebaseDatabase.getInstance().getReference("Users");

        contact_list();

    }



    private void contact_list()
    {

        options = new FirebaseRecyclerOptions.Builder<User>().setQuery(reference,User.class).build();
        adapter = new FirebaseRecyclerAdapter<User,ContactViewHolder>(options) {
            @NonNull
            @Override
            public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_list,parent,false);

                return new ContactViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ContactViewHolder holder, int position, @NonNull User model) {

                final String userID = getRef(position).getKey();
                user_reference.child(userID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(snapshot.exists())
                        {
                            String image = "";
                            if (snapshot.hasChild("image"))
                            {
                                image = snapshot.child("image").getValue().toString();
                            }

                            String name = snapshot.child("firstname").getValue().toString();
                            String lastname = snapshot.child("lastname").getValue().toString();
//                            String last_message = getIntent().getExtras().get("last_seen_message").toString();

                            String fullname = name+" "+lastname;

                            holder.setDetail(fullname,image);

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.hasChild("Transaction_num"))
                                            {
                                                String transaction_number = snapshot.child("Transaction_num").getValue().toString();
                                                Log.d("check_transacnumber0",transaction_number);
                                                FirebaseDatabase.getInstance().getReference("Matched_user").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if (snapshot.exists())
                                                        {
                                                            if (snapshot.hasChild(uid_current_user))
                                                            {
                                                                if (snapshot.child(uid_current_user).hasChild(userID))
                                                                {
                                                                    Intent chatIntent = new Intent(getApplicationContext(),Chat_matchActivity.class);
                                                                    chatIntent.putExtra("name_chatact",fullname);
                                                                    chatIntent.putExtra("other_uid_chatact",userID);
                                                                    chatIntent.putExtra("transaction_number_ch",transaction_number);
                                                                    startActivity(chatIntent);
                                                                    Log.d("hello","hello");
                                                                }
                                                                else
                                                                {
                                                                    Intent chatIntent = new Intent(getApplicationContext(),ChatActivity.class);
                                                                    chatIntent.putExtra("name_chatact",fullname);
                                                                    chatIntent.putExtra("other_uid_chatact",userID);
                                                                    chatIntent.putExtra("transaction_number_ch",transaction_number);
                                                                    startActivity(chatIntent);
                                                                }
                                                            }
                                                            else
                                                            {
                                                                Intent chatIntent = new Intent(getApplicationContext(),ChatActivity.class);
                                                                chatIntent.putExtra("name_chatact",fullname);
                                                                chatIntent.putExtra("other_uid_chatact",userID);
                                                                chatIntent.putExtra("transaction_number_ch",transaction_number);
                                                                startActivity(chatIntent);
                                                            }
                                                        }
                                                        else
                                                        {
                                                            Intent chatIntent = new Intent(getApplicationContext(),ChatActivity.class);
                                                            chatIntent.putExtra("name_chatact",fullname);
                                                            chatIntent.putExtra("other_uid_chatact",userID);
                                                            chatIntent.putExtra("transaction_number_ch",transaction_number);
                                                            startActivity(chatIntent);
                                                        }

                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });


                                }
                            });
                        }

                        DatabaseReference chat_refference = FirebaseDatabase.getInstance().getReference("Messages");
                        chat_refference.child(uid_current_user).child(userID).addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                Messages messages=  snapshot.getValue(Messages.class);
                                if (messages.getType().equals("image"))
                                {
                                    holder.setLastMessage("Photo");
                                }
                                else{

                                    holder.setLastMessage(messages.getMessage());
                                }
                                holder.settime(messages.getTime());
                                holder.setdate(messages.getDate());

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

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }



                });


            }


        };

        adapter.startListening();
//        adapter.notifyDataSetChanged();
        adapter.notifyItemRangeChanged(0, adapter.getItemCount());
        if (adapter!=null)
        {
            chat_list.setAdapter(adapter);
        }

        chat_list.smoothScrollToPosition(chat_list.getAdapter().getItemCount());

    }

    public class ContactViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            mView =itemView;
        }

        public void setDetail(String nameill,String imageill)
        {
            TextView name =mView.findViewById(R.id.name_direct_message);
            CircleImageView image = mView.findViewById(R.id.profile_image_direct_message);


            name.setText(nameill);
            Picasso.get().load(imageill).into(image);
//            last_message.setText(last_textill);

        }
        public void setLastMessage(String lastill)
        {
            TextView last_message = mView.findViewById(R.id.last_text);
            last_message.setText(lastill);
        }
        public void settime(String lasttimeill)
        {
            TextView last_message_time = mView.findViewById(R.id.last_text_time);
            last_message_time.setText(lasttimeill);
        }

        public void setdate(String lastdateill)
        {
            TextView last_message_time = mView.findViewById(R.id.date_direct_message);
            last_message_time.setText(lastdateill);
        }



    }



}