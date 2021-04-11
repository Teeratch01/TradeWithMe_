package com.example.TradewithMe;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class FirebaseNotificationServce extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size()>0)
        {
            Map<String,String> map = remoteMessage.getData();
            String title = map.get("title");
            String message = map.get("message");
            String senderID =map.get("senderID");
            String senderImage =map.get("senderImage");
            String receiverID =map.get("receiverID");

            if (message.equals("The exchanger want to match with you , Do you want to accept?") || message.equals("The exchanger decline your request,please contact again.") || message.equals(title+" has edit the transaction.Please contact the user again."))
            {
                if (Build.VERSION.SDK_INT>Build.VERSION_CODES.O)
                {
                    createconfirmOreoNotification(title,message,senderID,senderImage,receiverID);
                }
                else
                {
                    createconfirmNormalNotificaiton(title,message,senderID,senderImage,receiverID);
                }
            }
            else if (message.equals("The transaction is successful,Please rate to the other exchanger"))
            {
                if (Build.VERSION.SDK_INT>Build.VERSION_CODES.O)
                {
                    createsuccessOreoNotification(title,message,senderID,senderImage,receiverID);
                }
                else
                {
                    createsuccessNormalNotificaiton(title,message,senderID,senderImage,receiverID);
                }
            }
            else
                {
                if (Build.VERSION.SDK_INT>Build.VERSION_CODES.O)
                {
                    createOreoNotification(title,message,senderID,senderImage,receiverID);
                }
                else
                {
                    createNormalNotificaiton(title,message,senderID,senderImage,receiverID);
                }
            }


        }
        super.onMessageReceived(remoteMessage);
    }

    @Override
    public void onNewToken(@NonNull String s) {
        updateToken(s);
        super.onNewToken(s);
    }

    private void updateToken(String token)
    {

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if ((firebaseAuth.getCurrentUser() != null))
        {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            Map<String,Object> map = new HashMap<>();
            map.put("token",token);
            databaseReference.updateChildren(map);

        }

    }

    private void createNormalNotificaiton(String title,String message,String senderID,String senderImage,String receiverID)
    {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"1000");
        builder.setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_dollar)
                .setAutoCancel(true)
                .setColor(ResourcesCompat.getColor(getResources(),R.color.purple_500,null))
                .setSound(uri);

        Intent intent = new Intent(this,Direct_Message.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

        builder.setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(new Random().nextInt(85-65),builder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createOreoNotification(String title, String message, String senderID, String senderImage, String receiverID)
    {
        NotificationChannel channel =new NotificationChannel("1000","Message",NotificationManager.IMPORTANCE_HIGH);
        channel.setShowBadge(true);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setDescription("Message Description");
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);


        Intent intent =new Intent(this,Direct_Message.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

        Notification notification = new Notification.Builder(this,"1000")
                .setContentTitle(title)
                .setContentText(message)
                .setColor(ResourcesCompat.getColor(getResources(),R.color.purple_500,null))
                .setSmallIcon(R.drawable.ic_dollar)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        manager.notify(new Random().nextInt(85-65),notification);
    }


    private void createconfirmNormalNotificaiton(String title,String message,String senderID,String senderImage,String receiverID)
    {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"1200");
        builder.setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_dollar)
                .setAutoCancel(true)
                .setColor(ResourcesCompat.getColor(getResources(),R.color.purple_500,null))

                .setSound(uri);
        
        FirebaseDatabase.getInstance().getReference("Contacts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String transaction_num  = snapshot.child(senderID).child(receiverID).child("Transaction_num").getValue().toString();
                Log.d("check_num",transaction_num);
                Intent intent =new Intent(getApplicationContext(),ChatActivity.class);
                intent.putExtra("name_chatact",title);
                intent.putExtra("other_uid_chatact",senderID);
                intent.putExtra("transaction_number_ch",transaction_num);

                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0,intent,PendingIntent.FLAG_ONE_SHOT);

                builder.setContentIntent(pendingIntent)
                        .setFullScreenIntent(pendingIntent,true);


                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                manager.notify(new Random().nextInt(85-65),builder.build());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
//        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

//        builder.setContentIntent(pendingIntent);
//
//        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        manager.notify(new Random().nextInt(85-65),builder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createconfirmOreoNotification(String title, String message, String senderID, String senderImage, String receiverID)
    {
        NotificationChannel channel =new NotificationChannel("1200","Message",NotificationManager.IMPORTANCE_HIGH);
        channel.setShowBadge(true);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setDescription("Message Description");
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);


//        Intent intent =new Intent(this,ChatActivity.class);
//        intent.putExtra("name_chatact",title);
//        intent.putExtra("other_uid_chatact",senderID);
        FirebaseDatabase.getInstance().getReference("Contacts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String transaction_num  = snapshot.child(senderID).child(receiverID).child("Transaction_num").getValue().toString();
                Log.d("check_num",transaction_num);
                Intent intent =new Intent(getApplicationContext(),ChatActivity.class);
                intent.putExtra("name_chatact",title);
                intent.putExtra("other_uid_chatact",senderID);
                intent.putExtra("transaction_number_ch",transaction_num);

                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0,intent,PendingIntent.FLAG_ONE_SHOT);

                Notification notification = new Notification.Builder(getApplicationContext(),"1000")
                        .setContentTitle(title)
                        .setContentText(message)
                        .setColor(ResourcesCompat.getColor(getResources(),R.color.purple_500,null))
                        .setSmallIcon(R.drawable.ic_dollar)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setFullScreenIntent(pendingIntent,true)
                        .build();

                manager.notify(new Random().nextInt(85-65),notification);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void createsuccessNormalNotificaiton(String title,String message,String senderID,String senderImage,String receiverID)
    {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"1300");
        builder.setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_dollar)
                .setAutoCancel(true)
                .setColor(ResourcesCompat.getColor(getResources(),R.color.purple_500,null))

                .setSound(uri);

//        Intent intent = new Intent(this,ChatActivity.class);
//        intent.putExtra("name_chatact",title);
//        intent.putExtra("other_uid_chatact",senderID);
        FirebaseDatabase.getInstance().getReference("Contacts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String transaction_num  = snapshot.child(senderID).child(receiverID).child("Transaction_num").getValue().toString();
                Log.d("check_num",transaction_num);
                Intent intent =new Intent(getApplicationContext(),Chat_matchActivity.class);
                intent.putExtra("name_chatact",title);
                intent.putExtra("other_uid_chatact",senderID);
                intent.putExtra("transaction_number_ch",transaction_num);

                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0,intent,PendingIntent.FLAG_ONE_SHOT);

                builder.setContentIntent(pendingIntent)
                        .setFullScreenIntent(pendingIntent,true);


                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                manager.notify(new Random().nextInt(85-65),builder.build());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
//        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

//        builder.setContentIntent(pendingIntent);
//
//        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        manager.notify(new Random().nextInt(85-65),builder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createsuccessOreoNotification(String title, String message, String senderID, String senderImage, String receiverID)
    {
        NotificationChannel channel =new NotificationChannel("1300","Message",NotificationManager.IMPORTANCE_HIGH);
        channel.setShowBadge(true);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setDescription("Message Description");
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);


//        Intent intent =new Intent(this,ChatActivity.class);
//        intent.putExtra("name_chatact",title);
//        intent.putExtra("other_uid_chatact",senderID);
        FirebaseDatabase.getInstance().getReference("Contacts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String transaction_num  = snapshot.child(senderID).child(receiverID).child("Transaction_num").getValue().toString();
                Log.d("check_num",transaction_num);
                Intent intent =new Intent(getApplicationContext(),Chat_matchActivity.class);
                intent.putExtra("name_chatact",title);
                intent.putExtra("other_uid_chatact",senderID);
                intent.putExtra("transaction_number_ch",transaction_num);

                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0,intent,PendingIntent.FLAG_ONE_SHOT);

                Notification notification = new Notification.Builder(getApplicationContext(),"1000")
                        .setContentTitle(title)
                        .setContentText(message)
                        .setColor(ResourcesCompat.getColor(getResources(),R.color.purple_500,null))
                        .setSmallIcon(R.drawable.ic_dollar)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setFullScreenIntent(pendingIntent,true)
                        .build();

                manager.notify(new Random().nextInt(85-65),notification);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}
