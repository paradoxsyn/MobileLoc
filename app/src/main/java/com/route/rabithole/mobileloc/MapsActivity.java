package com.route.rabithole.mobileloc;

import android.app.ProgressDialog;

import android.content.pm.PackageManager;


import android.support.annotation.NonNull;

import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import android.widget.ImageButton;

import android.support.v7.widget.Toolbar;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;

import com.google.android.gms.maps.GoogleMap;

import com.google.android.gms.maps.SupportMapFragment;

import com.google.transit.realtime.GtfsRealtime;

import com.route.rabithole.mobileloc.Modules.BusFinderListener;

import com.route.rabithole.mobileloc.Modules.DirectionFinderListener;

import com.route.rabithole.mobileloc.Modules.Map;
import com.route.rabithole.mobileloc.Modules.MapListener;
import com.route.rabithole.mobileloc.Modules.Route;
import java.util.List;


public class MapsActivity extends AppCompatActivity implements DirectionFinderListener, BusFinderListener, MapListener, MapsActivityPresenter.View {


    private Button btnFindPath;
    private ImageButton btnFindLocation;
    private ProgressDialog progressDialog;

    private GoogleMap mMap;
    private Map map;
    private MapsActivityPresenter view;
    private PlaceAutocompleteFragment placeAutoOrigin;
    private PlaceAutocompleteFragment placeAutoDestination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.showOverflowMenu();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        //Set bus as default route
        map = new Map(this);
        map.init(this,this, mapFragment);
        view = new MapsActivityPresenter(this);

        placeAutoOrigin = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_origin);
        placeAutoOrigin.setHint("Type an origin point");
        placeAutoDestination = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_destination);
        placeAutoDestination.setHint("Type a destination point");
        view.addDestination(placeAutoDestination);
        view.addOrigin(placeAutoOrigin);

        btnFindPath = (Button)findViewById(R.id.buttonFindPath);
        btnFindPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (view.getOrigin().isEmpty()) {
                    view.errorMsg(getApplicationContext(), "origin");
                    return;
                }
                if (view.getDestination().isEmpty()){
                    view.errorMsg(getApplicationContext(),"destination");
                    return;
                }
                sendRequest();
            }
        });


    }


    private void sendRequest(){
        view.sendRequest(this);
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
                view.setTravelMode(0);
                break;
            }

            case R.id.car_settings:{
               view.setTravelMode(1);
            }

            case R.id.bus_settings:{
                view.setTravelMode(2);
                break;
            }

            default:{
                view.setTravelMode(2);
                break;
            }
            // case blocks for other MenuItems (if any)

        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        map.setLocationPermission(false);
        if(requestCode == map.getPermissionsRequestAccessFineLocation()){
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    map.setLocationPermission(true);
                }
            }
        map.updateLocationUI();
    }



    @Override
    public void onDirectionFinderStart() {
        showProgressDialog("Loading Directions...");
        view.resetMarkers();
    }

    @Override
    public void onBusFinderStart() {
        showProgressDialog("Locating Buses...");
    }

    @Override
    public void onBusFinderSuccess(List<Route> routes){
        /*busRoutes = new ArrayList<>();

        for(Route route : routes){
            for(int i=0;i<route.points.size();i++) {
                busRoutes.add(route.points.get(i));
            }
        }*/
        //Shows the GTFS Locations for the Bus Routes of the specific agency
    }

    @Override
    public void onBusFinderFeedResponse(GtfsRealtime.FeedEntity entity){
        hideProgressDialog();
        view.addBusMarker(entity.getVehicle().getPosition());
    }

    @Override
    public void onMapStart(){

    }

    @Override
    public void onPlaceComplete(final Place place){
        btnFindLocation = (ImageButton)findViewById(R.id.my_location);
        btnFindLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.addMarker(place);
                placeAutoOrigin.setText(place.getAddress().toString());
            }
        });
    }

    @Override
    public void onMapSuccess(GoogleMap mMap){
        this.mMap = mMap;
        view.setMap(mMap);
    }

    @Override
    public void showProgressDialog(String message){
        progressDialog = ProgressDialog.show(this, "Please wait", message, true);
    }

    @Override
    public void hideProgressDialog(){
        progressDialog.dismiss();
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        hideProgressDialog();
        view.addRoute(routes, mMap);
        view.getBusPositions(this);
        // Send out the request.
        //System.out.println("http://api.511.org/Transit/VehiclePositions?api_key=8bbfc4b0-ee7c-4ddd-8404-85cd8d678146&agency=" + agencies);
        //new RetrieveFeed().execute(("http://api.511.org/Transit/VehiclePositions?api_key=8bbfc4b0-ee7c-4ddd-8404-85cd8d678146&agency=" + agencies));



    }


}
