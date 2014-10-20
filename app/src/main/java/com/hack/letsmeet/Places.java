package com.hack.letsmeet;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Colin on 2014-09-20.
 */
public class Places {

    private static final String baseURI = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
    private static final String API_KEY = "AIzaSyBgzXDHVUbDBzZSbfI-q5EcoiMFmNpWBxw";


    private MapsActivity mapActivityContext;
    public List<HashMap<String,String>> placeData = new ArrayList<HashMap<String, String>>();
    public List<String> placeNames = new ArrayList<String>();
    private GoogleMap m_map;

// JSON:  https://maps.googleapis.com/maps/api/place/nearbysearch/json?
// location=-33.8670522,151.1957362&radius=500&types=food&name=cruise&key=AddYourOwnKeyHere

    public void placeSearch(double lat, double lon, List<String> types, int radiusInMetres, GoogleMap map, MapsActivity context){

        m_map =map;
        mapActivityContext = context;

        String urlString = baseURI;

        urlString += "location=" + lat + "," + lon;
        urlString += "&radius=" + radiusInMetres;
        urlString += "&types=";
        for(String type : types){
            if(!urlString.endsWith("="))
                urlString += "|";
            urlString += type;
        }

        urlString += "&key=" + API_KEY;

        PlacesTask placesTask = new PlacesTask();

        placesTask.execute(urlString);





    }

 //JSON OBJ to list


    public List<HashMap<String, String>> parse (JSONObject jsonObject){
        JSONArray jPlaces = null;

        try{
            jPlaces = jsonObject.getJSONArray("results");
        }catch (JSONException e){
            //TODO handle this properly

        }


        return getPlaces(jPlaces);
    }

    private List<HashMap<String, String>> getPlaces(JSONArray jPlaces) {
        int placesCount = jPlaces.length();
        List<HashMap<String, String>> placesList = new ArrayList<HashMap<String, String>>();
        HashMap<String,String> place = null;

        for(int i = 0; i < placesCount; i++){
            try{
                place = getPlace((JSONObject)jPlaces.get(i));
                placesList.add(place);
            }catch (JSONException e){
                //TODO handle this properly
            }
        }

        return placesList;
    }

    private HashMap<String, String> getPlace(JSONObject jPlace) {
        HashMap<String, String> place = new HashMap<String, String>();
        String placeName = "-NA-";
        String vicinity = "-NA-";
        String lat = "";
        String lon = "";

        try{
            //extracting place name if its there
            if(!jPlace.isNull("name")){
                placeName = jPlace.getString("name");
            }

            if(!jPlace.isNull("vicinity")){
                vicinity = jPlace.getString("vicinity");
            }

            lat = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lat");
            lon = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lng");

            place.put("place_name", placeName);
            place.put("vicinity", vicinity);
            place.put("lat", lat);
            place.put("lon", lon);


        }catch (JSONException e) {
         e.printStackTrace();
        }
        return place;
    }




    private class PlacesTask extends AsyncTask<String, Integer, String>{


        String data = null;

        @Override
        protected String doInBackground(String... url) {

            try{
                data = downloadURL(url[0]);
            }catch (Exception e){
                Log.d("Background Task", e.toString());
            }

            return  data;

        }



        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String result) {

            ParserTask parserTask = new ParserTask();

            parserTask.execute(result);
        }
    }

    private String downloadURL(String strURL) throws IOException {

        String data = "";
        InputStream inputStream = null;
        HttpURLConnection urlConnection   = null;

        try {
            URL url = new URL(strURL);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            inputStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader((new InputStreamReader(inputStream)));

            StringBuffer sb = new StringBuffer();

            String line = "";

            while ((line = br.readLine()) != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch (Exception e){
            Log.d("An Exception has occurred when downloading the data: ", e.toString());
        }finally{
            inputStream.close();
            urlConnection.disconnect();
        }

        return data;
    }

    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>>{

        JSONObject jObject;

        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {
            List<HashMap<String,String>> places = null;
            try{
                jObject = new JSONObject(jsonData[0]);

                places = parse(jObject);
            }catch (Exception e){
                Log.d("Exception",e.toString());
            }
            return places;
        }

       @Override
       protected void onPostExecute(List<HashMap<String,String>> list){

           //add data to map and stuff
           placeData = list;

           for(int i = 0; i < list.size(); i++){

               // Getting a place from the places list
               HashMap<String, String> hmPlace = list.get(i);

               // Getting latitude of the place
               double lat = Double.parseDouble(hmPlace.get("lat"));

               // Getting longitude of the place
               double lng = Double.parseDouble(hmPlace.get("lon"));

               // Getting name
               String name = hmPlace.get("place_name");

               // Getting vicinity
               String vicinity = hmPlace.get("vicinity");

               //overload with snippet ect later
               MapsActivity.addMarker(new LatLng(lat, lng), name, vicinity, BitmapDescriptorFactory.HUE_VIOLET);
               placeNames.add(name);
           }

            mapActivityContext.refreshAdapter();
//           mapActivityContext.userMarker.showInfoWindow();



       }
    }


}
