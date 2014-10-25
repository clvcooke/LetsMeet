package com.hack.letsmeet;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONObject;

/**
 * Created by bilal on 2014-09-21.
 */

/*

* Changed by Colin, 2014-10-23
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
    public static final int NOTIFICATION_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle stuff = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (messageType.equals(GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE)) {
            Log.d("GcmBroadcastReceiver", stuff.toString());
            String notType = stuff.getString("type");
            if (stuff.getString("type").equals("MeetupInitiated")) {
                sendNotification(context, stuff.getString("type"), stuff.getString("meeting"));
            } else {
                Intent newIntent = new Intent();
                String meetingString = stuff.getString("meeting");

                try {
                    MapsActivity.addMarkersForMeeting(new JSONObject(meetingString));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }


    }

    private void sendNotification(Context context, String msg, String meeting) {
        NotificationManager mNotificationManager= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(context, MapsActivity.class);
        intent.setAction("com.hack.letsmeet.MeetupInitiated");
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("isInitiated", true);
        intent.putExtra("meeting", meeting);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(android.R.drawable.ic_menu_add)
                        .setContentTitle("USER wants to meet")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
