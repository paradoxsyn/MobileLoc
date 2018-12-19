package com.route.rabithole.mobileloc.Modules;

import com.google.transit.realtime.GtfsRealtime;

import java.util.List;

public interface BusFinderListener {
    void onBusFinderStart();
    void onBusFinderSuccess(List<Route> route);
    void onBusFinderFeedResponse(GtfsRealtime.FeedEntity entity);
}
