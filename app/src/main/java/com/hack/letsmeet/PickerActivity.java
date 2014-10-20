package com.hack.letsmeet;

import android.app.AlertDialog;
//import android.app.Fragment;
//import android.app.FragmentManager;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.android.volley.Response;
import com.facebook.FacebookException;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.facebook.widget.FriendPickerFragment;
import com.facebook.widget.PickerFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by edward on 20/09/14.
 */
public class PickerActivity extends ListActivity {
    public static final Uri FRIEND_PICKER = Uri.parse("picker://friend");

    public static final String SENDER_ID="831680996472";

    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private String m_name;
    private String m_id;


    GoogleCloudMessaging gcm;
    String regid;

    Context context;

    FriendsListAdapter listAdapter;

    private static final ArrayList<String> PERMISSIONS = new ArrayList<String>() {

        {
            add("user_friends");

            add("public_profile");
        }
    };



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listAdapter = new FriendsListAdapter(this, android.R.layout.simple_list_item_1, new ArrayList<Friend>());
        setListAdapter(listAdapter);

        context = getApplicationContext();

        Bundle args = getIntent().getExtras();

        Uri intentUri = getIntent().getData();

        new Request(
                Session.getActiveSession(),
                "/me/friends",
                null,
                HttpMethod.GET,
                new Request.Callback() {
                    @Override
                    public void onCompleted(com.facebook.Response response) {
                     GraphObject graphObject  = response.getGraphObject();
                     JSONArray data = (JSONArray) graphObject.getProperty("data");
                     List<Friend> friendsList = new ArrayList<Friend>();

                        try {
                            for (int i=0;i<data.length();i++) {
                                JSONObject object = data.getJSONObject(i);

                                Friend friend = new Friend(object.getString("id"), object.getString("name"));
                                friendsList.add(friend);
                            }

                        }catch (JSONException e){
                            e.printStackTrace();
                        }

                        Log.d("g", data.toString());
                        listAdapter.clear();
                        listAdapter.addAll(friendsList);
                        listAdapter.notifyDataSetChanged();
                    }
                }
        ).executeAsync();

        /*RestApi.getInstance()
                .request(this, "test-auth", RestApi.Method.GET, new JSONObject(), new com.android.volley.Response.Listener() {
            @Override
            public void onResponse(Object o) {
                Log.d("MainActivity", o.toString());
            }
        });*/

        //if (FRIEND_PICKER.equals(intentUri)) {


        /*} else {
            // Nothing to do, finish
            setResult(RESULT_CANCELED);
            //finish();
            //return;
        }*/


        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(this);

            if (regid.isEmpty()) {
                registerInBackground();
            }
        } else {
            Log.i("PickerActivity", "No valid Google Play Services APK found.");
        }
    }

    private void registerInBackground() {
        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... voids) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    JSONObject params = new JSONObject();
                    params.put("registrationId", regid);
                    RestApi.getInstance().request(context, "addDevice", RestApi.Method.POST, params, new Response.Listener() {
                        @Override
                        public void onResponse(Object o) {
                            Log.d("PickerActivity", o.toString());
                        }
                    });

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;

            }
        }.execute();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("PickerActivity", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i("PickerActivity", "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    private void onError(Exception error) {
        onError(error.getLocalizedMessage(), false);
    }

    private void onError(String error, final boolean finishActivity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.error_dialog_title).
                setMessage(error).
                setPositiveButton(R.string.error_dialog_button_text,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (finishActivity) {
                                    finishActivity();
                                }
                            }
                        });
        builder.show();
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i("PickerActivity", "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i("PickerActivity", "App version changed.");
            return "";
        }
        return registrationId;
    }

    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(PickerActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void finishActivity() {
        setResult(RESULT_OK, null);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }
}
