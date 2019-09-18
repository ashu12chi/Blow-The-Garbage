package com.npdevs.blowthegarbage;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ComplaintActivity extends FragmentActivity implements OnMapReadyCallback {

	private GoogleMap mMap;
	private LatLng latLng;
	private float DEFAULT_ZOOM;
	private ProgressDialog progressDialog;
	private EditText editName;
	private TextInputLayout nameLay;
	private Button btnSend;
	private final DatabaseReference databaseReference = FirebaseDatabase.getInstance()
			.getReference("complaint");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_complaint);

		editName=findViewById(R.id.editName);

		btnSend=findViewById(R.id.btnGoBack);

		nameLay=findViewById(R.id.name_text_input1);

		progressDialog=new ProgressDialog(this);
		progressDialog.setCancelable(false);

		FirebaseApp.initializeApp(this);

		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
		DEFAULT_ZOOM=14.0f;
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;
		mMap.getUiSettings().setAllGesturesEnabled(true);
		mMap.getUiSettings().setScrollGesturesEnabledDuringRotateOrZoom(true);
		mMap.getUiSettings().setCompassEnabled(true);
		mMap.getUiSettings().setMyLocationButtonEnabled(true);
		mMap.getUiSettings().setMapToolbarEnabled(true);
		mMap.setBuildingsEnabled(true);
		mMap.setIndoorEnabled(true);
		mMap.setTrafficEnabled(true);
		mMap.setMyLocationEnabled(true);

		// Add a marker in Prayagraj and move the camera
		latLng = new LatLng(25.494635, 81.867338);
		mMap.addMarker(new MarkerOptions().position(latLng).title("Marker in Prayagraj")).setDraggable(true);
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,DEFAULT_ZOOM));

		mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
			@Override
			public void onMapLongClick(LatLng latLng1) {
				DEFAULT_ZOOM=mMap.getCameraPosition().zoom;
				mMap.clear();
				latLng = latLng1;
				mMap.addMarker(new MarkerOptions().position(latLng).title("Marker in Prayagraj")).setDraggable(true);
				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,DEFAULT_ZOOM));
				drawCircle(latLng);
			}
		});
		mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
			@Override
			public void onMarkerDragStart(Marker marker) {
				DEFAULT_ZOOM=mMap.getCameraPosition().zoom;
			}

			@Override
			public void onMarkerDrag(Marker marker) {

			}

			@Override
			public void onMarkerDragEnd(Marker marker) {
				latLng=marker.getPosition();
				mMap.addMarker(new MarkerOptions().position(latLng).title("Marker in Prayagraj")).setDraggable(true);
				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,DEFAULT_ZOOM));
			}
		});
		mMap.setOnCameraMoveListener(() -> DEFAULT_ZOOM=mMap.getCameraPosition().zoom);

		btnSend.setOnClickListener(v -> {
			databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
					String complaint = editName.getText().toString();
					long time = System.currentTimeMillis();
					databaseReference.child(time+"").child("complaint").setValue(complaint);
					databaseReference.child(time+"").child("latitude").setValue(latLng.latitude);
					databaseReference.child(time+"").child("longitude").setValue(latLng.longitude);
				}

				@Override
				public void onCancelled(@NonNull DatabaseError databaseError) {

				}
			});
			finish();
		});

	}
	private void drawCircle(LatLng point){
		CircleOptions circleOptions = new CircleOptions();
		circleOptions.center(point);
		circleOptions.radius(20);
		circleOptions.strokeColor(Color.RED);
		circleOptions.fillColor(0x30ff0000);
		circleOptions.strokeWidth(2);
		mMap.addCircle(circleOptions);
	}

}
