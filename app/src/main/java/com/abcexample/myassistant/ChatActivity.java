package com.abcexample.myassistant;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private static final int GALLERY_PICK = 1;
    private String id;
    private DatabaseReference mdatabase;
    private DatabaseReference mrootref;
    private DatabaseReference mchatref;
    private DatabaseReference msgdb;
    private FirebaseAuth mauth;
    private FirebaseUser user;
    private TextView mdisplayname;
    private TextView mlastseen;
    private CircleImageView mimage;
    private ImageButton add_files_button;
    private ImageButton send_button;
    private EditText Message;
    private StorageReference mStorageRef;
    private RecyclerView mmessageslist;
    private final List<Messages> messages_list= new ArrayList<>();
    private LinearLayoutManager mLinearlayout;
    private MessageAdapter madapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Bundle bundle=getIntent().getExtras();
        id= bundle.getString("userid");


        ActionBar actionBar= getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater= (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionbarview=inflater.inflate(R.layout.chat_customlayout, null);
        actionBar.setCustomView(actionbarview);

        mdisplayname=(TextView) actionbarview.findViewById(R.id.chatbarname);
        mlastseen=(TextView) actionbarview.findViewById(R.id.chatbarlastseen);
        mimage=(CircleImageView)actionbarview.findViewById(R.id.custom_bar_image);
        add_files_button=(ImageButton)findViewById(R.id.addfilesbutton);
        send_button=(ImageButton)findViewById(R.id.sendbutton);
        Message=(EditText)findViewById(R.id.message);

        mdatabase= FirebaseDatabase.getInstance().getReference();
        mrootref=FirebaseDatabase.getInstance().getReference();
        mchatref=FirebaseDatabase.getInstance().getReference().child("messages");
        msgdb=FirebaseDatabase.getInstance().getReference().child("messages");
        mStorageRef = FirebaseStorage.getInstance().getReference();

        mauth=FirebaseAuth.getInstance();
        user=mauth.getCurrentUser();

        madapter=new MessageAdapter(messages_list);
        mmessageslist=(RecyclerView)findViewById(R.id.messageslist);
        mLinearlayout= new LinearLayoutManager(this);
        mmessageslist.setHasFixedSize(true);
        mmessageslist.setLayoutManager(mLinearlayout);
        mmessageslist.setAdapter(madapter);

        loadmessages(id);


        mdatabase.child("Users").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name=dataSnapshot.child("Name").getValue().toString();
                String lastseen=dataSnapshot.child("lastseen").getValue().toString();
                String imageurl=dataSnapshot.child("thumbnail").getValue().toString();
                mdisplayname.setText(name);
                Glide.with(getApplicationContext()).load(imageurl).into(mimage);
                if(dataSnapshot.child("online").getValue().toString().equals("true")){
                    mlastseen.setText("online");

                }
                else{

                    GetTimeAgo gettime=new GetTimeAgo();
                    long lasttime = Long.parseLong(lastseen);
                    String lastseentime=gettime.getTimeAgo(lasttime, getApplicationContext());
                    mlastseen.setText(lastseentime);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mrootref.child("Chat").child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(id)&&!id.equals("Assistant")){
                    Map chataddmap=new HashMap();
                    chataddmap.put("seen", false);
                    chataddmap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatusermap=new HashMap();
                    chatusermap.put("Chat/"+user.getUid()+"/"+id, chataddmap);
                    chatusermap.put("Chat/"+id+"/"+user.getUid(), chataddmap);

                    mrootref.updateChildren(chatusermap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError!=null){

                                Log.d("CHAT_LOG", databaseError.getDetails().toString());
                            }
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        add_files_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);

            }
        });

        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendmessage();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GALLERY_PICK && resultCode==RESULT_OK){

            Uri imageuri= data.getData();

            final String current_user_ref="messages/"+ user.getUid()+"/"+id;
            final String chat_user_ref="messages/"+id+"/"+user.getUid();

            DatabaseReference user_message_push=mrootref
                    .child("messages").child(user.getUid()).child(id).push();
            final String push_id=user_message_push.getKey();

            StorageReference filepath= mStorageRef.child("message_images").child(user.getUid()).child(id).child(push_id + ".jpg");

            filepath.putFile(imageuri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if(task.isSuccessful()){

                        String downloadurl= task.getResult().getDownloadUrl().toString();

                        Map message_map=new HashMap();
                        message_map.put("message", downloadurl);
                        message_map.put("seen", false);
                        message_map.put("type", "image");
                        message_map.put("time", ServerValue.TIMESTAMP);
                        message_map.put("from", id);

                        Map message_user_map=new HashMap();
                        message_user_map.put(current_user_ref + "/" + push_id, message_map);
                        message_user_map.put(chat_user_ref + "/"+ push_id, message_map);

                        mrootref.updateChildren(message_user_map, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if(databaseError!=null){

                                    Log.d("CHAT_LOG", databaseError.getDetails().toString());
                                }
                                Message.setText("");
                            }
                        });

                    }
                }
            });


        }
    }

    private void loadmessages(final String id) {

        user=mauth.getCurrentUser();

        mchatref.child(user.getUid()).child(id).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message=dataSnapshot.getValue(Messages.class);
                madapter.add(message);
                mLinearlayout.scrollToPosition(messages_list.size()-1);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void sendmessage() {

        String message=Message.getText().toString();

        if(!TextUtils.isEmpty(message)){
            String current_user_ref="messages/"+ user.getUid()+"/"+id;
            String chat_user_ref="messages/"+id+"/"+user.getUid();

            DatabaseReference user_message_push=mrootref
                    .child("messages").child(user.getUid()).child(id).push();
            String push_id=user_message_push.getKey();

            Map message_map=new HashMap();
            message_map.put("message", message);
            message_map.put("seen", false);
            message_map.put("type", "text");
            message_map.put("time", ServerValue.TIMESTAMP);
            message_map.put("from", id);

            Map message_user_map=new HashMap();
            message_user_map.put(current_user_ref + "/" + push_id, message_map);
            message_user_map.put(chat_user_ref + "/"+ push_id, message_map);

            mrootref.updateChildren(message_user_map, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError!=null){

                        Log.d("CHAT_LOG", databaseError.getDetails().toString());
                    }
                    Message.setText("");
                }
            });

        }
    }

}
