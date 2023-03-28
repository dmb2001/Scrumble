package com.app.scrumble;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.app.scrumble.model.group.scrapbook.Scrapbook;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener;
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class MapFragment extends BaseFragment implements OnMapReadyCallback, OnCameraIdleListener, OnMapLongClickListener, OnMarkerClickListener, OnCameraMoveStartedListener {

    public static final String NAME = "MAP";
    private static final String KEY_MARKER_ID = "KEY_MARKER_ID";
    private static final String KEY_LAST_KNOWN_LAT = "KEY_LAST_KNOWN_LAT";
    private static final String KEY_LAST_KNOWN_LONG = "KEY_LAST_KNOWN_LONG";

    FusedLocationProviderClient locationProvider;

    ImageButton profileButton;

    private MapView mapView;

    private LocationManager locationManager;

    private GoogleMap map;

    private LatLng userLocation;
    private boolean followUser = true;

    private static final float DEFAULT_ZOOM = 16.0f;

    public static MapFragment newInstance() {
        Bundle args = new Bundle();
        MapFragment fragment = new MapFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationProvider = LocationServices.getFusedLocationProviderClient(getContext());
        locationProvider.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null){
                            userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            if(map != null){
                                centerMapOnSelf(true);
                            }
                        }
                    }
                });
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 1000).setWaitForAccurateLocation(false).build();
        locationProvider.requestLocationUpdates(locationRequest, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                if(map != null && followUser){
                    centerMapOnSelf(true);
                }
            }
        }, Looper.getMainLooper());
    }

    private com.app.scrumble.model.group.scrapbook.Location convert(LatLng latLng){
        return new com.app.scrumble.model.group.scrapbook.Location(latLng.latitude, latLng.longitude);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View parentLayout = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = parentLayout.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        ImageButton homeButton = parentLayout.findViewById(R.id.button_map_home);
        homeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(map != null && userLocation != null){
                    followUser = true;
                    centerMapOnSelf(true);
                }else if(map != null){
                    Log.d("DEBUGGING", "");
                    userLocation = new LatLng(55.9, -3.3);
                    centerMapOnSelf(true);
                }
            }
        });
        ImageButton postButton = parentLayout.findViewById(R.id.button_new_post);

        postButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(map != null && userLocation != null){
                    showAsMainContent(NewSubmissionFragment.newInstance(new com.app.scrumble.model.group.scrapbook.Location(userLocation.latitude, userLocation.longitude)), true);
                }else{
                    Toast.makeText(getContext(), "Hang on, determining your location...", Toast.LENGTH_SHORT).show();
                }
            }
        });
        profileButton = parentLayout.findViewById(R.id.button_nav_profile);
        profileButton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(getCurrentUser() != null){
                            showAsMainContent(ProfileFragment.newInstance(getCurrentUser().getId()), true);
                        }
                    }
                }
        );
        if(getCurrentUser() != null){
            loadProfilePic();
        }

        profileButton.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isSafe() && getCurrentUser() != null){
                    loadProfilePic();
                }
            }
        }, 2000);

        ImageButton settingsButton = parentLayout.findViewById(R.id.button_nav_settings);
        settingsButton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(getCurrentUser() != null){
                            showAsMainContent(new SettingsFragment(), true);
                        }
                    }
                }
        );
        return parentLayout;
    }

    private void loadProfilePic(){
        String URL = URLStringBuilder.buildProfilePictureLocation(getCurrentUser().getId());
        Glide
                .with(getContext())
                .load(Uri.parse(URL))
                .centerCrop()
                .circleCrop()
                .fallback(R.drawable.ic_blank_profile_pic_grey_background)
                .error(R.drawable.ic_blank_profile_pic_grey_background)
                .format(DecodeFormat.PREFER_RGB_565)
                .into(profileButton);
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(userLocation != null){
            outState.putDouble(KEY_LAST_KNOWN_LAT, userLocation.latitude);
            outState.putDouble(KEY_LAST_KNOWN_LONG, userLocation.longitude);
        }
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onResume() {
        showAsNavigationContent(MainNavigation.newInstance());
        mapView.onResume();
        if(userLocation != null){
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,DEFAULT_ZOOM));
        }else if(getArguments() != null && getArguments().containsKey(KEY_LAST_KNOWN_LAT)){
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(getArguments().getDouble(KEY_LAST_KNOWN_LAT), getArguments().getDouble(KEY_LAST_KNOWN_LONG)),DEFAULT_ZOOM));
        }
        super.onResume();
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d("DEBUGGING", "MAP ready!");
        this.map = googleMap;
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_style));
        map.setOnCameraIdleListener(this);
        map.setOnMapLongClickListener(this);
        map.setOnMarkerClickListener(this);
        map.setOnCameraMoveStartedListener(this);
        if(userLocation != null){
            followUser = true;
            centerMapOnSelf(false);
        }
    }

    private void centerMapOnSelf(boolean animate) {
        if(animate){
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, DEFAULT_ZOOM));
        }else{
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, DEFAULT_ZOOM));
        }
    }

    private float getRadiusOfCurrentMapView(){
        VisibleRegion visibleRegion = map.getProjection().getVisibleRegion();

        LatLng farRight = visibleRegion.farRight;
        LatLng farLeft = visibleRegion.farLeft;
        LatLng nearRight = visibleRegion.nearRight;
        LatLng nearLeft = visibleRegion.nearLeft;

        float[] distanceWidth = new float[2];
        Location.distanceBetween(
                (farRight.latitude+nearRight.latitude)/2,
                (farRight.longitude+nearRight.longitude)/2,
                (farLeft.latitude+nearLeft.latitude)/2,
                (farLeft.longitude+nearLeft.longitude)/2,
                distanceWidth
        );


        float[] distanceHeight = new float[2];
        Location.distanceBetween(
                (farRight.latitude+nearRight.latitude)/2,
                (farRight.longitude+nearRight.longitude)/2,
                (farLeft.latitude+nearLeft.latitude)/2,
                (farLeft.longitude+nearLeft.longitude)/2,
                distanceHeight
        );

        float distance;

        if (distanceWidth[0]>distanceHeight[0]){
            distance = distanceWidth[0];
        } else {
            distance = distanceHeight[0];
        }

        return distance;
    }

    @Override
    public void onCameraIdle() {
        Log.d("DEBUGGING", "onCameraIdle");
        final CameraPosition currentCameraPos = map.getCameraPosition();
        final long radius = new Float(getRadiusOfCurrentMapView()).longValue();
        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        if(isSafe() && userLocation != null){
                            Log.d("DEBUGGING", "querying for location: " + userLocation.latitude + " " + userLocation.longitude + " within radius " + radius);
                            Set<Scrapbook> result = getScrapBookDAO().queryScrapbooksByLocation(new com.app.scrumble.model.group.scrapbook.Location(userLocation.latitude, userLocation.longitude), Math.min(radius, 10000));
                            if(result != null && result.size() > 0){
                                runOnUIThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                if(isSafe() && map != null && Objects.equals(currentCameraPos, map.getCameraPosition())){
                                                    for (Scrapbook scrapbook : result){
                                                        MarkerOptions options = new MarkerOptions();
                                                        options.position(new LatLng(scrapbook.getLocation().getLatitude(), scrapbook.getLocation().getLongitude()));
                                                        options.title(scrapbook.getTitle());
                                                        options.draggable(false);
                                                        options.icon(getCustomMarker(scrapbook));
                                                        Marker newMarker = map.addMarker(options);
                                                        if(newMarker != null){
                                                            newMarker.setTag(scrapbook.getID());
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                );
                            }else{
                                Log.d("DEBUGGING", "NO NEARBY SCRAPBOOKS");
                            }
                        }
                    }
                }
        );
    }

    private BitmapDescriptor getCustomMarker(Scrapbook scrapbook){

        // Inflate the ViewGroup from a layout XML file
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.marker_custom, null);

        // Set the width and height of the ViewGroup
        int height = 145;
        int width = 145;

        viewGroup.setLayoutParams(new ViewGroup.LayoutParams(width, height));

        // Measure and layout the ViewGroup
        viewGroup.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        viewGroup.layout(0, 0, viewGroup.getMeasuredWidth(), viewGroup.getMeasuredHeight());

        // Create a Bitmap with the same dimensions as the ViewGroup
        Bitmap bitmap = Bitmap.createBitmap(viewGroup.getWidth(), viewGroup.getHeight(), Bitmap.Config.ARGB_8888);

        // Create a Canvas with the Bitmap
        Canvas canvas = new Canvas(bitmap);

        // Draw the ViewGroup onto the Canvas
        viewGroup.draw(canvas);

        // Use the Bitmap as a marker icon
        return BitmapDescriptorFactory.fromBitmap(bitmap);

    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        showAsMainContent(NewSubmissionFragment.newInstance(new com.app.scrumble.model.group.scrapbook.Location(latLng.latitude, latLng.longitude)), true);
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        try {
            long scrapbookID = (long)marker.getTag();
            showAsMainContent(ScrapBookFragment.newInstance(scrapbookID), true);
            return true;
        }catch (Exception e){}
        return false;
    }

    @Override
    public void onCameraMoveStarted(int reason) {
        if(reason == OnCameraMoveStartedListener.REASON_GESTURE){
            followUser = false;
        }
    }
}
