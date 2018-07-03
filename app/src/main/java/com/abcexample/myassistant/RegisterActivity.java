package com.abcexample.myassistant;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText name;
    private EditText emailid;
    private EditText dateofbirth;
    private Button savebutton;
    private ProgressBar signupprogressbar;
    private DatabaseReference mdatabase;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private Map datamap;
    private FireBaseDataMap obj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        name=(EditText)findViewById(R.id.Name);
        emailid=(EditText)findViewById(R.id.email);
        signupprogressbar=(ProgressBar)findViewById(R.id.registerprogressbar);
        dateofbirth=(EditText)findViewById(R.id.dateofbirth);
        savebutton=(Button)findViewById(R.id.savebutton);
        auth=FirebaseAuth.getInstance();
        user=auth.getCurrentUser();
        mdatabase= FirebaseDatabase.getInstance().getReference().child("Users");
        obj = new FireBaseDataMap();
        datamap = obj.fireebaseMap();

        savebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signupprogressbar.setVisibility(View.VISIBLE);
                String token= FirebaseInstanceId.getInstance().getToken();
                datamap.put("Name", name.getText().toString());
                datamap.put("Email", emailid.getText().toString());
                datamap.put("Date of Birth", dateofbirth.getText().toString());
                datamap.put("Imageurl", "https://dcassetcdn.com/common/images/v3/no-profile-pic-tiny.png");
                datamap.put("Status", "Available");
                datamap.put("thumbnail", "https://dcassetcdn.com/common/images/v3/no-profile-pic-tiny.png");
                datamap.put("switch", "false");
                datamap.put("DeviceToken", token);
                mdatabase.child(user.getUid()).updateChildren(datamap).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        Intent i=new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(i);
                        finish();
                    }
                });

            }
        });

    }

}

