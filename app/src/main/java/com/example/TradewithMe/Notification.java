package com.example.TradewithMe;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
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


public class Notification extends Fragment {

    private RecyclerView notification_recycler;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference,userRefference;
    private String uid_currrent_user;
    private FirebaseRecyclerOptions Options;
    private FirebaseRecyclerAdapter Adapter;
    private LinearLayoutManager mLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view;
        view =inflater.inflate(R.layout.fragment_notification, container, false);

        notification_recycler = view.findViewById(R.id.notification_recyclerview);
        mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        notification_recycler.setLayoutManager(mLayoutManager);

        firebaseAuth = FirebaseAuth.getInstance();
        uid_currrent_user = firebaseAuth.getCurrentUser().getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference("Record").child(uid_currrent_user).child("Matched");
        userRefference = FirebaseDatabase.getInstance().getReference("Users");

        match_list();
        return view;
    }

    private void match_list() {
        Options = new FirebaseRecyclerOptions.Builder<Record>().setQuery(databaseReference,Record.class).build();
        Adapter = new FirebaseRecyclerAdapter<Record,MatchedViewHolder>(Options) {
            @Override
            protected void onBindViewHolder(@NonNull MatchedViewHolder holder, int position, @NonNull Record model) {

                String matched_id = model.getUid();
                String date = model.getDate();

                Log.d("check_id",matched_id);

                userRefference.child(matched_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists())
                        {
                            String image = "";
                            if (snapshot.hasChild("image"))
                            {
                                image = snapshot.child("image").getValue().toString();
                            }

                            String name = snapshot.child("firstname").getValue().toString();
                            Log.d("check_name",name);
                            String lastname = snapshot.child("lastname").getValue().toString();
                            String detail = name+" "+lastname+" has been matched with you!";

                            holder.setDetail(detail,date,image);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }


            @NonNull
            @Override
            public MatchedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.match_user_list,parent,false);
                return new MatchedViewHolder(view);
            }
        };

        Adapter.startListening();
        Adapter.notifyDataSetChanged();
        if (Adapter!=null)
        {
            notification_recycler.setAdapter(Adapter);
        }

        notification_recycler.smoothScrollToPosition(notification_recycler.getAdapter().getItemCount());
    }

    public class MatchedViewHolder extends RecyclerView.ViewHolder
    {

        View mview;
        public MatchedViewHolder(@NonNull View itemView) {
            super(itemView);
            mview=itemView;
        }

        public void setDetail(String detail_ill,String date_ill,String image_ill)
        {
            TextView detail = mview.findViewById(R.id.match_detail);
            TextView date = mview.findViewById(R.id.match_date);
            CircleImageView profile_image = mview.findViewById(R.id.profile_image_match);

            detail.setText(detail_ill);
            date.setText(date_ill);

            if (!image_ill.isEmpty())
            {
                Picasso.get().load(image_ill).into(profile_image);
            }

        }

    }

}