package com.npdevs.blowthegarbage;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GarbageClassify extends AppCompatActivity {

	private static final String MODEL_PATH = "mobilenet_quant_v1_224.tflite";
	private static final boolean QUANT = true;
	private static final String LABEL_PATH = "labels.txt";
	private static final int INPUT_SIZE = 224;
	private boolean isGarbage;
	private int garbageIndex;
	private String[] garbage= {"Laptop","Notebook"};
	private Classifier classifier;
	private FusedLocationProviderClient mFusedLocationProviderClient;
	private boolean mLocationPermissionGranted=false;
	private Location mLastKnownLocation;
	private StorageReference storageReference;
	private DatabaseReference databaseReference;
	private long time=System.currentTimeMillis();
	private String MOB_NUMBER;
	private ArrayList<String> upvoters;
	private Executor executor = Executors.newSingleThreadExecutor();
	private TextView textViewResult;
	private Button btnDetectObject, btnNext;
	private ImageView imageViewResult;
	private CameraView cameraView;
	private Bitmap bitmap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_garbage_classify);
		cameraView = findViewById(R.id.cameraView);
		imageViewResult = findViewById(R.id.imageViewResult);
		textViewResult = findViewById(R.id.textViewResult);
		textViewResult.setMovementMethod(new ScrollingMovementMethod());
		Intent intent = getIntent();
		MOB_NUMBER = intent.getStringExtra("MOB_NUMBER");
		FirebaseApp.initializeApp(this);

		mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
		getLocationPermission();
		getDeviceLocation();
		storageReference = FirebaseStorage.getInstance().getReference("garbage-request");
		databaseReference = FirebaseDatabase.getInstance().getReference("garbage-request");

		btnNext = findViewById(R.id.btnToggleCamera);
		btnDetectObject = findViewById(R.id.btnDetectObject);

		cameraView.addCameraKitListener(new CameraKitEventListener() {
			@Override
			public void onEvent(CameraKitEvent cameraKitEvent) {

			}

			@Override
			public void onError(CameraKitError cameraKitError) {

			}

			@Override
			public void onImage(CameraKitImage cameraKitImage) {

				bitmap = cameraKitImage.getBitmap();

				bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);

				imageViewResult.setImageBitmap(bitmap);

				final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
				for(Classifier.Recognition item : results){
					String objects=item.toString();
					garbageIndex=0;
					objects=objects.substring(objects.indexOf(' '),objects.indexOf(' ',8));
					for(String x:garbage)
					{
						if(objects.trim().equalsIgnoreCase(x)) {
							isGarbage = true;
							break;
						}
						garbageIndex++;
					}
					Log.e("Ashu",objects);
				}
				textViewResult.setText(results.toString());
			}

			@Override
			public void onVideo(CameraKitVideo cameraKitVideo) {

			}
		});

		btnNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.e("Ashu",isGarbage+"");
				try {
					if (isGarbage) {
						Toast.makeText(GarbageClassify.this, "Verified!!!", Toast.LENGTH_SHORT).show();

						getDeviceLocation();
						Log.e("NSP", "Location Recieved: " + mLastKnownLocation);
						Garbage garbageUpload = new Garbage(garbage[garbageIndex], true, false, mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude(), 0, true,"No-url",MOB_NUMBER,upvoters);
						String uploadID = time + "";
						databaseReference.child(uploadID).setValue(garbageUpload);
					} else {
						Toast.makeText(GarbageClassify.this, "Verification Failed!!!\nPlease upload image for verification", Toast.LENGTH_LONG).show();
					}
				} catch (Exception e) {
					Log.e("LOC",e.toString());
					Toast.makeText(GarbageClassify.this, "Verification Failed!!!\nPlease try again", Toast.LENGTH_LONG).show();
				}
			}
		});

		btnDetectObject.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cameraView.captureImage();
			}
		});

		initTensorFlowAndLoadModel();
	}

	@Override
	protected void onResume() {
		super.onResume();
		cameraView.start();
	}

	@Override
	protected void onPause() {
		cameraView.stop();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		executor.execute(new Runnable() {
			@Override
			public void run() {
				classifier.close();
			}
		});
	}

	private void initTensorFlowAndLoadModel() {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					classifier = TensorFlowImageClassifier.create(
							getAssets(),
							MODEL_PATH,
							LABEL_PATH,
							INPUT_SIZE,
							QUANT);
					makeButtonVisible();
				} catch (final Exception e) {
					throw new RuntimeException("Error initializing TensorFlow!", e);
				}
			}
		});
	}

	private void makeButtonVisible() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				btnDetectObject.setVisibility(View.VISIBLE);
			}
		});
	}

	private void getLocationPermission() {
		/*
		 * Request location permission, so that we can get the location of the
		 * device. The result of the permission request is handled by a callback,
		 * onRequestPermissionsResult.
		 */
		if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
				android.Manifest.permission.ACCESS_FINE_LOCATION)
				== PackageManager.PERMISSION_GRANTED) {
			mLocationPermissionGranted = true;
		} else {
			ActivityCompat.requestPermissions(this,
					new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
					500);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		mLocationPermissionGranted = false;
		switch (requestCode) {
			case 500: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					mLocationPermissionGranted = true;
				}
			}
		}

	}
	private void getDeviceLocation() {
		/*
		 * Get the best and most recent location of the device, which may be null in rare
		 * cases when a location is not available.
		 */
		try {
			if (mLocationPermissionGranted) {
				Task locationResult = mFusedLocationProviderClient.getLastLocation();
				locationResult.addOnCompleteListener(this, new OnCompleteListener() {
					@Override
					public void onComplete(@NonNull Task task) {
						if (task.isSuccessful()) {
							// Set the map's camera position to the current location of the device.
							mLastKnownLocation = (Location) task.getResult();
						} else {
							Log.d("NSP", "Current location is null. Using defaults.");
							Log.e("NSP", "Exception: %s", task.getException());
						}
					}
				});
			}
		} catch(SecurityException e)  {
			Log.e("Exception: %s", e.getMessage());
		}
	}
}
