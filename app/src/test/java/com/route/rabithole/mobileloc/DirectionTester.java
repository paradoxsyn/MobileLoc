package com.route.rabithole.mobileloc;

import com.google.android.gms.maps.model.LatLng;
import com.route.rabithole.mobileloc.Modules.DirectionFinder;
import com.route.rabithole.mobileloc.Modules.DirectionFinderListener;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.UnsupportedEncodingException;

public class DirectionTester extends TestCase {


    @Mock
    private DirectionFinder d;
    private DirectionFinderListener l;
    private LatLng ll;
    private String od;

    public DirectionTester(){
        this(null);
    }

    public DirectionTester(DirectionFinder d) {
        this.d = d;
    }

    @Test
    public void testInitString(){
        od = "l";
        d = new DirectionFinder(l,od,od);
        assertNotNull(d);
    }

    @Test
    public void testInitLtLng(){
        ll = new LatLng(1,1);
        d = new DirectionFinder(l,ll,ll);
        assertNotNull(d);
    }

    @Test
    public void testStringEmpty(){
        od = "";
        d = new DirectionFinder(l,od,od);
        assertTrue(od.isEmpty());

    }
}
