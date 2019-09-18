package com.npdevs.blowthegarbage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminComplaint extends FragmentActivity implements OnMapReadyCallback {

	private GoogleMap mMap;
	private LatLng latLng;
	private float DEFAULT_ZOOM;
	private RecyclerView recyclerView;
	private ProgressDialog progressDialog;
	List<ComplaintRecycler> msampleItem = new ArrayList<>();
	private final DatabaseReference databaseReference = FirebaseDatabase.getInstance()
			.getReference("complaint");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_admin_complaint);
		progressDialog=new ProgressDialog(this);
		progressDialog.setCancelable(false);
		recyclerView=findViewById(R.id.recycler_view);

		databaseReference.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				mMap.clear();
				msampleItem.clear();
				for(DataSnapshot postSnapshot:dataSnapshot.getChildren())
				{
					msampleItem.add(new ComplaintRecycler(postSnapshot.child("complaint").getValue().toString(),
							postSnapshot.child("latitude").getValue().toString(),postSnapshot.child("longitude").getValue().toString()));
				}
				recyclerView = findViewById(R.id.recycler_view);
				recyclerView.setHasFixedSize(true);
				RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(AdminComplaint.this);
				recyclerView.setLayoutManager(layoutManager);
				RecyclerView.Adapter adapter = new AdminComplaint.MainAdapter(msampleItem);
				recyclerView.setAdapter(adapter);
				progressDialog.cancel();
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

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
		mMap.addMarker(new MarkerOptions().position(latLng).title("Marker in Prayagraj")).setDraggable(false);
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,DEFAULT_ZOOM));

		mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
			@Override
			public void onCameraMove() {
				DEFAULT_ZOOM=mMap.getCameraPosition().zoom;
			}
		});

	}
	private class MainAdapter extends RecyclerView.Adapter<AdminComplaint.MainAdapter.ViewHolder> {

		private List<ComplaintRecycler> samples;
		class ViewHolder extends RecyclerView.ViewHolder {

			private TextView nameView;
			private CardView cardView;

			ViewHolder(View view) {
				super(view);
				nameView = view.findViewById(R.id.complaint);
				cardView = view.findViewById(R.id.cardView);
			}
		}

		MainAdapter(List<ComplaintRecycler> samples) {
			this.samples = samples;
			Log.e("nsp",samples.size()+"");
		}

		@NonNull
		@Override
		public AdminComplaint.MainAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			View view = LayoutInflater
					.from(parent.getContext())
					.inflate(R.layout.item_main_feature1, parent, false);

			return new AdminComplaint.MainAdapter.ViewHolder(view);
		}

		@Override
		public void onBindViewHolder(@NonNull AdminComplaint.MainAdapter.ViewHolder holder, int position) {
			holder.nameView.setText(samples.get(position).getComplaint());
			holder.cardView.setOnClickListener(view -> {
				LatLng latLng1 = new LatLng(Double.parseDouble(samples.get(position).getLatitude())
						,Double.parseDouble(samples.get(position).getLongitude()));
				Toast.makeText(getApplicationContext(),"Clicked!",Toast.LENGTH_SHORT).show();
				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng1,DEFAULT_ZOOM));
				drawCircle(latLng1);
				mMap.addMarker(new MarkerOptions().position(latLng1).title("Marker in Prayagraj")).setDraggable(false);
			});
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

			StrictMode.setThreadPolicy(policy);

		}

		@Override
		public int getItemCount() {
			return samples.size();
		}
		private void drawCircle(LatLng point){
			CircleOptions circleOptions = new CircleOptions();
			circleOptions.center(point);
			circleOptions.radius(200);
			circleOptions.strokeColor(Color.RED);
			circleOptions.fillColor(0x30ff0000);
			circleOptions.strokeWidth(2);
			mMap.addCircle(circleOptions);
		}
	}
}
