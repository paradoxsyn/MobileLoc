package com.route.rabithole.mobileloc;


import com.route.rabithole.mobileloc.Modules.Map;

import org.junit.Assert;

import org.junit.Test;

import static org.mockito.Mockito.mock;

public class MapTester {


    @Test
    public void testPlace(){
        Map m = mock(Map.class);
        try{
            m.getCurrentPlace();
        }catch(NullPointerException e){
            Assert.fail("Exception" + e);
        }
    }


}
