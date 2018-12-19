package com.route.rabithole.mobileloc.Modules;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

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
import java.util.List;


public class DirectionFinder {
    private static final String DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String GOOGLE_API_KEY = "AIzaSyDlDlomIKcjlA7-og1jSoZZjFddIxUJ8nM";
    private DirectionFinderListener listener;
    private String origin;
    private String destination;
    private String mode;
    private LatLng originLL, destinationLL;
    private double orilat,orilong,destlat,destlong;

    public DirectionFinder(){

    }

    public DirectionFinder(DirectionFinderListener listener, String origin, String destination) {
        this.listener = listener;
        this.origin = origin;
        this.destination = destination;
    }

    public DirectionFinder(DirectionFinderListener listener, LatLng origin, LatLng destination){
        this.listener = listener;
        this.originLL = origin;
        this.destinationLL = destination;

        orilat = originLL.latitude;
        orilong = originLL.longitude;
        destlat = destinationLL.latitude;
        destlong = destinationLL.longitude;
    }

    public void executeT() throws UnsupportedEncodingException {
        listener.onDirectionFinderStart();
        new DownloadRawData().execute(createUrlTransit());
    }

    public void executeD() throws UnsupportedEncodingException {
        listener.onDirectionFinderStart();
        new DownloadRawData().execute(createUrlDriving());
    }

    public void executeW() throws UnsupportedEncodingException {
        listener.onDirectionFinderStart();
        new DownloadRawData().execute(createUrlWalking());
    }

    private String createUrlTransit() throws UnsupportedEncodingException {
        //String urlOrigin = URLEncoder.encode(origin, "utf-8");
        //String urlDestination = URLEncoder.encode(destination, "utf-8");
        String end = DIRECTION_URL_API + "origin=" + orilat + "," + orilong + "&destination=" + destlat + "," + destlong + "&key=" + GOOGLE_API_KEY + "&mode=transit"  + "&transit_mode=bus";
        System.out.println(end);
        return DIRECTION_URL_API + "origin=" + orilat + "," + orilong + "&destination=" + destlat + "," + destlong + "&key=" + GOOGLE_API_KEY + "&mode=transit" + "&transit_mode=bus";
    }

    private String createUrlWalking() throws UnsupportedEncodingException {
        //String urlOrigin = URLEncoder.encode(origin, "utf-8");
        //String urlDestination = URLEncoder.encode(destination, "utf-8");
        String end = DIRECTION_URL_API + "origin=" + orilat + "," + orilong + "&destination=" + destlat + "," + destlong + "&key=" + GOOGLE_API_KEY + "&mode=walking";
        System.out.println(end);
        return DIRECTION_URL_API + "origin=" + orilat + "," + orilong + "&destination=" + destlat + "," + destlong + "&key=" + GOOGLE_API_KEY + "&mode=walking";
    }

    private String createUrlDriving() throws UnsupportedEncodingException {
        //String urlOrigin = URLEncoder.encode(origin, "utf-8");
        //String urlDestination = URLEncoder.encode(destination, "utf-8");
        String end = DIRECTION_URL_API + "origin=" + orilat + "," + orilong + "&destination=" + destlat + "," + destlong + "&key=" + GOOGLE_API_KEY + "&mode=driving";
        System.out.println(end);
        return DIRECTION_URL_API + "origin=" + orilat + "," + orilong + "&destination=" + destlat + "," + destlong + "&key=" + GOOGLE_API_KEY + "&mode=driving";
    }

    private class DownloadRawData extends AsyncTask<String, Void, String> {

        //Open the downloaded data in a separate thread and iterate through it

        @Override
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
    }

    private void parseJSon(String data) throws JSONException {
        if (data == null) {
            return;
        }

        List<Route> routes = new ArrayList<Route>();
        JSONObject jsonData = new JSONObject(data);
        JSONArray jsonRoutes = jsonData.getJSONArray("routes");
        for (int i = 0; i < jsonRoutes.length(); i++) {
            JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
            Route route = new Route();

            JSONObject overview_polylineJson = jsonRoute.getJSONObject("overview_polyline");
            JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
            JSONObject jsonLeg = jsonLegs.getJSONObject(0);
            JSONArray jsonSteps = jsonLeg.getJSONArray("steps");
            JSONObject jsonStep = jsonSteps.getJSONObject(1);
            JSONObject jsonTrans = jsonStep.getJSONObject("transit_details");//FINALLY
            JSONObject jsonLine = jsonTrans.getJSONObject("line");
            JSONArray jsonAgency = jsonLine.getJSONArray("agencies");
            JSONObject jsonEndLocation = jsonLeg.getJSONObject("end_location");
            JSONObject jsonStartLocation = jsonLeg.getJSONObject("start_location");


            route.endAddress = jsonLeg.getString("end_address");
            route.startAddress = jsonLeg.getString("start_address");
            route.startLocation = new LatLng(jsonStartLocation.getDouble("lat"), jsonStartLocation.getDouble("lng"));
            route.endLocation = new LatLng(jsonEndLocation.getDouble("lat"), jsonEndLocation.getDouble("lng"));
            route.points = decodePolyLine(overview_polylineJson.getString("points"));
            route.agencies = jsonAgency.getJSONObject(0).getString("name");

            routes.add(route);



        }

        listener.onDirectionFinderSuccess(routes);
    }

    private List<LatLng> decodePolyLine(final String poly) {
        // Based off Google Maps docs

        int len = poly.length();
        int index = 0;
        List<LatLng> decoded = new ArrayList<LatLng>();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            decoded.add(new LatLng(
                    lat / 100000d, lng / 100000d
            ));
        }

        return decoded;
    }
}