package com.abcexample.myassistant;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.api.services.vision.v1.model.Image;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity {

    private RecyclerView  friendslist;
    private DatabaseReference mdatabase;
    private DatabaseReference database;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private ProgressDialog mprogress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        auth= FirebaseAuth.getInstance();
        user=auth.getCurrentUser();
        mdatabase= FirebaseDatabase.getInstance().getReference().child("Friends").child(user.getUid());
        mdatabase.keepSynced(true);
        database=FirebaseDatabase.getInstance().getReference().child("Users");
        database.keepSynced(true);
        friendslist=(RecyclerView)findViewById(R.id.friendslist);
        friendslist.setHasFixedSize(true);
        friendslist.setLayoutManager(new LinearLayoutManager(this));

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mprogress=new ProgressDialog(this);
        mprogress.setTitle("Loading Data");
        mprogress.setMessage("Please Wait....");
        mprogress.setCanceledOnTouchOutside(false);
        mprogress.show();

        FirebaseRecyclerAdapter<Friends, FriendsActivity.UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsActivity.UsersViewHolder>(
                Friends.class,
                R.layout.custom,
                FriendsActivity.UsersViewHolder.class,
                mdatabase) {
            @Override
            protected void populateViewHolder(final FriendsActivity.UsersViewHolder viewHolder, Friends model, int position) {
                viewHolder.settime(model.getTime());

                final String id= getRef(position).getKey();
                database.child(id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        viewHolder.setDisplayName(dataSnapshot.child("Name").getValue().toString());
                        viewHolder.setthumbnail(dataSnapshot.child("thumbnail").getValue().toString(), getApplicationContext());
                        if(dataSnapshot.child("online").getValue().toString().equals("true")){
                            viewHolder.setonline();
                        }
                        mprogress.dismiss();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        mprogress.dismiss();
                    }
                });
                mprogress.dismiss();

                viewHolder.mview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CharSequence options[]=new CharSequence[]{"Open Profile", "Send Message"};

                        final AlertDialog.Builder builder=new AlertDialog.Builder(FriendsActivity.this);
                        builder.setTitle("Select Option");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which==0){
                                    Intent i=new Intent(FriendsActivity.this, ProfileActivity.class);
                                    Bundle bundle=new Bundle();
                                    bundle.putString("userid", id);
                                    i.putExtras(bundle);
                                    startActivity(i);
                                }
                                if(which==1){
                                    Intent i=new Intent(FriendsActivity.this, ChatActivity.class);
                                    Bundle bundle=new Bundle();
                                    bundle.putString("userid", id);
                                    i.putExtras(bundle);
                                    startActivity(i);
                                }

                            }
                        });
                        builder.show();
                    }
                });
            }
        };

        friendslist.setAdapter(firebaseRecyclerAdapter);
        mprogress.dismiss();
    }

    @Override
    public void onBackPressed() {
        finish();
        Intent i=new Intent(this, MainActivity.class);
        startActivity(i);
    }




    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View mview;

        public UsersViewHolder(View itemView) {
            super(itemView);
            mview = itemView;
        }

        public void setDisplayName(String name)
        {
            TextView Usernameview=(TextView) mview.findViewById(R.id.allusersname);
            Usernameview.setText(name);
        }


        public void settime(String time) {
            TextView Timeview=(TextView) mview.findViewById(R.id.allusersstatus);
            Timeview.setText(time);
        }

        public void setthumbnail(String thumbnail, Context act) {

            CircleImageView thumb_image = (CircleImageView) mview.findViewById(R.id.allusersimage);
            Glide.with(act).load(thumbnail).into(thumb_image);

        }

        public void setonline(){
            ImageView online=(ImageView)mview.findViewById(R.id.onlinebutton);
            online.setVisibility(View.VISIBLE);
        }

    }

}
