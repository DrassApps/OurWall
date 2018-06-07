package com.drassapps.ourwall;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class FragmentMap extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    // MARK - CLASS

    private static final int LOCATION_SERVICE = 0;
    private Common ui = new Common();
    private GoogleApiClient googleApiClient;
    private FusedLocationProviderClient locationProvider;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;
    private LocationCallback locationCallback;
    private Location currentLocation;
    private SettingsClient settingsClient;
    private SharedPreferences.Editor editor;
    private ObjectDB usuData;

    // MARK - UI

    private MapView mapView;
    private GoogleMap googleMap;
    private RelativeLayout mLayout;
    private String currentRef;
    private BottomSheetBehavior bottomSheetBehavior;
    private TextView bottomSheetEmail;
    private ImageView bottomSheetHideButton, bottomSheetImage;


    // MARK - LIFE CYCLE

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    // MARK - MAIN
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle s) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SharedPreferences pref = getActivity().getPreferences(Context.MODE_PRIVATE);
        editor = pref.edit();

        String currentUser = pref.getString("userEmail", "");
        currentRef = pref.getString("currentRef", "");
        if (currentUser.equals("")) { saveUser(); }
        if (currentRef.equals("")) { setFirstRef(); currentRef = pref.getString("currentRef", ""); }

        if (!checkPermission()) { requestPermissions(); }

        // MARK - UI
        mLayout = getView().findViewById(R.id.mapFragment);

        // Setup map view
        mapView = getView().findViewById(R.id.mapFragment_map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        MapsInitializer.initialize(this.getActivity());

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // Initialize location services
        locationProvider = LocationServices.getFusedLocationProviderClient(getActivity());
        settingsClient = LocationServices.getSettingsClient(getActivity());

        // Retrieve user location and display
        createLocationCallback();
        setUpLocationRequest();
        buildLocationSettingsRequest();
        startLocationUpdates();
        getDataFromDB();
        configuraBottonSheet();

        ImageView reload = getView().findViewById(R.id.mapFragment_reloadImage);
        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { getDataFromDB(); }
        });
    }

    // MARK - METHODS

    // Save current user
    private void saveUser() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        editor.putString("currentUserUID", user.getUid());
        editor.putString("userEmail", user.getEmail());
        editor.commit();
    }

    // Set first current ref
    private void setFirstRef() {
        editor.putString("currentRef", "1");
        editor.putString("nMessagesSend", "0");
        editor.putString("nImagesSend", "0");
        editor.putString("nSoundSend", "0");
        editor.commit();
    }

    // Display usu locations
    private void displayLocationsMessage() {

        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.userpoint);

        LatLng lat = new LatLng(Double.parseDouble(usuData.lat), Double.parseDouble(usuData.lon));

        Marker marker1 = googleMap.addMarker(new MarkerOptions()
                .position(lat)
                .title(usuData.email)
                .snippet(usuData.input)
                .icon(icon));
        marker1.showInfoWindow();

        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) { showShareInfo(); }
        });
    }
    // Get locations on DB
    private void getLocationsOnDB(final String currentRef) {
        DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mDatabaseRefUsuario = mDatabaseRef.child(currentRef);

        mDatabaseRefUsuario.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                usuData = dataSnapshot.getValue(ObjectDB.class);

                if (usuData != null) { displayLocationsMessage(); }
            }
            @Override
            public void onCancelled(DatabaseError error) { }
        });
    }

    // Get locations of shares
    public void getDataFromDB() {
            for (int i = 0; i < Integer.valueOf(currentRef);i ++) {
                getLocationsOnDB(String.valueOf(i));
        }
    }

    // Initialize ui bottomsheet
    private void configuraBottonSheet(){

        LinearLayout btt_linear = getView().findViewById(R.id.btt_sh);
        bottomSheetBehavior = BottomSheetBehavior.from(btt_linear);

        bottomSheetHideButton = getView().findViewById(R.id.btsheet_hideButton);
        bottomSheetImage = getView().findViewById(R.id.btsheet_imageShare);
        bottomSheetEmail = getView().findViewById(R.id.btsheet_emailName);
    }

    // Show bottomSheet with info of sensor.
    private void showShareInfo() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        Log.i("MAIN", "EH" + currentRef);
        // Load image using GLIDE
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(String.valueOf(Integer.valueOf(currentRef)-1));
        Glide.with(getActivity()).using(new FirebaseImageLoader()).load(ref).into(bottomSheetImage);

        bottomSheetEmail.setText(usuData.email);

        bottomSheetHideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
    }

    // Set up location request with interval and accuracy type
    @SuppressLint("RestrictedApi")
    protected void setUpLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    // Build needed location request
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        locationSettingsRequest = builder.build();
    }

    // Get user location with location services
    private void startLocationUpdates() {
        settingsClient.checkLocationSettings(locationSettingsRequest).addOnSuccessListener(
                getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        if (!checkPermission()) { requestPermissions(); }
                        else {
                            locationProvider.requestLocationUpdates(
                                    locationRequest, locationCallback, Looper.myLooper());
                        }
                    }
                });
    }

    // Get user location and displya it on google map
    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                currentLocation = locationResult.getLastLocation();
                if (currentLocation != null) {

                    // Needed to set location
                    if (!checkPermission()) { requestPermissions(); }
                    else {
                        // Enable location button, controls and display location of user
                        googleMap.setMyLocationEnabled(true);
                        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                        googleMap.getUiSettings().setZoomControlsEnabled(true);

                        // Get current latatitude and longitude
                        Double latitude = currentLocation.getLatitude();
                        Double longitude = currentLocation.getLongitude();
                        Float zoom = 14.0f;

                        SharedPreferences pref = getActivity().getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("lat", String.valueOf(latitude));
                        editor.putString("lon", String.valueOf(longitude));
                        editor.commit();

                        // Create a new LatLng for move de camera to our location
                        LatLng current = new LatLng(latitude, longitude);
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current,zoom));
                    }
                }
            }
        };

    }
    // MARK - GOOGLE MAP INTERFACE
    @Override
    public void onMapReady(GoogleMap map) { googleMap = map; }
    @Override
    public void onConnectionSuspended(int arg0) { }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }
    @Override
    public void onConnected(Bundle bundle) { }

    // MARK - PERMISSIONS

    // Check permissions for > 23 APIs
    private boolean checkPermission() {
        int permissionState = ActivityCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    // Resquest permissions
    private void requestPermissions() {
        boolean shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale (
                getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION);
        // ProvdeRationale will be true when user click "don't ask me again"
        if (shouldProvideRationale) {
            // Show snackBar with an intent to package configuration
            ui.showConfiguration(mLayout,getActivity());
        } else {
            // Request permissions
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_SERVICE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // Our know code
        if (requestCode == LOCATION_SERVICE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                locationProvider = LocationServices.getFusedLocationProviderClient(getActivity());
                settingsClient = LocationServices.getSettingsClient(getActivity());
                createLocationCallback();
                setUpLocationRequest();
                buildLocationSettingsRequest();
                startLocationUpdates();
            } else {
                // Permission denied.
                ui.showConfiguration(mLayout,getActivity());
            }
        }
    }
}