package com.hack.letsmeet.googlePlay;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.parse.ParseObject;

import java.io.IOException;

/**
 * Created by Colin on 2014-10-23.
 */
//Shell class for static methods to do with google services

public class ServiceInteraction {


    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    //TODO replace this id
    private static final String SENDER_ID = "ChangeThisID";
    private static GoogleCloudMessaging gcm;
    private static String regid;
    private static Context m_context;

    public static boolean checkForGooglePlay(Context context){


        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {

            //No google play, too bad
            Log.d("GOOGLE", "google play services could not be found");
            return false;
        }

        return true;
    }

    public static void registerGCM(Context context){
        m_context = context;

        if(checkForGooglePlay(context)){
        gcm =  GoogleCloudMessaging.getInstance(context);
        regid = getRegistrationId(context);

        if(regid.isEmpty()){
            registerInBackground();
        }
        }else{
            Log.i("REG", "No valid Google Play Services APK found.");
        }
    }

    public static String getRegistrationId(Context context){
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");

        if (registrationId.isEmpty()){
            Log.i("REG", "Registration not found");
            return "";
        }


        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion){
            Log.i("REG", "App version changed.");
            return "";
        }

        return registrationId;

    }

    private static SharedPreferences getGCMPreferences(Context context) {
       return context.getSharedPreferences(ServiceInteraction.class.getSimpleName(), Context.MODE_PRIVATE);
    }



    private static int getAppVersion(Context context){
       try{
           PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
           return packageInfo.versionCode;
       }catch(PackageManager.NameNotFoundException e){
           //this should not happen
           throw new RuntimeException("Could not get package name, very bad: " + e);
       }
    }


    private static void registerInBackground(){
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try{
                    if(gcm == null){
                        gcm = GoogleCloudMessaging.getInstance(m_context);
                    }
                    regid = gcm.register(SENDER_ID);

                    msg ="Device registered, registration ID=" + regid;



                    //Sending regid to parse and storing in cloud, associate this regid with the friend ID;
                    sendRegIdToParase(regid);


                    storeRegistrationId(m_context, regid);
                }catch(IOException ex){
                    msg = "Error :" + ex.getMessage();
                }

                return msg;

            }

           @Override
            protected void onPostExecute(String msg){
                Log.i("REG",msg);


           }
        }.execute(null, null, null);





    }

    private static void sendRegIdToParase(String regid) {

        ParseObject regObject = new ParseObject("regID");
        regObject.put("ID", regid);
        regObject.saveInBackground();

    }

    private static void storeRegistrationId(Context context, String regId){
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i("REG", "Saving regId on app version "+ appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

}
