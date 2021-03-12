package com.example.TradewithMe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class Feedback extends AppCompatActivity {

    private RecyclerView feedback_recyclerview;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference,user_ref;
    private String uid_user;
    private FirebaseRecyclerOptions options;
    private FirebaseRecyclerAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        feedback_recyclerview  = findViewById(R.id.feedback_recyclerview);
        feedback_recyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        uid_user = getIntent().getExtras().get("feedback_id").toString();
        databaseReference = FirebaseDatabase.getInstance().getReference("Feedback").child(uid_user);
        user_ref = FirebaseDatabase.getInstance().getReference("Users");

        feedback_list();

    }

    private void feedback_list() {

        options = new FirebaseRecyclerOptions.Builder<Record_success>().setQuery(databaseReference,Record_success.class).build();
        adapter = new FirebaseRecyclerAdapter<Record_success,FeedbackViewholder>(options) {

            @NonNull
            @Override
            public FeedbackViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.feedback_list,parent,false);
                return new FeedbackViewholder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull FeedbackViewholder holder, int position, @NonNull Record_success model) {
                String rating = model.getRating();
                String comment = model.getComment();
                String uid = model.getUid();
                String date = model.getDate();

                user_ref.child(uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists())
                        {
                            if (snapshot.hasChild("image"))
                            {
                                String image = "";
                                if (snapshot.hasChild("image"))
                                {
                                    image = snapshot.child("image").getValue().toString();
                                }

                                String name = snapshot.child("firstname").getValue().toString();
                                Log.d("check_name",name);
                                String lastname = snapshot.child("lastname").getValue().toString();
                                String fullname = name+" "+lastname;

                                holder.setDetail(fullname,"Rating : "+rating,"Comment : "+comment,date,image);
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        };

        adapter.startListening();
        adapter.notifyDataSetChanged();
        if (adapter != null)
        {
            feedback_recyclerview.setAdapter(adapter);
        }
        feedback_recyclerview.smoothScrollToPosition(feedback_recyclerview.getAdapter().getItemCount());
    }

    public class FeedbackViewholder extends RecyclerView.ViewHolder
    {

        View mview;
        public FeedbackViewholder(@NonNull View itemView) {
            super(itemView);
            mview = itemView;
        }
        public void setDetail(String name_ill,String rating_ill,String comment_ill,String date_ill,String image_ill)
        {
            TextView name = mview.findViewById(R.id.feedback_name);
            TextView rating = mview.findViewById(R.id.feedback_rating);
            TextView comment = mview.findViewById(R.id.feedback_feedback);
            TextView date = mview.findViewById(R.id.feedback_date);
            CircleImageView profile_image = mview.findViewById(R.id.profile_image_feedback);

            name.setText(name_ill);
            rating.setText(rating_ill);
            comment.setText(comment_ill);
            date.setText(date_ill);
            Picasso.get().load(image_ill).into(profile_image);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}