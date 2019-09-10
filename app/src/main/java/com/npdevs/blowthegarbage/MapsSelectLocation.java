package com.npdevs.blowthegarbage;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;

public class MapsSelectLocation extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback {

	private GoogleMap mMap;
	private GoogleApiClient mGoogleApiClient;
	private LocationRequest locationRequest;
	private final int REQUEST_CHECK_SETTINGS = 100;
	private final int REQUEST_LOCATION_PERMISSION= 500;
	private final int REQUEST_CODE_AUTOCOMPLETE= 600;
	private FusedLocationProviderClient mFusedLocationProviderClient;
	private Location mLastLocation;
	private LocationCallback locationCallback;
	private float DEFAULT_ZOOM=16.5f;
	private LatLng latLng;
	private Marker marker;
	private String access_token="pk.eyJ1IjoibmlzaGNoYWwiLCJhIjoiY2swMHZxeXNqMHE3NjNkc2N5NTJndnN2dCJ9.O2DHCiqvsvdRulclqUYxmg";
	private FloatingActionButton floatingActionButton;
	private Button buttonConfirm;
	private String MOB_NUMBER;
	private DatabaseReference myRef;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maps_select_location);
		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		assert mapFragment != null;
		mapFragment.getMapAsync(this);
		Mapbox.getInstance(this,access_token);

		FirebaseApp.initializeApp(this);
		MOB_NUMBER = getIntent().getStringExtra("MOB_NUMBER");
		myRef = FirebaseDatabase.getInstance().getReference("garbage-request");

		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addApi(LocationServices.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).build();
		mGoogleApiClient.connect();
		locationRequest = LocationRequest.create();
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locationRequest.setInterval(30 * 1000);
		locationRequest.setFastestInterval(5 * 1000);
		floatingActionButton=findViewById(R.id.floatingActionButton);
		buttonConfirm=findViewById(R.id.buttonConfirm);

		mFusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this);

		buttonConfirm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent=new Intent(MapsSelectLocation.this,UploadActivity.class);
				double location[]=new double[2];
				location[0]=latLng.latitude;
				location[1]=latLng.longitude;
				intent.putExtra("Location",location);
				intent.putExtra("MOB_NUMBER",MOB_NUMBER);
				startActivity(intent);
			}
		});
	}
	@Override
	public void onMapReady(GoogleMap googleMap) {
		boolean success=googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
				this, R.raw.style_json));
		if (!success) {
			Log.e("TAG", "Style parsing failed.");
		}
		mMap = googleMap;
//		mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		mMap.getUiSettings().setAllGesturesEnabled(true);
		mMap.getUiSettings().setScrollGesturesEnabledDuringRotateOrZoom(true);
		mMap.getUiSettings().setCompassEnabled(true);
		mMap.getUiSettings().setMyLocationButtonEnabled(true);
		mMap.getUiSettings().setMapToolbarEnabled(true);
		mMap.setBuildingsEnabled(true);
		mMap.setIndoorEnabled(true);
		mMap.setTrafficEnabled(true);
		mMap.setPadding(0,100, 0, 0);
		marker=mMap.addMarker(new MarkerOptions().position(new LatLng(-35.016, 143.321)).title("Drag to adjust...").draggable(true));

		enableMyLocation();
		showGarbages();

		mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
			@Override
			public boolean onMyLocationButtonClick() {
				DEFAULT_ZOOM=mMap.getCameraPosition().zoom;
				getDeviceLocation();
				return false;
			}
		});

		mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
			@Override
			public void onMarkerDragStart(Marker marker) {
			}
			@Override
			public void onMarkerDrag(Marker marker) {
			}
			@Override
			public void onMarkerDragEnd(Marker marker) {
				DEFAULT_ZOOM=mMap.getCameraPosition().zoom;
				latLng=new LatLng(marker.getPosition().latitude,marker.getPosition().longitude);
				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,DEFAULT_ZOOM));
			}
		});

		mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker marker1) {
				if(marker1.getTitle().equals("Garbage Here"));
				{
					openDialog(marker1);
				}
			}
		});

		mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
			@Override
			public void onMapLongClick(LatLng ll) {
				latLng=ll;
				marker.remove();
				marker=mMap.addMarker(new MarkerOptions().position(latLng).title("Drag to adjust...").draggable(true));
				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,DEFAULT_ZOOM));
			}
		});

		floatingActionButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				PlaceAutocomplete.clearRecentHistory(getApplicationContext());
				Intent intent = new PlaceAutocomplete.IntentBuilder()
						.placeOptions(PlaceOptions.builder().backgroundColor(Color.WHITE).proximity(Point.fromLngLat(latLng.longitude,latLng.latitude)).country("IN").build())
						.accessToken(access_token)
						.build(MapsSelectLocation.this);
				startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
			}
		});
	}
	private void showGarbages(){
		mMap.clear();
		marker=mMap.addMarker(new MarkerOptions().position(marker.getPosition()).title(marker.getTitle()).draggable(true).snippet(marker.getSnippet()));
		myRef.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for(DataSnapshot postSnapshot:dataSnapshot.getChildren()) {
					Garbage post=postSnapshot.getValue(Garbage.class);
					assert post != null;
					LatLng loc=new LatLng(post.getLatitude(),post.getLongitude());
					mMap.addMarker(new MarkerOptions().position(loc).title("Garbage Here").draggable(false).snippet("\tUpvotes: "+post.getUpvotes()+"\nTime: "+postSnapshot.getKey())
							.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	private void enableMyLocation() {
		if (ContextCompat.checkSelfPermission(this,
				Manifest.permission.ACCESS_FINE_LOCATION)
				== PackageManager.PERMISSION_GRANTED) {
			mMap.setMyLocationEnabled(true);
			getDeviceLocation();
		} else {
			ActivityCompat.requestPermissions(this, new String[]
							{Manifest.permission.ACCESS_FINE_LOCATION},
					REQUEST_LOCATION_PERMISSION);
		}
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
				getDeviceLocation();
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
				getDeviceLocation();
			} else {

				Toast.makeText(getApplicationContext(), "GPS is not enabled", Toast.LENGTH_SHORT).show();
			}

		}

		else if (resultCode == MapsSelectLocation.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {
			CarmenFeature feature = PlaceAutocomplete.getPlace(data);
			Toast.makeText(this, feature.text(), Toast.LENGTH_LONG).show();
			latLng=new LatLng((feature.bbox().north()+feature.bbox().south())/2.0,(feature.bbox().east()+feature.bbox().west())/2.0);
			marker.remove();
			marker=mMap.addMarker(new MarkerOptions().title(feature.placeName()).snippet("Searched place").position(latLng)
					.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)).draggable(true));
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,DEFAULT_ZOOM));
		}
	}

	private void getDeviceLocation() {
		mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
			@Override
			public void onComplete(@NonNull Task<Location> task) {
				if(task.isSuccessful()) {
					mLastLocation=task.getResult();
					if(mLastLocation!=null) {
						latLng=new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
						mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,DEFAULT_ZOOM));
						marker.remove();
						marker=mMap.addMarker(new MarkerOptions().position(latLng).title("Drag to adjust...").draggable(true));
					} else {
						final LocationRequest locationRequest=LocationRequest.create();
						locationRequest.setInterval(10000);
						locationRequest.setFastestInterval(5000);
						locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
						locationCallback=new LocationCallback() {
							@Override
							public void onLocationResult(LocationResult locationResult) {
								super.onLocationResult(locationResult);
								if(locationResult==null){
									return;
								}
								mLastLocation=locationResult.getLastLocation();
								latLng=new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
								mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,DEFAULT_ZOOM));
								marker.remove();
								marker=mMap.addMarker(new MarkerOptions().position(latLng).title("Drag to adjust...").draggable(true));
								mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
							}
						};
						mFusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,null);
					}
				}else
				{
					Toast.makeText(MapsSelectLocation.this,"Unable to get Last Location",Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	public void openDialog(final Marker marker1) {

		AlertDialog alertDialog = new AlertDialog.Builder(this).create();

		TextView title = new TextView(this);

		title.setText("");
		title.setPadding(10, 10, 10, 10);   // Set Position
		title.setGravity(Gravity.CENTER);
		title.setTextColor(Color.BLACK);
		title.setTextSize(20);
		alertDialog.setCustomTitle(title);

		TextView msg = new TextView(this);

		msg.setText("    SELECT AN OPTION");
		msg.setTextColor(Color.BLACK);
		msg.setTextSize(20);
		alertDialog.setView(msg);

		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, " UPVOTE ", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				String key1=marker1.getSnippet();
				final String key=key1.substring(key1.lastIndexOf(':')+2);
				final int up=Integer.parseInt(key1.substring(key1.indexOf(':')+2,key1.indexOf('\n')));
				System.out.println(key+" "+up);
				DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("garbage-request/"+key);
				databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
					@Override
					public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						Garbage data=dataSnapshot.getValue(Garbage.class);
						if(!data.getUpvoters().contains(MOB_NUMBER)) {
							data.getUpvoters().add(MOB_NUMBER);
							data.setUpvotes(up+1);
							myRef.child(key).setValue(data);
							showGarbages();
						}
						else {
							Toast.makeText(getApplicationContext(),"Already voted!!!",Toast.LENGTH_SHORT).show();
						}
					}

					@Override
					public void onCancelled(@NonNull DatabaseError databaseError) {
						Toast.makeText(getApplicationContext(),"Failed!!!",Toast.LENGTH_SHORT).show();
					}
				});
			}
		});

		alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL,"BACK  ", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// No need to write anything here
			}
		});

		alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE," DOWNVOTE ", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String key1=marker1.getSnippet();
				final String key=key1.substring(key1.lastIndexOf(':')+2);
				final int up=Integer.parseInt(key1.substring(key1.indexOf(':')+2,key1.indexOf('\n')));
				System.out.println(key+" "+up);
				DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("garbage-request/"+key);
				databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
					@Override
					public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						Garbage data=dataSnapshot.getValue(Garbage.class);
						if(!data.getUpvoters().contains(MOB_NUMBER)) {
							data.getUpvoters().add(MOB_NUMBER);
							data.setUpvotes(up-1);
							myRef.child(key).setValue(data);
							showGarbages();
						}
						else {
							Toast.makeText(getApplicationContext(),"Already voted!!!",Toast.LENGTH_SHORT).show();
						}
					}

					@Override
					public void onCancelled(@NonNull DatabaseError databaseError) {
						Toast.makeText(getApplicationContext(),"Failed!!!",Toast.LENGTH_SHORT).show();
					}
				});
			}
		});

		new Dialog(getApplicationContext());
		alertDialog.show();

		// Set Properties for OK Button
		final Button okBT = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
		LinearLayout.LayoutParams neutralBtnLP = (LinearLayout.LayoutParams) okBT.getLayoutParams();
		neutralBtnLP.gravity = Gravity.FILL_HORIZONTAL;
		okBT.setPadding(50, 10, 10, 10);   // Set Position
		okBT.setTextColor(Color.BLUE);
		okBT.setLayoutParams(neutralBtnLP);

		final Button cancelBT = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
		LinearLayout.LayoutParams negBtnLP = (LinearLayout.LayoutParams) okBT.getLayoutParams();
		negBtnLP.gravity = Gravity.FILL_HORIZONTAL;
		cancelBT.setTextColor(Color.RED);
		cancelBT.setLayoutParams(negBtnLP);
	}

}
