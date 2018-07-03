package com.abcexample.myassistant;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.api.client.util.Data;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllusersActivity extends AppCompatActivity {

    private RecyclerView  muserslist;
    private DatabaseReference mdatabase;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private ProgressDialog mprogress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allusers);

        auth=FirebaseAuth.getInstance();
        user=auth.getCurrentUser();

        mdatabase= FirebaseDatabase.getInstance().getReference().child("Users");
        mdatabase.keepSynced(true);

        muserslist=(RecyclerView)findViewById(R.id.userslist);
        muserslist.setHasFixedSize(true);
        muserslist.setLayoutManager(new LinearLayoutManager(this));

    }


    @Override
    protected void onStart() {
        super.onStart();
        mprogress=new ProgressDialog(this);
        mprogress.setTitle("Loading Data");
        mprogress.setMessage("Please Wait....");
        mprogress.setCanceledOnTouchOutside(false);
        mprogress.show();
        FirebaseRecyclerAdapter<Users, AllusersActivity.UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, AllusersActivity.UsersViewHolder>(
                Users.class,
                R.layout.custom,
                AllusersActivity.UsersViewHolder.class,
                mdatabase) {
            @Override
            protected void populateViewHolder(final AllusersActivity.UsersViewHolder viewHolder, final Users model, int position) {
                viewHolder.setDisplayName(model.getName());
                viewHolder.setstatus(model.getStatus());
                viewHolder.setthumbnail(model.getThumbnail(), getApplicationContext());
                mprogress.dismiss();

                final String id=getRef(position).getKey();

                viewHolder.mview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i=new Intent(AllusersActivity.this, ProfileActivity.class);
                        Bundle bundle=new Bundle();
                        bundle.putString("userid", id);
                        i.putExtras(bundle);
                        startActivity(i);
                    }
                });
            }
        };
        muserslist.setAdapter(firebaseRecyclerAdapter);
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
