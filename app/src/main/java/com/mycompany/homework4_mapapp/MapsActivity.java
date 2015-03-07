package com.mycompany.homework4_mapapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.maps.CameraUpdateFactory;
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
import android.view.View.OnClickListener;
import android.view.View;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity {
    static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();
    static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private static final String PLACES_API_KEY = "AIzaSyDmiTwUlmU7_N1hM8YoTBaTG4LMNNp6DVw";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/geocode/json?";
    public ArrayList<Marker> markers = new ArrayList();
    public Marker selectedMarker;

    private SupportMapFragment mapFragment;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    double latitude;
    double longitude;
    LatLng currentLoc;
    List<Address> geocodeMatches = null;
    Button search;
    public String locString = "blank";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //setUpMapIfNeeded();
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMap = mapFragment.getMap();
        //mMap.setMyLocationEnabled(true);
        Button search = (Button)findViewById(R.id.button);
        search.setOnClickListener(findClickListener);
        Button terrain = (Button)findViewById(R.id.terrain);
        terrain.setOnClickListener(terrainClickListener);
        Button hybrid = (Button)findViewById(R.id.hybrid);
        hybrid.setOnClickListener(hybridClickListener);
        Button normal = (Button)findViewById(R.id.normal);
        normal.setOnClickListener(normalClickListener);
        Button satellite = (Button)findViewById(R.id.satellite);
        satellite.setOnClickListener(satelliteClickListener);



    }
    // Defining button click event listener for the find button
    OnClickListener findClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            sendMessage(v);

        }
    };

    OnClickListener terrainClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switchTerrain(v);

        }
    };

    OnClickListener normalClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switchNormal(v);

        }
    };

    OnClickListener satelliteClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switchSatellite(v);

        }
    };
    OnClickListener hybridClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switchHybrid(v);

        }
    };

    public void sendMessage(View view){

        EditText editText =(EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();
        Update update = new Update();
        update.execute(message);
    }

    class Update extends AsyncTask<String, Void, Void> {


        @Override
        protected Void doInBackground(String... messages) {
            HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                    @Override
                    public void initialize(HttpRequest request) {
                        request.setParser(new JsonObjectParser(JSON_FACTORY));
                    }
                }
            );

            GenericUrl url = new GenericUrl(PLACES_API_BASE);
            url.put("address", messages[0]); //address

            HttpRequest request = null;
            try {
                request = requestFactory.buildGetRequest(url);
                HttpResponse httpResponse = request.execute();
                PlaceResults locationsResult = httpResponse.parseAs(PlaceResults.class);
                if(locationsResult.resultsList.size()>0){
                    currentLoc = new LatLng(locationsResult.resultsList.get(0).geometryList.location.lat,
                            locationsResult.resultsList.get(0).geometryList.location.lng);
                    locString = locationsResult.resultsList.get(0).address;

                }


            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //.onPostExecute(aVoid);
            mMap.addMarker(new MarkerOptions().position(currentLoc).title(locString));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 10));
        }
    }

    public void switchTerrain(View v){
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
    }

    public void switchHybrid(View v){
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    }

    public void switchSatellite(View v){
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
    }
    public void switchNormal(View v){
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //setUpMapIfNeeded();
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

    public static class PlaceResults {

        @Key("results")
        public List<Results> resultsList;

    }
    public static class Results {

        @Key("geometry")
        public Geometry geometryList;

        @Key("formatted_address")
        public String address;

    }

    public static class Geometry {

        @Key("location")
        public Location location;
    }

    public static class Location {
        @Key("lat")
        public double lat;

        @Key("lng")
        public double lng;
    }
}  // end of class MapsActivity

