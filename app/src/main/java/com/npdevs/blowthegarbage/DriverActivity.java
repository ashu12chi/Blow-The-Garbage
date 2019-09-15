package com.npdevs.blowthegarbage;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.optimization.v1.MapboxOptimization;
import com.mapbox.api.optimization.v1.models.OptimizationResponse;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

public class DriverActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapLongClickListener, PermissionsListener {

	private static final String ICON_GEOJSON_SOURCE_ID = "icon-source-id";
	private static final String FIRST = "first";
	private static final String ANY = "any";
	private static final String TEAL_COLOR = "#23D2BE";
	private static final float POLYLINE_WIDTH = 5;
	private MapView mapView;
	private MapboxMap mapboxMap;
	private DirectionsRoute optimizedRoute;
	private MapboxOptimization optimizedClient;
	private List<Point> stops = new ArrayList<>();
	private Point origin;

	private PermissionsManager permissionsManager;
	private String mobNo;
	private DatabaseReference myRef,garbageRef;
	private LatLng cleanerStart;
	private Cleaner cleaner;
	private double cleanerRange;
	IconFactory iconFactory;
	Icon icon;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Mapbox access token is configured here. This needs to be called either in your application
		// object or in the same activity which contains the mapview.
		Mapbox.getInstance(this, getString(R.string.access_token));

		// This contains the MapView in XML and needs to be called after the access token is configured.
		setContentView(R.layout.activity_driver);

		iconFactory = IconFactory.getInstance(DriverActivity.this);
		icon = iconFactory.fromResource(R.drawable.map_marker_dark);

		mobNo=getIntent().getStringExtra("MOB_NUMBER");
		FirebaseApp.initializeApp(this);
		myRef= FirebaseDatabase.getInstance().getReference("cleaners/"+mobNo);

		// to get Driver's start point and other data
		myRef.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				try {
					cleaner = dataSnapshot.getValue(Cleaner.class);
					assert cleaner != null;
					cleanerStart = new LatLng(cleaner.getLatitude(), cleaner.getLongitude());
					cleanerRange=cleaner.getRange()*1000.0;
				}catch(Exception e) {
					Toast.makeText(getApplicationContext(),"User data is corrupt",Toast.LENGTH_LONG).show();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				Toast.makeText(getApplicationContext(),"Some error occurred",Toast.LENGTH_LONG).show();
				finish();
			}
		});
		garbageRef= FirebaseDatabase.getInstance().getReference("garbage-request");

		showGarbages();


		// Setup the MapView
		mapView = findViewById(R.id.mapView);
		mapView.onCreate(savedInstanceState);
		mapView.getMapAsync(this);
	}

	private void showGarbages() {
		garbageRef.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				Log.e("TAG","GB count "+dataSnapshot.getChildrenCount());
				for(DataSnapshot postSnapshot:dataSnapshot.getChildren()) {
					Garbage post=postSnapshot.getValue(Garbage.class);
					LatLng garLatLng=new LatLng(post.getLatitude(),post.getLongitude());
					if(garLatLng.distanceTo(cleanerStart)<=cleanerRange && post.getVerified()) {
						Log.e("TAG","In range "+postSnapshot.getKey());

						// Optimization API is limited to 12 coordinate sets
						if (alreadyTwelveMarkersOnMap()) {
							Toast.makeText(DriverActivity.this, "Only 12 steps allowed", Toast.LENGTH_LONG).show();
						} else {
							Style style = mapboxMap.getStyle();
							if (style != null) {
//								addDestinationMarker(style, garLatLng);
								mapboxMap.addMarker(new MarkerOptions()
								.position(garLatLng)
								.title("Garbage here")
								.snippet("Pick this"))
								.setIcon(icon);
								addPointToStopsList(garLatLng);
								getOptimizedRoute(style, stops);
							}
						}
					}
					else
						Log.e("TAG","Not in range "+postSnapshot.getKey());
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	@Override
	public void onMapReady(@NonNull final MapboxMap mapboxMap) {
		this.mapboxMap = mapboxMap;
		mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
			@Override
			public void onStyleLoaded(@NonNull Style style) {
				enableLocationComponent(style);

				// Add the origin Point to the list
				addFirstStopToStopsList();

				initOptimizedRouteLineLayer(style);

				mapboxMap.addOnMapLongClickListener(DriverActivity.this);
			}
		});
	}


	private void initOptimizedRouteLineLayer(@NonNull Style loadedMapStyle) {
		loadedMapStyle.addSource(new GeoJsonSource("optimized-route-source-id"));
		loadedMapStyle.addLayerBelow(new LineLayer("optimized-route-layer-id", "optimized-route-source-id")
				.withProperties(
						lineColor(Color.parseColor(TEAL_COLOR)),
						lineWidth(POLYLINE_WIDTH)
				), "icon-layer-id");
	}

	@Override
	public boolean onMapLongClick(@NonNull LatLng point) {
		stops.clear();
		if (mapboxMap != null) {
			Style style = mapboxMap.getStyle();
			if (style != null) {
				resetDestinationMarkers(style);
				removeOptimizedRoute(style);
				addFirstStopToStopsList();
				return true;
			}
		}
		return false;
	}

	private void resetDestinationMarkers(@NonNull Style style) {
		GeoJsonSource optimizedLineSource = style.getSourceAs(ICON_GEOJSON_SOURCE_ID);
		if (optimizedLineSource != null) {
			optimizedLineSource.setGeoJson(Point.fromLngLat(origin.longitude(), origin.latitude()));
		}
	}

	private void removeOptimizedRoute(@NonNull Style style) {
		GeoJsonSource optimizedLineSource = style.getSourceAs("optimized-route-source-id");
		if (optimizedLineSource != null) {
			optimizedLineSource.setGeoJson(FeatureCollection.fromFeatures(new Feature[] {}));
		}
	}

	private boolean alreadyTwelveMarkersOnMap() {
		return stops.size() == 12;
	}

	private void addPointToStopsList(LatLng point) {
		stops.add(Point.fromLngLat(point.getLongitude(), point.getLatitude()));
	}

	private void addFirstStopToStopsList() {
		// Set first stop

		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
		}
		Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
		if (location != null) {
			double lat = location.getLatitude();
			double longi = location.getLongitude();
			origin=Point.fromLngLat(longi,lat);
			stops.add(origin);
			Log.d("NSP", "zoomMyCuurentLocation: location not null");
		} else {
			setMyLastLocation();
		}
	}
	private void setMyLastLocation() {
		Log.d("NSP", "setMyLastLocation: excecute, and get last location");
		FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			return;
		}
		fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
			@Override
			public void onSuccess(Location location) {
				if (location != null){
					double lat = location.getLatitude();
					double longi = location.getLongitude();
					LatLng latLng = new LatLng(lat,longi);
					origin=Point.fromLngLat(longi,lat);
					stops.add(origin);
					Log.d("NSP", "MyLastLocation coordinate :"+latLng);
				}
			}
		});
	}

	private void getOptimizedRoute(@NonNull final Style style, List<Point> coordinates) {
		optimizedClient = MapboxOptimization.builder()
				.source(FIRST)
				.destination(ANY)
				.coordinates(coordinates)
				.overview(DirectionsCriteria.OVERVIEW_FULL)
				.profile(DirectionsCriteria.PROFILE_DRIVING)
				.accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.access_token))
				.build();

		optimizedClient.enqueueCall(new Callback<OptimizationResponse>() {
			@Override
			public void onResponse(Call<OptimizationResponse> call, Response<OptimizationResponse> response) {
				if (!response.isSuccessful()) {
					Log.e("NSP","NO SUCCESS");
					Toast.makeText(DriverActivity.this, "NO SUCCESS", Toast.LENGTH_SHORT).show();
				} else {
					if (response.body() != null) {
						List<DirectionsRoute> routes = response.body().trips();
						if (routes != null) {
							if (routes.isEmpty()) {
								Log.e("NSP","%s size = %s SUCCESSFUL BUT NO ROUTES "+routes.size());
								Toast.makeText(DriverActivity.this, "SUCCESSFUL BUT NO ROUTES",
										Toast.LENGTH_SHORT).show();
							} else {
								// Get most optimized route from API response
								optimizedRoute = routes.get(0);
								drawOptimizedRoute(style, optimizedRoute);
//								NavigationLauncherOptions options = NavigationLauncherOptions.builder()
//										.directionsRoute(optimizedRoute)
//										.shouldSimulateRoute(true)
//										.build();
//                              // Call this method with Context from within an Activity
//								NavigationLauncher.startNavigation(DriverActivity.this, options);
							}
						} else {
							Log.e("NSP","list of routes in the response is null");
							Toast.makeText(DriverActivity.this, String.format("NULL RESPONSE",
									"The Optimization API response's list of routes"), Toast.LENGTH_SHORT).show();
						}
					} else {
						Log.e("NSP","response.body() is null");
						Toast.makeText(DriverActivity.this, String.format("NULL RESPONSE",
								"The Optimization API response's body"), Toast.LENGTH_SHORT).show();
					}
				}
			}

			@Override
			public void onFailure(Call<OptimizationResponse> call, Throwable throwable) {
				Log.e("NSP","Error: %s "+throwable.getMessage());
			}
		});
	}

	private void drawOptimizedRoute(@NonNull Style style, DirectionsRoute route) {
		GeoJsonSource optimizedLineSource = style.getSourceAs("optimized-route-source-id");
		if (optimizedLineSource != null) {
			optimizedLineSource.setGeoJson(FeatureCollection.fromFeature(Feature.fromGeometry(
					LineString.fromPolyline(route.geometry(), PRECISION_6))));
		}
	}
	@SuppressWarnings( {"MissingPermission"})
	private void enableLocationComponent(Style style) {

		// Check if permissions are enabled and if not request
		if (PermissionsManager.areLocationPermissionsGranted(this)) {

			// Get an instance of the component
			LocationComponent locationComponent = mapboxMap.getLocationComponent();

			// Activate with a built LocationComponentActivationOptions object
			locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(this, style).build());

			// Enable to make component visible
			locationComponent.setLocationComponentEnabled(true);

			// Set the component's camera mode
			locationComponent.setCameraMode(CameraMode.TRACKING);

			// Set the component's render mode
			locationComponent.setRenderMode(RenderMode.COMPASS);

		} else {

			permissionsManager = new PermissionsManager(this);

			permissionsManager.requestLocationPermissions(this);

		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	@Override
	public void onExplanationNeeded(List<String> permissionsToExplain) {
		Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onPermissionResult(boolean granted) {
		if (granted) {
			enableLocationComponent(mapboxMap.getStyle());
		} else {
			Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
			finish();
		}
	}


	@Override
	public void onResume() {
		super.onResume();
		mapView.onResume();
	}

	@Override
	protected void onStart() {
		super.onStart();
		mapView.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mapView.onStop();
	}

	@Override
	public void onPause() {
		super.onPause();
		mapView.onPause();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Cancel the directions API request
		if (optimizedClient != null) {
			optimizedClient.cancelCall();
		}
		mapView.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mapView.onLowMemory();
	}
}
