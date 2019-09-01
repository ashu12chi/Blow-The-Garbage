package com.npdevs.blowthegarbage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class SignUp extends AppCompatActivity {

    private Button signup,getOTP;
    private EditText name,mobNumber,address,password1,password2,otp;
    private FirebaseAuth mAuth;
    private String codeSent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        signup = findViewById(R.id.button);

        name = findViewById(R.id.editText);
        mobNumber = findViewById(R.id.editText2);
        address = findViewById(R.id.editText3);
        password1 = findViewById(R.id.editText4);
        password2 = findViewById(R.id.editText5);
        otp = findViewById(R.id.editText7);
        getOTP = findViewById(R.id.button5);

        mAuth=FirebaseAuth.getInstance();

        FirebaseApp.initializeApp(this);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifySignInCode();
            }
        });
        getOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendVerificationCode();
            }
        });
    }
    private void openMainActivity(PhoneAuthCredential phoneAuthCredential) {

        mAuth.signInWithCredential(phoneAuthCredential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            final DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                                    .getReference("users");
                            String name1 = name.getText().toString();
                            String mobNumber1 = mobNumber.getText().toString();
                            String address1 = address.getText().toString();
                            String password11 = password1.getText().toString();
                            String password21 = password2.getText().toString();
                            final Users users = new Users(name1,mobNumber1,password11,address1);
                            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.child(users.getMobNumber()).exists())
                                        Toast.makeText(getApplicationContext(),"User already exists!!!",Toast.LENGTH_SHORT)
                                                .show();
                                    else
                                    {
                                        databaseReference.child(users.getMobNumber()).setValue(users);
                                        Toast.makeText(getApplicationContext(),"SignUp successful!!!",Toast.LENGTH_SHORT).show();

                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                });

        mAuth.signOut();
        mAuth.signOut();
    }

    private void verifySignInCode() {
        String code = otp.getText().toString();
        PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(codeSent,code);
        openMainActivity(phoneAuthCredential);
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

        }

        @Override
        public void onVerificationFailed(FirebaseException e) {

        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            codeSent=s;
            System.out.println("Reached Here Code "+codeSent);
        }
    };

    private void sendVerificationCode() {
        String mobNumber1 = mobNumber.getText().toString();
        PhoneAuthProvider.getInstance()
                .verifyPhoneNumber("+91"+mobNumber1,60, TimeUnit.SECONDS,this, mCallbacks);
        System.out.println("Reached Here");
    }
}