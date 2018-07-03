package com.abcexample.myassistant;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import java.text.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference mdatabase;
    private DatabaseReference database;
    CircleImageView profile_pic;
    TextView profile_name;
    TextView profile_status;
    ProgressDialog progress;
    private Button sendrequest;
    private String currentstatus;
    private DatabaseReference Friendsdatabase;
    private DatabaseReference notificationdb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Bundle bundle=getIntent().getExtras();
        final String id=bundle.getString("userid");

        profile_pic=(CircleImageView)findViewById(R.id.profilepic);
        profile_name=(TextView)findViewById(R.id.profilename);
        profile_status=(TextView)findViewById(R.id.profilestatus);
        sendrequest=(Button)findViewById(R.id.sendrequestbutton);

        auth=FirebaseAuth.getInstance();
        user=auth.getCurrentUser();
        mdatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(id);
        mdatabase.keepSynced(true);
        database=FirebaseDatabase.getInstance().getReference().child("FriendRequestData");
        notificationdb=FirebaseDatabase.getInstance().getReference().child("Notifications");
        Friendsdatabase=FirebaseDatabase.getInstance().getReference().child("Friends");
        currentstatus="not_friends";

        progress=new ProgressDialog(this);
        progress.setTitle("Loading Profile Data");
        progress.setMessage("Please Wait....");
        progress.setCanceledOnTouchOutside(false);
        progress.show();


        mdatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Glide.with(getApplicationContext())
                        .load(dataSnapshot.child("thumbnail").getValue().toString())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(profile_pic);
                profile_name.setText(dataSnapshot.child("Name").getValue().toString());
                profile_status.setText(dataSnapshot.child("Status").getValue().toString());

                //Checking Request

                database.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(id))
                        {
                            String req_type=dataSnapshot.child(id).child("request_type").getValue().toString();
                            if(req_type.equals("received")){
                                currentstatus="req_received";
                                sendrequest.setText("Accept Friend Request");

                            }
                            else if(req_type.equals("sent")){
                                currentstatus="req_sent";
                                sendrequest.setText("Cancel Friend Request");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                Friendsdatabase.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(id)){
                            currentstatus="friends";
                            sendrequest.setText("Un Friend");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                progress.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Database Error", Toast.LENGTH_SHORT ).show();
                progress.dismiss();
            }
        });


        sendrequest.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {

                sendrequest.setEnabled(false);

                //Not Friends State//

                if(currentstatus.equals("not_friends")) {

                    database.child(user.getUid()).child(id).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            database.child(id).child(user.getUid()).child("request_type").setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    String date=DateFormat.getDateTimeInstance().format(new Date());
                                    HashMap<String, String> notificationdata=new HashMap<>();
                                    notificationdata.put("from", user.getUid());
                                    notificationdata.put("type", "request");
                                    notificationdata.put("dateandtime", date);
                                    notificationdb.child(id).push().setValue(notificationdata).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            sendrequest.setEnabled(true);
                                            currentstatus="req_sent";
                                            sendrequest.setText("Cancel Friend Request");
                                        }
                                    });

                                }
                            });

                            sendrequest.setEnabled(true);
                        }
                    });

                }

                //Request Sent State//

                if(currentstatus.equals("req_sent")){

                    database.child(user.getUid()).child(id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            database.child(id).child(user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    sendrequest.setEnabled(true);
                                    currentstatus="not_friends";
                                    sendrequest.setText("Send Friend Request");
                                }
                            });
                        }
                    });
                }

                //Request Received State//

                if(currentstatus.equals("req_received")){

                    final String Current_date= DateFormat.getDateTimeInstance().format(new Date());
                    Friendsdatabase.child(user.getUid()).child(id).child("Time").setValue(Current_date).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Friendsdatabase.child(id).child(user.getUid()).child("Time").setValue(Current_date).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    database.child(user.getUid()).child(id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            database.child(id).child(user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    sendrequest.setEnabled(true);
                                                    currentstatus="friends";
                                                    sendrequest.setText("Un Friend");
                                                }
                                            });
                                        }
                                    });

                                }
                            });
                        }
                    });

                }

                if(currentstatus.equals("friends"))
                {
                    Friendsdatabase.child(user.getUid()).child(id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Friendsdatabase.child(id).child(user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    sendrequest.setEnabled(true);
                                    currentstatus="not_friends";
                                    sendrequest.setText("Send Friend Request");
                                }
                            });
                        }
                    });
                }

            }
        });

    }
}
