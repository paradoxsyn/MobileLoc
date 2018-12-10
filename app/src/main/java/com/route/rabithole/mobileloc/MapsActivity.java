package com.route.rabithole.mobileloc;

import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.transit.realtime.GtfsRealtime;
import com.route.rabithole.mobileloc.Modules.DirectionFinder;
import com.route.rabithole.mobileloc.Modules.DirectionFinderListener;
import com.route.rabithole.mobileloc.Modules.Route;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,DirectionFinderListener {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int DEFAULT_ZOOM = 15;
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);

    private GoogleMap mMap;
    private boolean mLocationPermissionGranted = false;

    protected GeoDataClient mGeoDataClient;
    protected PlaceDetectionClient mPlaceDetectionClient;
    protected FusedLocationProviderClient mFusedLocationProviderClient;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    PlaceAutocompleteFragment placeAutoOrigin;
    PlaceAutocompleteFragment placeAutoDestination;

    private Button btnFindPath;
    private EditText editTextOrigin;
    private EditText editTextDestination;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarker = new ArrayList<>();
    private List<Polyline> polyLinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;

    private String origin,destination;
    private LatLng originLL, destinationLL;
    private String agencies;

    private boolean isT,isD,isW;
    private GtfsRealtime.FeedMessage feed;
    private URL url;
    private boolean done;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.showOverflowMenu();

        //Set bus as default route
        isT=true;

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Set strings and start a permissions check.
        origin = "";
        destination = "";
        agencies = "";
        addOrigin();
        addDestination();

        btnFindPath = (Button)findViewById(R.id.buttonFindPath);
        btnFindPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest();
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Create overflow menu for different mode of transportation options.
        getMenuInflater().inflate(R.menu.maps_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Check the different options.
        switch (item.getItemId()) {
            case R.id.walk_settings: {
                mMap.clear();
                isW=true;
                isT=false;
                isD=false;
                break;
            }

            case R.id.car_settings:{
                mMap.clear();
                isD=true;
                isW=false;
                isT=false;
                break;
            }

            case R.id.bus_settings:{
                mMap.clear();
                isT=true;
                isD=false;
                isW=false;
                break;
            }

            default:{
                mMap.clear();
                isT=true;
                isD=false;
                isW=false;
            }
            // case blocks for other MenuItems (if any)

        }
        return true;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();


    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result){
        // Hopefully this doesn't happen
        Log.e("Connection Failed", "Connection failed" + result.getErrorMessage());
    }

    private void updateLocationUI(){
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation(){
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         * MUST run Google Maps on a fresh device at least once.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            Log.d("error", "Current location is null. Using defaults.");
                            Log.e("error", "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void addMarker(Place p){
        // Pinpoints the location on the Google Map of the Place by adding a Marker.

        MarkerOptions markerOptions = new MarkerOptions();

        markerOptions.position(p.getLatLng());
        markerOptions.title(p.getName()+"");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

        mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(p.getLatLng()));
    }

    private void addBusMarker(GtfsRealtime.Position p){
        // Pinpoints the location of the bus on the Google Map by adding a Bus Marker.

        MarkerOptions markerOptions = new MarkerOptions();
        LatLng pos = new LatLng(p.getLatitude(),p.getLongitude());
        markerOptions.position(pos);
        markerOptions.title("BUS");
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.bus2));

        mMap.addMarker(markerOptions);
    }


    public void addOrigin(){

        placeAutoOrigin = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_origin);
        placeAutoOrigin.setHint("Type an origin point");
        placeAutoOrigin.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                Log.d("Maps", "Place selected: " + place.getName());
                addMarker(place);
                origin = place.getAddress().toString();
                originLL = place.getLatLng();
            }

            @Override
            public void onError(Status status) {
                Log.d("Maps", "An error occurred: " + status);
            }
        });
    }

    public void addDestination(){
        placeAutoDestination = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_destination);
        placeAutoDestination.setHint("Type a destination point");
        placeAutoDestination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                Log.d("Maps", "Place selected: " + place.getAddress().toString());
                addMarker(place);
                destination = place.getAddress().toString();
                destinationLL = place.getLatLng();
            }

            @Override
            public void onError(Status status) {
                Log.d("Maps", "An error occurred: " + status);
            }
        });
    }

    private void sendRequest() {
        //origin = editTextOrigin.getText().toString();
        //destination = editTextDestination.getText().toString();
        mMap.clear();

        if (origin.isEmpty()) {
            Toast.makeText(this, "Please enter origin!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()){
            Toast.makeText(this, "Please enter destination!", Toast.LENGTH_SHORT).show();
            return;
        }

        if(isT) {
            try {
                new DirectionFinder(this, originLL, destinationLL).executeT();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(isD) {
            try {
                new DirectionFinder(this, originLL, destinationLL).executeD();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(isW) {
            try {
                new DirectionFinder(this, originLL, destinationLL).executeW();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait", "Finding direction", true);
        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }
        if (destinationMarker != null) {
            for (Marker marker : destinationMarker) {
                marker.remove();
            }
        }
        if (polyLinePaths != null) {
            for (Polyline polylinePath : polyLinePaths) {
                polylinePath.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polyLinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarker = new ArrayList<>();

        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .title(route.startAddress)
                    .position(route.startLocation)));

            destinationMarker.add(mMap.addMarker(new MarkerOptions()
                    .title(route.endAddress)
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions()
                    .geodesic(true)
                    .color(Color.BLUE)
                    .width(10);

            for (int i = 0; i < route.points.size(); i++) {
                polylineOptions.add(route.points.get(i));
            }

            polyLinePaths.add(mMap.addPolyline(polylineOptions));


            // Some agencies have are filed under a different code, switch if needed.
            if(route.agencies.equals("511E")){
                agencies = "5E";
            }else if(route.agencies.equals("VTA")){
                agencies = "SC";
            }else if(route.agencies.equals("SamTrans")){
                agencies = "SM";
            }

        }
        // Send out the request.
        new RetrieveFeed().execute(("http://api.511.org/Transit/VehiclePositions?api_key=8bbfc4b0-ee7c-4ddd-8404-85cd8d678146&agency=" + agencies));
    }

    private class RetrieveFeed extends AsyncTask<String, Void, String> {
        // Feed for Bus Location
        @Override
        protected String doInBackground(String... params) {
            String link = params[0];
            try {
                url = new URL(link);
                feed = GtfsRealtime.FeedMessage.parseFrom(url.openStream());

                for(GtfsRealtime.FeedEntity entity :feed.getEntityList())
                {
                    if (entity.hasTripUpdate()) {
                        System.out.println(entity.getTripUpdate());
                    }
                }

            }catch(MalformedURLException e)
            {
                Log.e("Exception: %s", e.getMessage());
            } catch(IOException e)
            {
                Log.e("Exception: %s", e.getMessage());
            }catch (NullPointerException e){
                Log.e("Exception: %s", e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result){
            // Has to be executed on main thread.
            for(GtfsRealtime.FeedEntity entity :feed.getEntityList())
            {
                addBusMarker(entity.getVehicle().getPosition());
            }
        }
    }


}
