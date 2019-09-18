package com.npdevs.blowthegarbage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback {
	private Button about;
	private Button login;
	private EditText mobNumber;
	private EditText password;
	private Button signup;
	private TextInputLayout textInputLayout1;
	private TextInputLayout textInputLayout2;
	private GoogleApiClient mGoogleApiClient;
	private LocationRequest locationRequest;
	private ProgressDialog progressDialog;
	private int REQUEST_CHECK_SETTINGS = 100;
	private int REQUEST_LOCATION_PERMISSION=500;
	private String mobNo;
	private String pswd;
	private String loggedIn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		enableMyLocation();
		schedulealarm();

		loadPreferences();
		if(loggedIn.charAt(0)=='D') {
			Toast.makeText(getApplicationContext(),"Login Success!",Toast.LENGTH_LONG).show();
			Intent intent = new Intent(MainActivity.this,DriverActivity.class);
			intent.putExtra("MOB_NUMBER",loggedIn.substring(1));
			startActivity(intent);
			finish();
		} else if(loggedIn.charAt(0)=='A') {
			Intent intent = new Intent(MainActivity.this,Admin.class);
			startActivity(intent);
			finish();
		}
		else if(!loggedIn.equals("no")) {
			Toast.makeText(getApplicationContext(),"Login Success!",Toast.LENGTH_LONG).show();
			Intent intent = new Intent(MainActivity.this,OptionsPage.class);
			intent.putExtra("MOB_NUMBER",loggedIn);
			startActivity(intent);
			finish();
		}
		setContentView(R.layout.activity_main);
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addApi(LocationServices.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).build();
		FirebaseApp.initializeApp(this);
		mGoogleApiClient.connect();
		locationRequest = LocationRequest.create();
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locationRequest.setInterval(30 * 1000);
		locationRequest.setFastestInterval(5 * 1000);

		about = findViewById(R.id.about);
		login = findViewById(R.id.login);
		mobNumber = findViewById(R.id.mobNumber);
		password = findViewById(R.id.password);
		signup = findViewById(R.id.button3);
		textInputLayout1=findViewById(R.id.name_text_input1);
		textInputLayout1.getEditText().addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				if(charSequence.length()==0) {
					textInputLayout1.setError("Field can't be left empty!");
				}
				else if(charSequence.charAt(0)=='D' && charSequence.length()!=11) {
					textInputLayout1.setError("Enter valid ID!");
				}
				else if(charSequence.charAt(0)!='D' && charSequence.length()!=10){
					textInputLayout1.setError("Enter 10 digit Mobile Number!");
				}
				else
				{
					textInputLayout1.setError(null);
				}
			}

			@Override
			public void afterTextChanged(Editable editable) {
			}
		});

		textInputLayout2=findViewById(R.id.name_text_input2);
		textInputLayout2.getEditText().addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				if(charSequence.length()==0){
					textInputLayout2.setError("Enter valid password!");
				}
				else
				{
					textInputLayout2.setError(null);
				}
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void afterTextChanged(Editable editable) {
			}
		});

		about.setOnClickListener(view -> openAboutActivity());
		signup.setOnClickListener(view -> {
			progressDialog=new ProgressDialog(MainActivity.this);
			openSignUpActivity();
		});
		login.setOnClickListener(view -> {
			progressDialog=new ProgressDialog(MainActivity.this);
			login();
		});
	}
	private void enableMyLocation() {
		if (ContextCompat.checkSelfPermission(this,
				Manifest.permission.ACCESS_FINE_LOCATION)
				== PackageManager.PERMISSION_GRANTED) {
		} else {
			ActivityCompat.requestPermissions(this, new String[]
							{Manifest.permission.ACCESS_FINE_LOCATION},
					REQUEST_LOCATION_PERMISSION);
		}
	}

	private void login() {
		mobNo=mobNumber.getText().toString();
		pswd=password.getText().toString();
		if(mobNo.equals("A987") && pswd.equals("A987"))
		{
			clearTable();
			saveTable();
			Intent intent = new Intent(MainActivity.this,Admin.class);
			startActivity(intent);
			finish();
		}
		if(mobNo.equals("R987") && pswd.equals("R987"))
		{
			Intent intent = new Intent(MainActivity.this,DriverRegisterActivity.class);
			startActivity(intent);
		}
		if(mobNo.charAt(0)=='D' && mobNo.length()!=11) {
			textInputLayout1.setError("Enter valid ID!");
			mobNumber.requestFocus();
			return;
		}
		if(mobNo.charAt(0)!='D' && mobNo.length()!=10)
		{
			textInputLayout1.setError("Enter 10 digit Mobile Number!");
			mobNumber.requestFocus();
			return;
		}
		if(pswd.isEmpty())
		{
			textInputLayout2.setError("Enter valid Password!");
			password.requestFocus();
			return;
		} else {
			progressDialog.setMessage("Logging In...");
			progressDialog.setCancelable(false);
			progressDialog.show();
			if (mobNo.charAt(0)=='D')
				openDriverActivity();
			else
				openOptionsPage();
		}
	}

	private void openDriverActivity() {
		DatabaseReference myRef= FirebaseDatabase.getInstance().getReference("cleaners");
		myRef.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				String mob=mobNumber.getText().toString().substring(1);
				String pwd=password.getText().toString();
				MessageDigest digest = null;
				try {
					digest = MessageDigest.getInstance("SHA-256");
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
				assert digest != null;
				byte[] pasd = digest.digest(pwd.getBytes(StandardCharsets.UTF_8));
				pwd= Arrays.toString(pasd);
				if(dataSnapshot.child(mob).exists())
				{
					if(!mob.isEmpty())
					{
						Cleaner login=dataSnapshot.child(mob).getValue(Cleaner.class);
						if(login.getPass().equals(pwd))
						{
							mobNo="D"+mob;
							clearTable();
							saveTable();
							Toast.makeText(getApplicationContext(),"Login Success!",Toast.LENGTH_LONG).show();
							Intent intent = new Intent(MainActivity.this,DriverActivity.class);
							intent.putExtra("MOB_NUMBER",mob);
							startActivity(intent);
							progressDialog.cancel();
							finish();
						}
						else {
							Toast.makeText(getApplicationContext(),"Wrong Password!",Toast.LENGTH_LONG).show();
							progressDialog.cancel();
						}
					}
					else
					{
						Toast.makeText(getApplicationContext(),"Enter valid ID!",Toast.LENGTH_LONG).show();
						progressDialog.cancel();
					}
				}
				else
				{
					Toast.makeText(getApplicationContext(),"User is not registered!",Toast.LENGTH_LONG).show();
					progressDialog.cancel();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				Toast.makeText(getApplicationContext(),"Process Cancelled!",Toast.LENGTH_LONG).show();
				progressDialog.cancel();
			}
		});
	}

	private void openOptionsPage() {
		DatabaseReference myRef= FirebaseDatabase.getInstance().getReference("users");
		myRef.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				String mob=mobNumber.getText().toString();
				String pwd=password.getText().toString();
				MessageDigest digest = null;
				try {
					digest = MessageDigest.getInstance("SHA-256");
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
				assert digest != null;
				byte[] pasd = digest.digest(pwd.getBytes(StandardCharsets.UTF_8));
				pwd= Arrays.toString(pasd);
				if(dataSnapshot.child(mob).exists())
				{
					if(!mob.isEmpty())
					{
						Users login=dataSnapshot.child(mob).getValue(Users.class);
						if(login.getPassword().equals(pwd))
						{
							mobNo=mob;
							clearTable();
							saveTable();
							Toast.makeText(getApplicationContext(),"Login Success!",Toast.LENGTH_LONG).show();
							Intent intent = new Intent(MainActivity.this,OptionsPage.class);
							intent.putExtra("MOB_NUMBER",mob);
							startActivity(intent);
							progressDialog.cancel();
							finish();
						}
						else {
							Toast.makeText(getApplicationContext(),"Wrong Password!",Toast.LENGTH_LONG).show();
							progressDialog.cancel();
						}
					}
					else
					{
						Toast.makeText(getApplicationContext(),"Enter 10 digit Mobile number!",Toast.LENGTH_LONG).show();
						progressDialog.cancel();
					}
				}
				else
				{
					Toast.makeText(getApplicationContext(),"User is not registered!",Toast.LENGTH_LONG).show();
					progressDialog.cancel();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				Toast.makeText(getApplicationContext(),"Process Cancelled!",Toast.LENGTH_LONG).show();
				progressDialog.cancel();
			}
		});
	}
	private void openSignUpActivity() {
		Intent intent = new Intent(this,SignUp.class);
		startActivity(intent);
	}

	private void openAboutActivity() {
		Intent intent = new Intent(this,About.class);
		startActivity(intent);
	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {
		LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
				.addLocationRequest(locationRequest);
		builder.setAlwaysShow(true);
		PendingResult result =
				LocationServices.SettingsApi.checkLocationSettings(
						mGoogleApiClient,
						builder.build()
				);

		result.setResultCallback(this);
	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

	}

	@Override
	public void onResult(@NonNull Result result) {
		final Status status = result.getStatus();
		switch (status.getStatusCode()) {
			case LocationSettingsStatusCodes.SUCCESS:
				break;

			case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
				try {
					status.startResolutionForResult(this, REQUEST_CHECK_SETTINGS);
				} catch (IntentSender.SendIntentException e) {
				}
				break;
			case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
				// Location settings are unavailable so not possible to show any dialog now
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CHECK_SETTINGS) {

			if (resultCode == RESULT_OK) {

				Toast.makeText(getApplicationContext(), "GPS enabled", Toast.LENGTH_SHORT).show();
			} else {

				Toast.makeText(getApplicationContext(), "GPS is not enabled", Toast.LENGTH_SHORT).show();
			}

		}
	}

	private void loadPreferences()
	{
		SharedPreferences sharedPreferences=getSharedPreferences("usersave",MODE_PRIVATE);
		loggedIn=sharedPreferences.getString("User","no");
		if(loggedIn.equals("") || loggedIn.isEmpty() || loggedIn.equals("no"))
			loggedIn="no";
	}

	private void clearTable()
	{
		SharedPreferences preferences = getSharedPreferences("usersave", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.clear();
		editor.commit();
	}

	private void saveTable()
	{
		SharedPreferences sharedPreferences=getSharedPreferences("usersave",MODE_PRIVATE);
		SharedPreferences.Editor editor=sharedPreferences.edit();
		editor.putString("User",mobNo);
		editor.apply();
	}

	private void schedulealarm() {

		// Construct an intent that will execute the AlarmReceiver
		Intent intent = new Intent(this, AlarmReciever.class);
		// Create a PendingIntent to be triggered when the alarm goes off
		final PendingIntent pIntent = PendingIntent.getBroadcast(this, AlarmReciever.REQUEST_CODE,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		// Setup periodic alarm every every half hour from this point onwards
		AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		// First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
		// Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
		alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime(),
				1000*40, pIntent);
		Log.e("Info","Reached Here by NSP");

	}
}
