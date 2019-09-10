package com.npdevs.blowthegarbage;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        description = findViewById(R.id.editText6);
        imageView = findViewById(R.id.imageView);
        radioButton = findViewById(R.id.radioButton);
        radioButton1 = findViewById(R.id.radioButton2);
        radioButton2 = findViewById(R.id.radioButton3);
        radioButton3 = findViewById(R.id.radioButton4);
        progressBar = findViewById(R.id.progressBar2);
        choose = findViewById(R.id.button2);
        upload = findViewById(R.id.button4);
        double[] location=getIntent().getDoubleArrayExtra("Location");
        assert location != null;
        latLng=new LatLng(location[0],location[1]);
        FirebaseApp.initializeApp(this);

        storageReference = FirebaseStorage.getInstance().getReference("garbage-request");
        databaseReference = FirebaseDatabase.getInstance().getReference("garbage-request");
        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  Log.e("Ashu","Reached!!!");
                uploadFile();
                //Log.e("Ashu","Reached!!!");
            }
        });
        radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                severe = true;
            }
        });
        radioButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                severe = false;
            }
        });
        radioButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                organic = true;
            }
        });
        radioButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                organic = false;
            }
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
      //  Log.e("Ashu","Reached!!!");
        if(ImageUri!=null){
            StorageReference fileReference = storageReference.child(time+"."+getFileExtension(ImageUri));
            fileReference.putFile(ImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @SuppressLint("ShowToast")
                        @Override
                        public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setProgress(0);
                                }
                            },3000);
                            Toast.makeText(UploadActivity.this,"Garbage Successful!",Toast.LENGTH_LONG);
                            storageReference.child(time+"."+getFileExtension(ImageUri)).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String url = uri.toString();
                                    Garbage garbage = new Garbage(description.getText().toString().trim(),severe,organic,latLng.latitude,latLng.longitude,0,false,url);
                                    String uploadID = time+"";
                                    databaseReference.child(uploadID).setValue(garbage);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                         //   Log.e("Ashu","Reached11!!!");
                            Toast.makeText(UploadActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                         //   Log.e("Ashu","Reached111!!!");
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            progressBar.setProgress((int)progress);
                        }
                    });
        }else{
            Toast.makeText(this,"No File selected!!!",Toast.LENGTH_SHORT).show();
        }
    }
}