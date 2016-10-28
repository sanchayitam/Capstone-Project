package com.example.sanch.myapplication.Utils;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.example.sanch.myapplication.MainActivity;
import com.example.sanch.myapplication.R;
import com.example.sanch.myapplication.model.Definition;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import static android.content.Context.NOTIFICATION_SERVICE;

public class WotdNotification {
    /**
     * The unique identifier for this type of notification.
     */
    private static final String NOTIFICATION_TAG = "Wotd";
    static private int NOTIFICATION_ID = 1;
    private Context mContext;
    static private NotificationManager mNotificationManager;
    static String mTitle;
    static String mText;

    private static final String WOTD_PATH = "/WOTD";
    public static final String NOTIFICATION_TITLE = "title";
    public static final String NOTIFICATION_CONTENT = "content";

    public WotdNotification(Context context) {
        this.mContext = context;
        //Get the notification manager
        mNotificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
    }

   public void create_notification() {
       WotdTask FetchWotd = new WotdTask();
       FetchWotd.execute();
       notify(mContext);
   }

    public static void notify(Context context) {

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        Notification.BigTextStyle bigStyle = new Notification.BigTextStyle();
        bigStyle.bigText(mText);

        Notification.Builder mBuilder =
                new Notification.Builder(context)

                        // Set appropriate defaults for the notification light, sound,
                        // and vibration.
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setSmallIcon(R.drawable.wotd)
                        .setContentTitle("WOTD :" + mTitle)
                        .setContentText(mText)
                       // .setStyle(bigStyle)
                        .setContentIntent(resultPendingIntent)     // Set the pending intent to be initiated when the user touches the notification.
                        // Automatically dismiss the notification when it is touched.
                        .setAutoCancel(true);

        //Show the notification
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private static void notify(final Context context, final Notification notification) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.notify(NOTIFICATION_TAG, 0, notification);
        } else {
            nm.notify(NOTIFICATION_TAG.hashCode(), notification);
        }
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public static void cancel(final Context context) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.cancel(NOTIFICATION_TAG, 0);
        } else {
            nm.cancel(NOTIFICATION_TAG.hashCode());
        }
    }

    public class WotdTask extends AsyncTask<Void, Void, Definition> {

        String base_url = "http://api.wordnik.com/v4/words.json/wordOfTheDay";

        String api_str = mContext.getString(R.string.wotd_api_key);

        protected Definition doInBackground(Void... voids) {
            try {
                    URL url = new URL(
                            base_url
                                    + "?api_key="
                                    +  api_str);
                                    //+ "de46aea2a06a6bd33572d005afc01f025e0a2875bc6a089e8");
                    URLConnection connection = url.openConnection();

                    JSONObject response = new JSONObject(
                        new BufferedReader(new InputStreamReader(
                                connection.getInputStream())).readLine());
                    Definition  definition  =  new Definition("", response.getString("word"),
                        response.getJSONArray("definitions").getJSONObject(0).getString("partOfSpeech"), "",
                        response.getJSONArray("definitions").getJSONObject(0).getString("text"), "");

                return definition;
            } catch (IOException e) {
            } catch (JSONException e) {
            }
            return null;
        }

        protected void onPostExecute(Definition content) {
            if (content != null) {
                mTitle =  String.format("%-25s%s", content.getWord(), content.getPartOfSpeech());
                mText = content.getDefinitions();
            }
            if (content == null) {
                mTitle = "";
                mText = mContext.getResources().getString(R.string.err_retrieving);
            }
        }
    }

    public void notifyWearable() {
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d(NOTIFICATION_TAG, "onConnected: " + connectionHint);
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.d(NOTIFICATION_TAG, "onConnectionSuspended: " + cause);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d(NOTIFICATION_TAG, "onConnectionFailed: " + result);
                    }
                })
                .addApi(Wearable.API)
                .build();

           if (mGoogleApiClient.isConnected()) {
                PutDataMapRequest dataMapRequest = PutDataMapRequest.create(WOTD_PATH);
                dataMapRequest.getDataMap().putString(NOTIFICATION_TITLE, mTitle);
                dataMapRequest.getDataMap().putString(NOTIFICATION_CONTENT, mText);
                PutDataRequest putDataRequest = dataMapRequest.asPutDataRequest();
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataRequest);
        } else {
                Log.e(NOTIFICATION_TAG, "No connection to wearable available!");
        }
    }
}