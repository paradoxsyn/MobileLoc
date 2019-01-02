package com.route.rabithole.mobileloc;

import com.google.android.gms.maps.model.LatLng;
import com.route.rabithole.mobileloc.Modules.BusFinder;
import com.route.rabithole.mobileloc.Modules.BusFinderListener;
import com.route.rabithole.mobileloc.Modules.DirectionFinder;
import com.route.rabithole.mobileloc.Modules.DirectionFinderListener;

import junit.framework.TestCase;

import org.junit.Test;
import org.mockito.Mock;

public class BusTester extends TestCase {

    @Mock
    private BusFinder b;
    private BusFinderListener l;
    private LatLng ll;
    private String od;

    public BusTester(){
        this(null);
    }

    public BusTester(BusFinder b) {
        this.b = b;
    }

    @Test
    public void testInitString(){
        od = "l";
        b = new BusFinder(l,od,od);
        assertNotNull(b);
    }

    @Test
    public void testInitLtLng(){
        ll = new LatLng(1,1);
        b = new BusFinder(l,ll,ll);
        assertNotNull(b);
    }

    @Test
    public void testStringEmpty(){
        od = "";
        b = new BusFinder(l,od,od);
        assertTrue(od.isEmpty());

    }
}
