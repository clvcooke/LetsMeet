package com.hack.letsmeet;

import android.app.Activity;
import android.content.Intent;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.app.FragmentManager;
import android.util.Base64;
import android.util.Log;

import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;

//PARSE
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseUser;
//PARSE


import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Colin on 2014-09-20.
 */
public class MainActivity extends Activity {

    private static final int SPLASH = 0;
    private static final int SELECTION = 1;
    private static final int FRAGMENT_COUNT = SELECTION + 1;
    private boolean isResumed = false;
    private ArrayList<Fragment> fragments = new ArrayList<Fragment>();
    private Session.StatusCallback callback =
            new Session.StatusCallback() {
                @Override
                public void call(Session session,
                                 SessionState state, Exception exception) {
                    onSessionStateChange(session, state, exception);
                }
            };


    private UiLifecycleHelper uiHelper;
    @Override
    public void onCreate(Bundle savedInstanceState) {

        //PARSE

        Parse.initialize(this, "n1yOWtKGbnhLmCOOGGw2kh9PRuPw1YOwk7ZUusTc", "0yDdWDMksjvHnjAXmA4MSVEhoUNO6dftKG9lgW8p");
        ParseObject testObject = new ParseObject("TestObject");
        testObject.put("foo", "bar");
        testObject.saveInBackground();

        ParseObject latLonObject = new ParseObject("LatLonObject");
        latLonObject.put("Lat", 50);
        latLonObject.put("Lon", 50);
        latLonObject.saveInBackground();
        //PARSE

        //TODO add progress message
        List<String> permissions = Arrays.asList("public_profile", "user_friends", "user_about_me",
                "user_relationships", "user_birthday", "user_location");
        ParseFacebookUtils.logIn(permissions, this, new LogInCallback() {

            @Override
            public void done(ParseUser user, com.parse.ParseException e) {
                if (user == null) {
                    Log.d("NULL",
                            "Uh oh. The user cancelled the Facebook login.");
                } else if (user.isNew()) {
                    Log.d("NEW",
                            "User signed up and logged in through Facebook!");
                  /*  showUserDetailsActivity();*/
                } else {
                    Log.d("LOGGED IN",
                            "User logged in through Facebook!");
                    /*showUserDetailsActivity();*/
                }
            }
        });


        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.hack.letsmeet", PackageManager.GET_SIGNATURES);

            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (Exception e) {
            Log.wtf("KeyHash", "OMG IT DIDn't WORK!");
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);
        FragmentManager fm = getFragmentManager();
        fragments.add(SPLASH, fm.findFragmentById(R.id.splashFragment));
        fragments.add(SELECTION, fm.findFragmentById(R.id.selectionFragment));

        FragmentTransaction transaction = fm.beginTransaction();
        for (int i = 0; i < fragments.size(); i++) {
            transaction.hide(fragments.get(i));
        }

        transaction.show(fragments.get(0));
        transaction.commit();
    }

    private void showFragment(int fragmentIndex, boolean addToBackStack) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.show(fragments.get(fragmentIndex));
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        // Only make changes if the activity is visible
        if (isResumed) {
            FragmentManager manager = getFragmentManager();
            // Get the number of entries in the back stack
            int backStackSize = manager.getBackStackEntryCount();
            // Clear the back stack
            for (int i = 0; i < backStackSize; i++) {
                manager.popBackStack();
            }
            if (state.isOpened()) {
                // If the session state is open:
                // Show the authenticated fragment
             //   showFragment(SELECTION, false);

                MainActivity.this.launchPicker();

            } else if (state.isClosed()) {
                // If the session state is closed:
                // Show the login fragment
                showFragment(SPLASH, false);
            }
        }
    }

    private void signupAndRegisterDevice() {
        /*final RestApi restApi = RestApi.getInstance();

        Request request = Request.newGraphPathRequest(Session.getActiveSession(), "me", new Request.Callback(){

            @Override
            public void onCompleted(Response response) {
                Log.d("MainActivity", response.toString());

                String userid = (String) response.getGraphObject().getProperty("id");

                try {
                    JSONObject params = new JSONObject();
                    params.put("username", userid);

                    restApi.request(MainActivity.this, "signup", RestApi.Method.POST, params, new com.android.volley.Response.Listener() {
                        @Override
                        public void onResponse(Object o) {
                            Log.d("MainActivity", o.toString());
                        }
                    }, false);
                } catch (Exception e) {

                }
                String token = Session.getActiveSession().getAccessToken();

                restApi.setAuth(userid, token);

                MainActivity.this.launchPicker();
            }
        });

        Request.executeBatchAsync(request);*/
    }

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
        isResumed = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
        isResumed = false;
    }

    /*@Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        Session session = Session.getActiveSession();

        if (session != null && session.isOpened()) {
            // if the session is already open,
            // try to show the selection fragment
           launchMap();
        } else {
            // otherwise present the splash screen
            // and ask the person to login.
            showFragment(SPLASH, false);
        }
    }*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);

    }


    private final void launchPicker(){
        finish();

        Intent newIntent = new Intent(MainActivity.this, PickerActivity.class);
        startActivity(newIntent);
    }

}
