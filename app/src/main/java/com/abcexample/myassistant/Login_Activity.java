package com.abcexample.myassistant;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.concurrent.TimeUnit;


public class Login_Activity extends AppCompatActivity {

    private static final String TAG = "Firebase";
    private  EditText phonenumber;
    private EditText verificationcode;
    private Button sendotp;
    private  PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private ProgressBar loginprogressbar;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private int buttontype=0;
    private DatabaseReference mdatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_);

        phonenumber=(EditText) findViewById(R.id.phonenumber);
        verificationcode=(EditText)findViewById(R.id.verificationcode);
        mAuth=FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();
        mdatabase= FirebaseDatabase.getInstance().getReference().child("Users");
        sendotp=(Button)findViewById(R.id.loginbutton);
        loginprogressbar=(ProgressBar) findViewById(R.id.loginprogressbar);
        verificationcode.setVisibility(View.GONE);

        sendotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(buttontype==0) {
                    phonenumber.setVisibility(View.GONE);
                    String mphonenumber = phonenumber.getText().toString();
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            mphonenumber,
                            120,
                            TimeUnit.SECONDS,
                            Login_Activity.this,
                            mCallbacks
                    );
                    verificationcode.setVisibility(View.VISIBLE);
                    loginprogressbar.setVisibility(View.VISIBLE);
                    sendotp.setText("Verify OTP");
                }
                else{

                    String verification=verificationcode.getText().toString();
                    PhoneAuthCredential credential=PhoneAuthProvider.getCredential(mVerificationId, verification);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        mCallbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(getApplicationContext(), "Error in Verification", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                // Save verification ID and resending token so we can use them later
                buttontype=1;
                mVerificationId = verificationId;
                mResendToken = token;

            }


        };

    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Intent intent = new Intent(Login_Activity.this, RegisterActivity.class);
                            startActivity(intent);
                            finish();
                            FirebaseUser user = task.getResult().getUser();
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI

                            Toast.makeText(getApplicationContext(), "Error in Logging In", Toast.LENGTH_LONG).show();

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

}
