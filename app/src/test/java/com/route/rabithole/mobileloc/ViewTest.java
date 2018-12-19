package com.route.rabithole.mobileloc;

import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.route.rabithole.mobileloc.Modules.Map;
import com.route.rabithole.mobileloc.Modules.MapListener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import static org.mockito.Mockito.verify;


@RunWith(RobolectricTestRunner.class)
public class ViewTest {



    @Mock
    private MapsActivityPresenter presenter;
    private MapsActivity activity;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        activity = new MapsActivity();
        presenter = new MapsActivityPresenter(activity);
    }

    @Test
    public void testLocationButtonPress(){
        activity = Robolectric.buildActivity(MapsActivity.class).create().get();
        ImageButton location = (ImageButton) activity.findViewById(R.id.my_location);
        location.performClick();

        ShadowApplication.runBackgroundTasks();
    }
}
