package com.route.rabithole.mobileloc.Modules;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class Route {
    public String endAddress;
    public LatLng endLocation;
    public String startAddress;
    public LatLng startLocation;
    public String agencies;
    public List<LatLng> points;
}
