package com.route.rabithole.mobileloc.Modules;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.GoogleMap;

import java.util.List;

public interface MapListener {

    void onMapStart();
    void onMapSuccess(GoogleMap mMap);
    void onPlaceComplete(Place place);
}
