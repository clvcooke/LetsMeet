package com.hack.letsmeet;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MapsActivity extends FragmentActivity {

    public static GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LocationManager locationManager;
    private Location userLocation;
    private ListView drawerList;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private String[] listItems = {"Food"};
    private Places places;
    public static Marker userMarker;
    public static HashMap<String, Marker> markerMap = new HashMap<String, Marker>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        places = new Places();
        List<String> list = new ArrayList<String>();
        list.add("food");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        userLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        MapsInitializer.initialize(this);

        setUpMapIfNeeded();

        //places.placeSearch(userLocation.getLatitude(), userLocation.getLongitude(), list, 200, mMap, this);


        drawerList = (ListView) findViewById(R.id.left_drawer);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, list));
        drawerList.setOnItemClickListener(new DrawerItemClickListener());

        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                drawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /**
             * Called when a drawer has settled in a completely closed state.
             */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //getActionBar().setTitle(mTitle);
            }

            /**
             * Called when a drawer has settled in a completely open state.
             */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //getActionBar().setTitle(mDrawerTitle);
            }
        };
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);




        GoogleMap.OnInfoWindowClickListener click = new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                double lat = marker.getPosition().latitude;
                double lon = marker.getPosition().longitude;

                if(marker.getTitle()!="Me") {
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse("http://maps.google.com/maps?saddr=" + userMarker.getPosition().latitude + "," + userMarker.getPosition().longitude + "&daddr=" + lat + "," + lon));
                    startActivity(intent);
                }
            }
        };




        mMap.setOnInfoWindowClickListener(click);




        /* make the API call */
       /* new Request(
                Session.getActiveSession(),
                "/me/friends",
                null,
                HttpMethod.GET,
                new Request.Callback() {
                    @Override
                    public void onCompleted(Response response) {
                    /* handle the result
                    System.out.println(response.toString());
                    }
                }
        ).executeAsync();*/

        //call places API
        Intent intent = getIntent();
        try {
            JSONObject meeting = new JSONObject(intent.getStringExtra("meeting"));
            if (meeting != null) {
                addMarkersForMeeting(meeting);
            }

            if (intent.getBooleanExtra("isInitiated", false) == true) {
                // Send our location to server

                //PARSE

                ParseObject locationObject = new ParseObject("locationObject");
                locationObject.put("Lat", 43);
                locationObject.put("Lon", -83);


         /*       JSONObject coords = new JSONObject();
                coords.put("lat", 43);
                coords.put("lon", -80);

                */


           /*     RestApi.getInstance().request(this, "meetup/"+meeting.getString("_id")+"/addLocation", RestApi.Method.POST, coords, new com.android.volley.Response.Listener() {
                    @Override
                    public void onResponse(Object o) {
                        JSONObject response = (JSONObject) o;

                        try {
                            JSONObject meeting = response.getJSONObject("meeting");
                            addMarkersForMeeting(meeting);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });*/
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);

    }

    public void refreshAdapter(){
        //TODO make this BETTER, MUCH MUCH MUCH BETTER
        drawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, places.placeNames));


    }

    public static void addMarker(LatLng latLng, String name, String snippet) {
       addMarker(latLng, name, snippet, BitmapDescriptorFactory.HUE_RED);
    }

    public static void addMarker(LatLng latLng, String name){

        addMarker(latLng,name,"");

    }

    public static void addMarker(LatLng latLng, String name, String snippet, float color){
        Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title(name).snippet(snippet).icon(BitmapDescriptorFactory.defaultMarker(color)));

        markerMap.put(name, marker);
    }

    public static void addMarkersForMeeting(JSONObject meeting) throws JSONException {
        if (meeting == null) {
            return;
        }

        if (meeting.getJSONObject("senderLocation") != null) {
            JSONObject loc = meeting.getJSONObject("receiverLocation");
            LatLng latlng = new LatLng(loc.getDouble("lat"), loc.getDouble("lon"));
            addMarker(latlng, "Sender location");
        }

        if (meeting.getJSONObject("receiverLocation") != null) {
            JSONObject loc = meeting.getJSONObject("receiverLocation");
            LatLng latlng = new LatLng(loc.getDouble("lat"), loc.getDouble("lon"));
            addMarker(latlng, "receiver location");
        }

        if (meeting.getJSONObject("meetupLocation") != null) {
            JSONObject loc = meeting.getJSONObject("meetupLocation");
            LatLng latlng = new LatLng(loc.getDouble("lat"), loc.getDouble("lon"));
            addMarker(latlng, "Meetup location","",BitmapDescriptorFactory.HUE_YELLOW);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        actionBarDrawerToggle.syncState();
    }
    @Override
    public void onStop() {
        super.onStop();

    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }

        private void selectItem(int position) {
            Marker marker = markerMap.get(places.placeNames.get(position));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(),18));
            drawerLayout.closeDrawer(drawerList);
            marker.showInfoWindow();
        }
    }
    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
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
             mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            //MapView mapView = ((MapView) findViewById(R.id.map));
            //mapView.onCreate(savedInstanceState);

           // mMap = ((MapView) findViewById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

        if (userLocation == null) {
            return;
        }

        LatLng userLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());


        userMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(userLocation.getLatitude(),
                userLocation.getLongitude())).title("Me"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng,18));
    }
}
