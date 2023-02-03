package com.app.scrumble;

import static android.content.Context.LOCATION_SERVICE;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.app.scrumble.model.scrapbook.Scrapbook;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener;
import com.google.android.gms.maps.GoogleMap.OnCameraMoveListener;
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import java.util.Objects;
import java.util.Set;

public class MapFragment extends BaseFragment implements OnMapReadyCallback, OnCameraIdleListener, OnMapLongClickListener, OnMarkerClickListener, OnCameraMoveStartedListener {

    public static final String NAME = "MAP";
    private static final String KEY_MARKER_ID = "KEY_MARKER_ID";
    private static final String KEY_LAST_KNOWN_LAT = "KEY_LAST_KNOWN_LAT";
    private static final String KEY_LAST_KNOWN_LONG = "KEY_LAST_KNOWN_LONG";

    private MapView mapView;

    private LocationManager locationManager;

    private GoogleMap map;

    private LatLng userLocation;
    private boolean followUser = true;

    private static final float DEFAULT_ZOOM = 16.0f;
    private BitmapDescriptor meMarker;

    public static MapFragment newInstance() {
        Bundle args = new Bundle();
        MapFragment fragment = new MapFragment();
        fragment.setArguments(args);
        return fragment;
    }

//    private BitmapDescriptor bitmapFromVector(int vectorResId) {
//// below line is use to generate a drawable.
//        Drawable vectorDrawable = ContextCompat.getDrawable(getActivity().getApplicationContext(), vectorResId);
////      below line is use to set bounds to our vector drawable.
//        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
//
////       below line is use to create a bitmap for our
////      drawable which we have added.
//        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
//
////      below line is use to add bitmap in our canvas.
//        Canvas canvas = new Canvas(bitmap);
//
////      below line is use to draw our
////      vector drawable in canvas.
//        vectorDrawable.draw(canvas);
//
////      after generating our bitmap we are returning our bitmap.
//        return BitmapDescriptorFactory.fromBitmap(bitmap);
//    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        meMarker = bitmapFromVector(R.drawable.baseline_emoji_people_24);
        locationManager = (LocationManager) getActivity().getApplicationContext().getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                LatLng newLocation = new LatLng(location.getLatitude(), location.getLongitude());
                if(userLocation == null || com.app.scrumble.model.scrapbook.Location.distanceBetween(convert(newLocation), convert(userLocation)) > 3){
                    userLocation = newLocation;
                    if(map != null && followUser){
                        centerMapOnSelf(true);
                    }
                }
            }
        });
    }

    private com.app.scrumble.model.scrapbook.Location convert(LatLng latLng){
        return new com.app.scrumble.model.scrapbook.Location(latLng.latitude, latLng.longitude);
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
                }
            }
        });
        ImageButton postButton = parentLayout.findViewById(R.id.button_new_post);
        postButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(map != null && userLocation != null){
                    showAsMainContent(NewSubmissionFragment.newInstance(new com.app.scrumble.model.scrapbook.Location(userLocation.latitude, userLocation.longitude)), true);
                }else{
                    Toast.makeText(getContext(), "Hang on, determining your location...", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return parentLayout;
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
        final CameraPosition currentCameraPos = map.getCameraPosition();
        final long radius = new Float(getRadiusOfCurrentMapView()).longValue();
        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        if(isSafe() && userLocation != null){
                            Set<Scrapbook> result = getScrapBookDAO().queryScrapbooksByLocation(new com.app.scrumble.model.scrapbook.Location(userLocation.latitude, userLocation.longitude), Math.min(radius, 10000));
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

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        showAsMainContent(NewSubmissionFragment.newInstance(new com.app.scrumble.model.scrapbook.Location(latLng.latitude, latLng.longitude)), true);
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
