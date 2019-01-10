package com.route.rabithole.mobileloc.Modules;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.transit.realtime.GtfsRealtime;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BusFinder {

    private BusFinderListener listener;
    private String origin,destination;
    private GtfsRealtime.FeedMessage feed;
    private List<LatLng> latlngList;
    private LatLng originLL, destinationLL;
    Haversine haversine;
    private int minIndex;
    private List<Double> closest;
    private GtfsRealtime.FeedEntity closestEnt;

    public BusFinder(){

    }

    public BusFinder(BusFinderListener listener, String origin, String destination) {
        this.listener = listener;
        this.origin = origin;
        this.destination = destination;
        latlngList = new ArrayList<>();
        haversine = new Haversine();
        closest = new ArrayList<>();
    }

    public BusFinder(BusFinderListener listener, LatLng originLL, LatLng destinationLL) {
        this.listener = listener;
        this.originLL = originLL;
        this.destinationLL = destinationLL;
        latlngList = new ArrayList<>();
        haversine = new Haversine();
        closest = new ArrayList<>();
    }

    public void executeStops(String link) throws UnsupportedEncodingException {
        listener.onBusFinderStart();
        new BusFinder.RetrieveStops().execute(link);
    }

    public void executePosFeed(String link) throws UnsupportedEncodingException{
        listener.onBusFinderStart();
        new BusFinder.RetrieveFeed().execute(link);
    }

    private double compareRoutes(double latst, double lngst, double latend, double lngend){
        /*for(int i=0;i<bestRoutes.size();i++){
              if(busRoutes.get(i).equals(bestRoutes.get(i))){
                counter++;
            }
            //System.out.println("BUS ROUTES ARE" + busRoutes.get(i));
            //System.out.println("BEST ROUTES ARE" + bestRoutes.get(i));
        }*/

        closest.add(haversine.distance(latst,lngst,latend,lngend));
        minIndex = closest.indexOf(Collections.min(closest));

        //System.out.println(closest.get(minIndex));
        return closest.get(minIndex);
    }

    private class RetrieveFeed extends AsyncTask<String, Void, String> {
        // Feed for Bus Location
        @Override
        protected String doInBackground(String... params) {
            String link = params[0];
            try {
                URL url = new URL(link);
                feed = GtfsRealtime.FeedMessage.parseFrom(url.openStream());

            }catch(MalformedURLException e)
            {
                Log.e("Exception: %s", e.getMessage());
            } catch(IOException e)
            {
                Log.e("Exception: %s", e.getMessage());
            }catch (NullPointerException e){
                Log.e("Exception: %s", e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result){
            closestEnt = feed.getEntity(0);
            //Either compare the origin or the destination
            double closest = compareRoutes(destinationLL.latitude,destinationLL.longitude,closestEnt.getVehicle().getPosition().getLatitude(),closestEnt.getVehicle().getPosition().getLongitude());
            double closestO = compareRoutes(originLL.latitude,originLL.longitude,closestEnt.getVehicle().getPosition().getLatitude(),closestEnt.getVehicle().getPosition().getLongitude());
            double tmp, tmpO;
            for(GtfsRealtime.FeedEntity entity :feed.getEntityList())
            {
                //closest.add(entity.getVehicle().getPosition())
                tmp = compareRoutes(destinationLL.latitude,destinationLL.longitude,entity.getVehicle().getPosition().getLatitude(),entity.getVehicle().getPosition().getLongitude());
                //tmpO = compareRoutes(originLL.latitude,originLL.longitude,entity.getVehicle().getPosition().getLatitude(),entity.getVehicle().getPosition().getLongitude());
                if(tmp < closest){
                    //if(tmpO < closestO) {
                        closest = tmp;
                        //closestO = tmpO;
                        closestEnt = entity;
                    //}
                }
            }
            listener.onBusFinderFeedResponse(closestEnt);
            // Has to be executed on main thread.
        }
    }

    private class RetrieveStops extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... params) {
            String link = params[0];
            try {
                URL url = new URL(link);
                InputStream is = url.openConnection().getInputStream();
                StringBuilder buffer = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                return buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String res) {
            try {
                parseJSon(res);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void parseJSon(String data) throws JSONException {
            if (data == null) {
                return;
            }

            List<Route> routes = new ArrayList<Route>();
            JSONObject jsonData = new JSONObject(data);
            JSONArray jsonRoutes = jsonData.getJSONObject("Contents").getJSONObject("dataObjects").getJSONArray("ScheduledStopPoint");
            for (int i = 0; i < jsonRoutes.length(); i++) {
                JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
                Route route = new Route();

                JSONObject jsonLong = jsonRoute.getJSONObject("Location");
                JSONObject jsonLat = jsonRoute.getJSONObject("Location");
                latlngList.add(new LatLng(jsonLat.getDouble("Latitude"),jsonLong.getDouble("Longitude")));
                route.points = latlngList;
                //System.out.println("BUS STOP COORDS" + route.endLocation);
                routes.add(route);



            }

            listener.onBusFinderSuccess(routes);
        }
    }
}
