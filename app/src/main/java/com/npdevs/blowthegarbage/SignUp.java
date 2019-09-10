package com.npdevs.blowthegarbage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class SignUp extends AppCompatActivity {

	private Button signup,getOTP;
	private EditText name,mobNumber,address,password1,password2,otp;
	private FirebaseAuth mAuth;
	private ProgressDialog progressDialog;
	private String codeSent;
	private TextInputLayout nameLay,mobLay,addLay,p1Lay,p2Lay,otpLay;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_up);

		progressDialog=new ProgressDialog(this);
		progressDialog.setCancelable(false);
		signup = findViewById(R.id.button);
		name = findViewById(R.id.editText);
		mobNumber = findViewById(R.id.editText2);
		address = findViewById(R.id.editText3);
		password1 = findViewById(R.id.editText4);
		password2 = findViewById(R.id.editText5);
		otp = findViewById(R.id.editText7);
		getOTP = findViewById(R.id.button5);
		nameLay=findViewById(R.id.name_text_input1);
		mobLay=findViewById(R.id.name_text_input2);
		addLay=findViewById(R.id.name_text_input3);
		p1Lay=findViewById(R.id.name_text_input4);
		p2Lay=findViewById(R.id.name_text_input5);
		otpLay=findViewById(R.id.name_text_input6);

		FirebaseApp.initializeApp(this);
		mAuth=FirebaseAuth.getInstance();

		signup.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String n,m,a,p1,p2,o;
				n=name.getText().toString();
				m=mobNumber.getText().toString();
				a=address.getText().toString();
				p1=password1.getText().toString();
				p2=password2.getText().toString();
				o=otp.getText().toString();
				if(n.isEmpty()) {
					nameLay.setError("Enter valid name!");
					name.requestFocus();
					return;
				}
				if(m.length() != 10) {
					mobLay.setError("Enter valid Mobile Number!");
					mobNumber.requestFocus();
					return;
				}
				if(a.isEmpty()) {
					addLay.setError("Enter valid address!");
					address.requestFocus();
					return;
				}
				if(p1.isEmpty() || p1.length()<6) {
					p1Lay.setError("Enter at least 6 length password!");
					password1.requestFocus();
					return;
				}
				if(p2.isEmpty() || p2.length()<6 || !p2.equals(p1)) {
					p2Lay.setError("Enter same password here!");
					password2.requestFocus();
					return;
				} else {
					progressDialog.setMessage("Registering...");
					progressDialog.show();
					verifySignInCode();
				}
			}
		});
		getOTP.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String m=mobNumber.getText().toString();
				if(m.isEmpty()) {
					mobLay.setError("Enter valid Mobile Number");
					mobNumber.requestFocus();
					return;
				} else {
					progressDialog.setMessage("Sending OTP...");
					progressDialog.show();
					sendVerificationCode();
				}
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
							String password = password1.getText().toString();
							MessageDigest digest = null;
							try {
								digest = MessageDigest.getInstance("SHA-256");
							} catch (NoSuchAlgorithmException e) {
								e.printStackTrace();
							}
							assert digest != null;
							byte[] pp = digest.digest(password.getBytes(StandardCharsets.UTF_8));
							password= Arrays.toString(pp);
							final Users users = new Users(name1,mobNumber1,password,address1);
							databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
								@Override
								public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
									if(dataSnapshot.child(users.getMobNumber()).exists()) {
										Toast.makeText(getApplicationContext(), "User already exists!!!", Toast.LENGTH_SHORT)
												.show();
										progressDialog.cancel();
										finish();
									}
									else
									{
										databaseReference.child(users.getMobNumber()).setValue(users);
										Toast.makeText(getApplicationContext(),"SignUp successful!!!",Toast.LENGTH_SHORT).show();
										progressDialog.cancel();
										finish();
									}

								}

								@Override
								public void onCancelled(@NonNull DatabaseError databaseError) {
									Toast.makeText(getApplicationContext(),"Process Cancelled!",Toast.LENGTH_LONG).show();
									progressDialog.cancel();
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
			progressDialog.cancel();
		}

		@Override
		public void onVerificationFailed(FirebaseException e) {
			Toast.makeText(SignUp.this,"Something fishy happened!",Toast.LENGTH_SHORT).show();
			progressDialog.cancel();
		}

		@Override
		public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
			super.onCodeSent(s, forceResendingToken);
			codeSent=s;
			System.out.println("Reached Here Code "+codeSent);
			progressDialog.cancel();
		}
	};

	private void sendVerificationCode() {
		String mobNumber1 = mobNumber.getText().toString();
		PhoneAuthProvider.getInstance()
				.verifyPhoneNumber("+91"+mobNumber1,60, TimeUnit.SECONDS,this, mCallbacks);
		System.out.println("Reached Here");
		progressDialog.cancel();
	}
}