package com.mycompany.homework4_mapapp;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.util.Key;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity {
    static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();
    static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private static final String PLACES_API_KEY = "AIzaSyDmiTwUlmU7_N1hM8YoTBaTG4LMNNp6DVw";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/geocode/json?";
    private static final String TYPE_AUTOCOMPLETE = "address=";
    private static final String OUT_JSON = "";
    public ArrayList<Marker> markers = new ArrayList();
    public Marker selectedMarker;

    private SupportMapFragment mapFragment;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    double latitude;
    double longitude;
    LatLng currentLoc;
    List<Address> geocodeMatches = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMap = mapFragment.getMap();
        mMap.setMyLocationEnabled(true);


    }

    public void sendMessage(View view){

        EditText editText =(EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();

        HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                                                                                    @Override
                                                                                    public void initialize(HttpRequest request) {
                                                                                        request.setParser(new JsonObjectParser(JSON_FACTORY));
                                                                                    }
                                                                                }
        );


        GenericUrl url = new GenericUrl(PLACES_API_BASE);
        url.put("address", message); //address

        HttpRequest request = null;
        try {
            request = requestFactory.buildGetRequest(url);
            HttpResponse httpResponse = request.execute();
            Results locationsResult = httpResponse.parseAs(Results.class);
            if(locationsResult.resultsList.get(0)!=null){
                Double latitude = Double.parseDouble(locationsResult.resultsList.get(0).location.lat);
                Double longitude = Double.parseDouble(locationsResult.resultsList.get(0).location.lng);
                currentLoc = new LatLng(latitude, longitude);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }



    }


    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    public void addMarkerToMap(LatLng latLng) {
        Marker marker = mMap.addMarker(new MarkerOptions().position(latLng)
                .title("title")
                .snippet("snippet"));
        markers.add(marker);

    }
    public void clearMarkers() {
        mMap.clear();
        markers.clear();
    }

    public void removeSelectedMarker() {
        this.markers.remove(this.selectedMarker);
        this.selectedMarker.remove();
    }
    private void highLightMarker(int index) {
        highLightMarker(markers.get(index));
    }
    private void highLightMarker(Marker marker) {
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        marker.showInfoWindow();
        this.selectedMarker=marker;
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }


    public static class Results {

        @Key("results")
        public List<Geometry> resultsList;

    }

    public static class Geometry {

        @Key("geometry")
        public Location location;

    }


    public static class Location {
        @Key("lat")
        public String lat;

        @Key("lng")
        public String lng;

    }
}
