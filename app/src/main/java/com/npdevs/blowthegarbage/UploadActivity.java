package com.npdevs.blowthegarbage;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class UploadActivity extends AppCompatActivity {
	private EditText description;
	private ImageView imageView;
	private RadioButton radioButton, radioButton1, radioButton2, radioButton3;
	private Button choose, upload;
	private StorageReference storageReference;
	private DatabaseReference databaseReference;
	private ProgressBar progressBar;
	private Uri ImageUri;
	private boolean severe;
	private boolean organic;
	private long time = System.currentTimeMillis();
	private LatLng latLng;
	private String MOB_NUMBER="ashu";
	private ArrayList<String> upvoters;
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setTitle("Upload Garbage Image");
		setContentView(R.layout.activity_upload);
		progressDialog=new ProgressDialog(UploadActivity.this);
		description = findViewById(R.id.editText6);
		imageView = findViewById(R.id.imageView);
		radioButton = findViewById(R.id.radioButton);
		radioButton1 = findViewById(R.id.radioButton2);
		radioButton2 = findViewById(R.id.radioButton3);
		radioButton3 = findViewById(R.id.radioButton4);
		progressBar = findViewById(R.id.progressBar2);
		severe=false;
		organic=false;
		choose = findViewById(R.id.button2);
		upvoters = new ArrayList<>();
		upload = findViewById(R.id.button4);
		Intent intent = getIntent();
		MOB_NUMBER = intent.getStringExtra("MOB_NUMBER");
		double[] location=intent.getDoubleArrayExtra("Location");
		assert location != null;
		latLng=new LatLng(location[0],location[1]);
		FirebaseApp.initializeApp(this);

		storageReference = FirebaseStorage.getInstance().getReference("garbage-request");
		databaseReference = FirebaseDatabase.getInstance().getReference("garbage-request");
		choose.setOnClickListener(view -> openFileChooser());
		radioButton.toggle();
		radioButton3.toggle();
		radioButton.setOnClickListener(view -> severe = false);
		radioButton1.setOnClickListener(view -> severe = true);
		radioButton2.setOnClickListener(view -> organic = true);
		radioButton3.setOnClickListener(view -> organic = false);
		upload.setOnClickListener(view -> {
			progressDialog.setMessage("Uploading...");
			progressDialog.setCancelable(false);
			progressDialog.show();
			uploadFile();
		});
	}

	private void openFileChooser() {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(intent,1);
	}
	private String getFileExtension(Uri uri){
		ContentResolver contentResolver = getContentResolver();
		MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
		return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
	}
	protected void onActivityResult(int requestCode,int resultCode,Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
			ImageUri = data.getData();
			imageView.setImageURI(ImageUri);
			imageView.setBackgroundColor(Color.WHITE);
		}
	}
	private void uploadFile(){
		if(ImageUri!=null){
			StorageReference fileReference = storageReference.child(time+"."+getFileExtension(ImageUri));
			fileReference.putFile(ImageUri)
					.addOnSuccessListener(taskSnapshot -> {
						Handler handler = new Handler();
						handler.postDelayed(() -> progressBar.setProgress(0),3000);
						storageReference.child(time+"."+getFileExtension(ImageUri)).getDownloadUrl().addOnSuccessListener(uri -> {
							String url = uri.toString();
							upvoters.add(MOB_NUMBER);
								Garbage garbage = new Garbage(description.getText().toString().trim(),severe,organic,latLng.latitude,latLng.longitude,1,false,url,MOB_NUMBER,upvoters);
							String uploadID = time+"";
							databaseReference.child(uploadID).setValue(garbage);
							progressDialog.cancel();
							Toast.makeText(UploadActivity.this,"Garbage adding Successful!",Toast.LENGTH_LONG).show();
							finish();
						});
					})
					.addOnFailureListener(e -> {
						Toast.makeText(UploadActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
						progressDialog.cancel();
					})
					.addOnProgressListener(taskSnapshot -> {
						double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
						progressBar.setProgress((int)progress);
					});
		}else{
			Toast.makeText(this,"No File selected!!!",Toast.LENGTH_SHORT).show();
			progressDialog.cancel();
		}
	}
}