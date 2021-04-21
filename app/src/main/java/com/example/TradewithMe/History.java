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


public class History extends Fragment {

    private RecyclerView history_recycler;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference,userrefference;
    private String uid_current_user;
    private FirebaseRecyclerOptions options;
    private FirebaseRecyclerAdapter adapter;
    private LinearLayoutManager mLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view;
        view =inflater.inflate(R.layout.fragment_history, container, false);

        history_recycler = view.findViewById(R.id.history_recyclerView);
        mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        history_recycler.setLayoutManager(mLayoutManager);

        firebaseAuth = FirebaseAuth.getInstance();
        uid_current_user = firebaseAuth.getCurrentUser().getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference("Record").child(uid_current_user).child("Success");
        userrefference = FirebaseDatabase.getInstance().getReference("Users");

        history_list();
        return view;
    }

    private void history_list() {
        options = new FirebaseRecyclerOptions.Builder<Record>().setQuery(databaseReference,Record.class).build();
        adapter = new FirebaseRecyclerAdapter<Record,HistoryViewholder>(options) {

            @NonNull
            @Override
            public HistoryViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_list,parent,false);
                return new HistoryViewholder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull HistoryViewholder holder, int position, @NonNull Record model) {
                String success_user_id = model.getUid();
                String date = model.getDate();

                userrefference.child(success_user_id).addValueEventListener(new ValueEventListener() {
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
                                String detail = name+" "+lastname+" has successfully exchange with you!";

                                holder.setDetail(detail,date,image);
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
            history_recycler.setAdapter(adapter);
        }
        history_recycler.smoothScrollToPosition(history_recycler.getAdapter().getItemCount());
    }

    public class HistoryViewholder extends RecyclerView.ViewHolder
    {

        View mview;
        public HistoryViewholder(@NonNull View itemView) {
            super(itemView);
            mview = itemView;
        }
        public void setDetail(String detail_ill,String date_ill,String image_ill)
        {
            TextView detail = mview.findViewById(R.id.history_detail);
            TextView date = mview.findViewById(R.id.history_date);
            CircleImageView image = mview.findViewById(R.id.profile_image_history);

            detail.setText(detail_ill);
            date.setText(date_ill);

            if (!image_ill.isEmpty())
            {
                Picasso.get().load(image_ill).into(image);
            }

        }

    }

}