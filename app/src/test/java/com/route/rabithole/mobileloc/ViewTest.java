package com.route.rabithole.mobileloc;


import android.view.KeyEvent;

import android.widget.ImageButton;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import org.robolectric.shadows.ShadowApplication;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;



@RunWith(RobolectricTestRunner.class)
public class ViewTest {



    @Mock
    private MapsActivityPresenter presenter;

    @Mock
    private MapsActivity activity;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        activity = new MapsActivity();
        presenter = new MapsActivityPresenter(activity);
        activity = Robolectric.buildActivity(MapsActivity.class).create().visible().get();
    }

    @Test
    public void testLocationButtonPress(){

        ImageButton location = (ImageButton) activity.findViewById(R.id.my_location);
        location.performClick();

        ShadowApplication.runBackgroundTasks();
    }

    @Test
    public void testProgressDialog(){
        MapsActivity m = mock(MapsActivity.class);
        m.showProgressDialog("test");
        m.hideProgressDialog();
    }

    @Test
    public void testOptionsMenu() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_MENU);
                getInstrumentation().invokeMenuActionSync(activity, R.id.walk_settings, 0);
                assertEquals(true, getInstrumentation().invokeContextMenuAction(activity, R.id.walk_settings,0));
            }
        });


    }

    @Test
    public void testSendRequest() {
        //Try to make encapsulated as possible, sendRequest is private
    }
}
