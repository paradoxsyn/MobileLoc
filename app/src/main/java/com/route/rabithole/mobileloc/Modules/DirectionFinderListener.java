package com.route.rabithole.mobileloc.Modules;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;



public interface DirectionFinderListener {
    void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<Route> route);
}