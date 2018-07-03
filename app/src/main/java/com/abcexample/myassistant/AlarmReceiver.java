package com.abcexample.myassistant;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v7.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;


public class AlarmReceiver extends BroadcastReceiver {

    public static final int REQUEST_CODE = 12345;
    private DatabaseReference schedule_notification;
    private FirebaseAuth auth;
    private FirebaseUser user;


    @Override
    public void onReceive(final Context context, Intent intent) {

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        auth=FirebaseAuth.getInstance();
        user=auth.getCurrentUser();
        schedule_notification= FirebaseDatabase.getInstance().getReference().child("Users")
                .child(user.getUid())
                .child("Schedule")
                .child(String.valueOf(hour));

        schedule_notification.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String task=dataSnapshot.child("Task").getValue().toString();
                String hournew=dataSnapshot.child("Hour").getValue().toString();
                String minutenew=dataSnapshot.child("Minute").getValue().toString();

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

                builder.setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(getNotificationIcon(builder))
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.logo))
                        .setContentTitle("Time to " + task)
                        .setContentText(hournew + ":" + minutenew)
                        .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                        .setContentInfo("Remainder");

                NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
                Intent notifyintent =new Intent(context, ScheduleActivity.class);
                notifyintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

                PendingIntent pendingintent=PendingIntent.getActivity(context, 0, notifyintent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(pendingintent);

                notificationManager.notify(1, builder.build());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private int getNotificationIcon(NotificationCompat.Builder notificationBuilder) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int color = 0x008000;
            notificationBuilder.setColor(color);
            return R.drawable.logo;

        }
        return R.drawable.logo;
    }
}
