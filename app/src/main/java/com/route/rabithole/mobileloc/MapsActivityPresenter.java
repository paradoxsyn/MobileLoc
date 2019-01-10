package com.route.rabithole.mobileloc;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.transit.realtime.GtfsRealtime;
import com.route.rabithole.mobileloc.Modules.BusFinder;
import com.route.rabithole.mobileloc.Modules.BusFinderListener;
import com.route.rabithole.mobileloc.Modules.DirectionFinder;
import com.route.rabithole.mobileloc.Modules.DirectionFinderListener;
import com.route.rabithole.mobileloc.Modules.Route;

import java.util.ArrayList;
import java.util.List;

public class MapsActivityPresenter {

    private View view;
    private GoogleMap mMap;
    private BusFinder bus_finder;
    private DirectionFinder direction_finder;

    private boolean isT,isD,isW;
    private LatLng originLL,destinationLL;

    private String origin,destination;
    private String agencies;

    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarker = new ArrayList<>();
    private List<Polyline> polyLinePaths = new ArrayList<>();

    private List<LatLng> busRoutes;
    private List<LatLng> bestRoutes;

    private Handler timer;

    public MapsActivityPresenter(View view){
        this.view = view;

        isT=true;
        isD=false;
        isW=false;

        // Set strings
        origin = "";
        destination = "";
        agencies = "";
    }

    public void setMap(GoogleMap map){
        this.mMap = map;
    }

    public void sendRequest(DirectionFinderListener listener) {
        //origin = editTextOrigin.getText().toString();
        //destination = editTextDestination.getText().toString();
        mMap.clear();

        if(isT) {
            try {
                new DirectionFinder(listener, originLL, destinationLL).executeT();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(isD) {
            try {
                new DirectionFinder(listener, originLL, destinationLL).executeD();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(isW) {
            try {
                new DirectionFinder(listener, originLL, destinationLL).executeW();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public BusFinder getBusFinder(){
        return bus_finder;
    }

    public void getBusPositions(BusFinderListener listener){
        try {
           //bus_finder = new BusFinder(listener, "http://api.511.org/transit/stops?api_key=8bbfc4b0-ee7c-4ddd-8404-85cd8d678146&operator_id=" + agencies,originLL,destinationLL);
           bus_finder = new BusFinder(listener,originLL,destinationLL);
           bus_finder.executePosFeed("http://api.511.org/Transit/VehiclePositions?api_key=8bbfc4b0-ee7c-4ddd-8404-85cd8d678146&agency="+ agencies);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setTravelMode(int travelMode){

        switch(travelMode)
        {
            case 0:{
                mMap.clear();
                isW=true;
                isT=false;
                isD=false;
            }
            case 1:{
                mMap.clear();
                isD=true;
                isW=false;
                isT=false;
                break;
            }
            case 2:{
                mMap.clear();
                isT=true;
                isD=false;
                isW=false;
            }
            default:{
                mMap.clear();
                isT=true;
                isD=false;
                isW=false;
            }
        }

    }

    public void addMarker(Place p){
        // Pinpoints the location on the Google Map of the Place by adding a Marker.

        MarkerOptions markerOptions = new MarkerOptions();

        markerOptions.position(p.getLatLng());
        markerOptions.title(p.getName()+"");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

        mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(p.getLatLng()));
    }

    public void resetMarkers(){
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

    public void addBusMarker(GtfsRealtime.Position p){
        // Pinpoints the location of the bus on the Google Map by adding a Bus Marker.

        MarkerOptions markerOptions = new MarkerOptions();
        LatLng pos = new LatLng(p.getLatitude(),p.getLongitude());
        markerOptions.position(pos);
        markerOptions.title("BUS");
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.bus2));

        mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
    }

    public void addOrigin(PlaceAutocompleteFragment frag){

        frag.setOnPlaceSelectedListener(new PlaceSelectionListener() {
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

    public String getOrigin(){
        return origin;
    }

    public void addDestination(PlaceAutocompleteFragment frag){
        frag.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                Log.d("Maps", "Place selected: " + place.getAddress().toString());
                addMarker(place);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 16));
                destination = place.getAddress().toString();
                destinationLL = place.getLatLng();
            }

            @Override
            public void onError(Status status) {
                Log.d("Maps", "An error occurred: " + status);
            }
        });
    }

    public String getDestination(){
        return destination;
    }

    public void addRoute(List<Route> routes, GoogleMap mMap){
        polyLinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarker = new ArrayList<>();
        bestRoutes = new ArrayList<>();

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
                bestRoutes.add(route.points.get(i));
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
    }

    public void errorMsg(Context context, String which){
        Toast t = new Toast(context);
        t.makeText(context,"Please enter" + which + "!", Toast.LENGTH_SHORT).show();

    }


    public interface View{
        void showProgressDialog(String message);
        void hideProgressDialog();
    }

}


