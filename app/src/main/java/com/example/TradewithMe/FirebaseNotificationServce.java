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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

            if (Build.VERSION.SDK_INT>Build.VERSION_CODES.O)
            {
                createOreoNotification(title,message,senderID,senderImage,receiverID);
            }
            else
            {
                createNormalNotificaiton(title,message,senderID,senderImage,receiverID);
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
}