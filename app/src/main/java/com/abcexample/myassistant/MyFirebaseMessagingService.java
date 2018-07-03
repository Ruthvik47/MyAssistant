package com.abcexample.myassistant;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.Console;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notification_title=remoteMessage.getNotification().getTitle();
        String notification_body=remoteMessage.getNotification().getBody();
        String from_user_id=remoteMessage.getData().get("from_user_id");
        int mNotificationId = (int) System.currentTimeMillis();

        NotificationCompat.Builder builder=new NotificationCompat.Builder(this)
                .setContentTitle(notification_title)
                .setWhen(System.currentTimeMillis())
                .setContentText(notification_body)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),
                        R.drawable.logo))
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setSmallIcon(R.drawable.logo);


        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent notifyintent =new Intent(this, MainActivity.class);
        Bundle bundle=new Bundle();
        bundle.putString("userid", from_user_id);
        notifyintent.putExtras(bundle);
        notifyintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingintent=PendingIntent.getActivity(this, 0, notifyintent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingintent);

        mNotifyMgr.notify(mNotificationId, builder.build());

    }


}
