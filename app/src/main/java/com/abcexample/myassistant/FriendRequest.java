package com.abcexample.myassistant;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendRequest extends AppCompatActivity {

    private RecyclerView friendrequestlist;
    private DatabaseReference mdatabase;
    private DatabaseReference database;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private ProgressDialog mprogress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);

        auth= FirebaseAuth.getInstance();
        user=auth.getCurrentUser();
        mdatabase= FirebaseDatabase.getInstance().getReference().child("FriendRequestData").child(user.getUid());
        mdatabase.keepSynced(true);
        database=FirebaseDatabase.getInstance().getReference().child("Users");
        database.keepSynced(true);

        friendrequestlist=(RecyclerView)findViewById(R.id.requestlist);
        friendrequestlist.setHasFixedSize(true);
        friendrequestlist.setLayoutManager(new LinearLayoutManager(this));

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

        FirebaseRecyclerAdapter<Requests, FriendRequest.UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Requests, FriendRequest.UsersViewHolder>(
                Requests.class,
                R.layout.custom,
                FriendRequest.UsersViewHolder.class,
                mdatabase) {
            @Override
            protected void populateViewHolder(final FriendRequest.UsersViewHolder viewHolder, final Requests model, int position) {

                final String id=getRef(position).getKey();

                database.child(id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        viewHolder.setDisplayName(dataSnapshot.child("Name").getValue().toString());
                        viewHolder.setstatus(dataSnapshot.child("Status").getValue().toString());
                        viewHolder.setthumbnail(dataSnapshot.child("thumbnail").getValue().toString(), getApplicationContext());
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
                        Intent i=new Intent(FriendRequest.this, ProfileActivity.class);
                        Bundle bundle=new Bundle();
                        bundle.putString("userid", id);
                        i.putExtras(bundle);
                        startActivity(i);
                    }
                });
            }
        };

        friendrequestlist.setAdapter(firebaseRecyclerAdapter);

        mprogress.dismiss();
    }


    @Override
    public void onBackPressed() {

        finish();
        Intent i=new Intent(this, MainActivity.class);
        startActivity(i);
    }


    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        View mview;

        public UsersViewHolder(View itemView) {
            super(itemView);
            mview=itemView;
        }


        public void setDisplayName(String name)
        {
            TextView Usernameview=(TextView) mview.findViewById(R.id.allusersname);
            Usernameview.setText(name);
        }

        public void setstatus(String status) {

            TextView userstatus=(TextView)mview.findViewById(R.id.allusersstatus);
            userstatus.setText(status);
        }

        public void setthumbnail(String thumbnail, Context act) {

            CircleImageView thumb_image = (CircleImageView) mview.findViewById(R.id.allusersimage);
            Glide.with(act).load(thumbnail).into(thumb_image);

        }


    }
}
