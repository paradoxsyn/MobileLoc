package com.route.rabithole.mobileloc;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.transit.realtime.GtfsRealtime;
import com.route.rabithole.mobileloc.Modules.DirectionFinder;
import com.route.rabithole.mobileloc.Modules.DirectionFinderListener;
import com.route.rabithole.mobileloc.Modules.Route;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PresenterTester {

    @Test
    public void checkDestinationAndOrigin(){
        MapsActivityPresenter m = mock(MapsActivityPresenter.class);
        when(m.getDestination()).thenReturn("Hi");
        assertEquals(m.getDestination(),"Hi");

        when(m.getOrigin()).thenReturn("Ok");
        assertEquals(m.getOrigin(),"Ok");
    }

    @Test
    public void testSetMap() {
        MapsActivityPresenter p = mock(MapsActivityPresenter.class);
        GoogleMap m = mock(GoogleMap.class);
        doNothing().when(p).setMap(isA(GoogleMap.class));
        p.setMap(m);

        verify(p,times(1)).setMap(m);
    }

    @Test
    public void sendRequestTest(){

        // LatLng origin = new LatLng(-34.397,150.644);
        // LatLng destination = new LatLng(-34.397,150.644);
        //
        MapsActivityPresenter p = mock(MapsActivityPresenter.class);
        DirectionFinderListener lr = mock(DirectionFinderListener.class);
        doNothing().when(p).sendRequest(any(DirectionFinderListener.class));
        p.sendRequest(lr);

        verify(p,times(1)).sendRequest(lr);

    }

    @Test
    public void testTravelMode(){
        MapsActivityPresenter p = mock(MapsActivityPresenter.class);
        doNothing().when(p).setTravelMode(any(Integer.class));
        p.setTravelMode(0);
        verify(p,times(1)).setTravelMode(0);
    }

    @Test
    public void testAddMarker(){
        MapsActivityPresenter p = mock(MapsActivityPresenter.class);
        Place test = mock(Place.class);
        doNothing().when(p).addMarker(any(Place.class));
        p.addMarker(test);
        verify(p,times(1)).addMarker(test);
    }

    @Test
    public void testAddBusMarker(){
        MapsActivityPresenter p = mock(MapsActivityPresenter.class);
        GtfsRealtime.Position test = mock(GtfsRealtime.Position.class);
        doNothing().when(p).addBusMarker(any(GtfsRealtime.Position.class));
        p.addBusMarker(test);
        verify(p,times(1)).addBusMarker(test);
    }

    @Test
    public void testResetMarkers(){
        MapsActivityPresenter p = mock(MapsActivityPresenter.class);
        doNothing().when(p).resetMarkers();
        p.resetMarkers();
        verify(p,times(1)).resetMarkers();
    }

    @Test
    public void testAddRoute(){
        MapsActivityPresenter p = mock(MapsActivityPresenter.class);
        List<Route> route = mock(ArrayList.class);
        GoogleMap m = mock(GoogleMap.class);

        doNothing().when(p).addRoute(isA(ArrayList.class),isA(GoogleMap.class));
        p.addRoute(route,m);
        verify(p,times(1)).addRoute(route,m);
    }
}
