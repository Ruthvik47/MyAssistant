package com.abcexample.myassistant;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StatusActivity extends AppCompatActivity {

    private EditText updatestatus;
    private Button updatebuttton;
    private DatabaseReference mdatabase;
    private FirebaseAuth auth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        auth=FirebaseAuth.getInstance();
        user=auth.getCurrentUser();
        mdatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
        mdatabase.keepSynced(true);

        updatestatus=(EditText)findViewById(R.id.statusupdate);
        updatebuttton=(Button)findViewById(R.id.updatebutton);

        mdatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                updatestatus.setText(dataSnapshot.child("Status").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        updatebuttton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status= updatestatus.getText().toString();
                mdatabase.child("Status").setValue(status);
                Intent i=new Intent(StatusActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        });

    }
}
