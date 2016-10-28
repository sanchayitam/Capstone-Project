package com.example.sanch.myapplication.widget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.text.format.Time;
import android.util.Log;
import android.widget.RemoteViews;


import com.example.sanch.myapplication.MainActivity;
import com.example.sanch.myapplication.R;
import com.example.sanch.myapplication.VocabFragment;
import com.example.sanch.myapplication.model.Definition;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Define a simple widget that shows the Wiktionary "Word of the day." To build
 * an update we spawn a background {@link Service} to perform the API queries.
 */
public class WordAppWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        // To prevent any ANR timeouts, we perform the update in a service

        for(int i = 0; i < appWidgetIds.length; ++i){

            RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.word_app_widget);
            // Create intent to launch MainActivity
            Intent intent = new Intent(context, MainActivity.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,0);
            updateViews.setOnClickPendingIntent(R.id.widget, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetIds[i], updateViews);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}


