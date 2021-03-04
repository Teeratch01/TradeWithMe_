package com.example.TradewithMe;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.provider.ContactsContract;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.dynamic.IFragmentWrapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{

    private List<Messages> userMessageList;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference userRef;
    private String previous_date="";
    private Context context;

    public MessageAdapter(List<Messages> userMessageList,Context context)
    {
        this.userMessageList = userMessageList;
        this.context = context;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderMessageText,receiverMessageText,timesender,timereceiver,timeimagesender,timeimagereceiver,date_in_chat;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture,messageReceiverImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMessageText = itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
            timesender=itemView.findViewById(R.id.time_sender_message_text);
            timereceiver=itemView.findViewById(R.id.time_receiver_message_text);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);
            messageReceiverImage = itemView.findViewById(R.id.message_receiver_image_view);
            timeimagesender = itemView.findViewById(R.id.time_sender_image_text);
            timeimagereceiver = itemView.findViewById(R.id.time_receiver_image_text);
            date_in_chat = itemView.findViewById(R.id.date_text);

        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_message_layout,parent,false);
        firebaseAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        String messageSenderID = firebaseAuth.getCurrentUser().getUid();
        Messages messages = userMessageList.get(position);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();
        String fromdate = messages.getDate();


        userRef = FirebaseDatabase.getInstance().getReference("Users").child(fromUserID);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("image"))
                {
                    String receiverImage = snapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile).into(holder.receiverProfileImage);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        holder.receiverMessageText.setVisibility(View.GONE);
        holder.receiverProfileImage.setVisibility(View.GONE);
        holder.senderMessageText.setVisibility(View.GONE);
        holder.messageSenderPicture.setVisibility(View.GONE);
        holder.messageReceiverImage.setVisibility(View.GONE);
        holder.timesender.setVisibility(View.GONE);
        holder.timereceiver.setVisibility(View.GONE);
        holder.timeimagesender.setVisibility(View.GONE);
        holder.timeimagereceiver.setVisibility(View.GONE);
        holder.date_in_chat.setVisibility(View.GONE);




        if (fromMessageType.equals("text"))
        {
//            holder.receiverMessageText.setVisibility(View.INVISIBLE);
//            holder.receiverProfileImage.setVisibility(View.INVISIBLE);
//            holder.senderMessageText.setVisibility(View.INVISIBLE);
//            holder.timesender.setVisibility(View.INVISIBLE);
//            holder.timereceiver.setVisibility(View.INVISIBLE);

            if (fromUserID.equals(messageSenderID))
            {
                holder.senderMessageText.setVisibility(View.VISIBLE);
                holder.timesender.setVisibility(View.VISIBLE);
//                holder.senderMessageText.setBackgroundResource(R.drawable.sender_message_layout);
                holder.senderMessageText.setTextColor(Color.WHITE);
                holder.senderMessageText.setText(messages.getMessage());
                holder.timesender.setText(messages.getTime());

//                if (!previous_date.equals(fromdate)|| previous_date.equals(""))
//                {
//                    holder.date_in_chat.setVisibility(View.VISIBLE);
//                    holder.date_in_chat.setText(fromdate);
//                    previous_date = fromdate;
//                }
//                else
//                {
//                    holder.date_in_chat.setVisibility(View.GONE);
//                }




//                holder.last_text.setText(messages.getMessage());
            }
            else {

                holder.receiverMessageText.setVisibility(View.VISIBLE);
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.timereceiver.setVisibility(View.VISIBLE);
//                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_layout);
                holder.receiverMessageText.setTextColor(Color.BLACK);
                holder.receiverMessageText.setText(messages.getMessage());
                holder.timereceiver.setText(messages.getTime());
//                holder.last_text.setText(messages.getMessage());
//                if (!previous_date.equals(fromdate)|| previous_date.equals(""))
//                {
//                    holder.date_in_chat.setVisibility(View.VISIBLE);
//                    holder.date_in_chat.setText(fromdate);
//                    previous_date = fromdate;
//                }
//                else
//                {
//                    holder.date_in_chat.setVisibility(View.GONE);
//                }

            }
        }
        else if (fromMessageType.equals("image"))
        {
            if (fromUserID.equals(messageSenderID))
            {
                holder.messageSenderPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.messageSenderPicture);
                holder.messageSenderPicture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent pic_intent = new Intent(context,Illustrate_pic.class);
                        pic_intent.putExtra("pic_illustrate",messages.getMessage());
                        v.getContext().startActivity(pic_intent);
                    }
                });

                holder.timeimagesender.setVisibility(View.VISIBLE);
                holder.timeimagesender.setText(messages.getTime());
//                if (!previous_date.equals(fromdate)|| previous_date.equals(""))
//                {
//                    holder.date_in_chat.setVisibility(View.VISIBLE);
//                    holder.date_in_chat.setText(fromdate);
//                    previous_date = fromdate;
//                }
//                else
//                {
//                    holder.date_in_chat.setVisibility(View.GONE);
//                }

            }
            else {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.messageReceiverImage.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).into(holder.messageReceiverImage);

                holder.messageReceiverImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent pic_intent = new Intent(context,Illustrate_pic.class);
                        pic_intent.putExtra("pic_illustrate",messages.getMessage());
                        v.getContext().startActivity(pic_intent);
                    }
                });

                holder.timeimagereceiver.setVisibility(View.VISIBLE);
                holder.timeimagereceiver.setText(messages.getTime());

//                if (!previous_date.equals(fromdate)|| previous_date.equals(""))
//                {
//                    holder.date_in_chat.setVisibility(View.VISIBLE);
//                    holder.date_in_chat.setText(fromdate);
//                    previous_date = fromdate;
//                }
//                else
//                {
//                    holder.date_in_chat.setVisibility(View.GONE);
//                }

            }

        }
    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }


}
