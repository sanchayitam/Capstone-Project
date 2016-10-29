package com.example.sanch.myapplication.widget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.widget.RemoteViews;
import com.example.sanch.myapplication.MainActivity;
import com.example.sanch.myapplication.R;
import com.example.sanch.myapplication.model.Definition;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;


public class WordAppWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        // To prevent any ANR timeouts, we perform the update in a service
        context.startService(new Intent(context, UpdateService.class));
    }

    public static class UpdateService extends Service {
        String mWord;
        String mPOS;
        String mDef;

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            // Build the widget update for today
            RemoteViews updateViews = buildUpdate(this);

            // Push update for this widget to the home screen
            ComponentName thisWidget = new ComponentName(this, WordAppWidget.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            manager.updateAppWidget(thisWidget, updateViews);

            return super.onStartCommand(intent, flags, startId);
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        public class getContentsTask extends AsyncTask<Context, Void, Definition> {


            protected Definition doInBackground(Context... context) {
                String base_url = "http://api.wordnik.com/v4/words.json/wordOfTheDay";
                Context mContext = context[0];

                String api_str = mContext.getString(R.string.wotd_api_key);

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
                    mWord = new String(content.getWord());
                    mPOS =  new String (content.getPartOfSpeech());
                    mDef =  new String(content.getDefinitions());
                }
            }
        }

        public RemoteViews buildUpdate(Context context) {

            getContentsTask FetchWotd = new getContentsTask();
            FetchWotd.execute(context);

            // Build an update that holds the updated widget contents
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.word_app_widget);

              remoteViews.setTextViewText(R.id.word, mWord);
            // remoteViews.setTextViewText(R.id.bid_price, mCursor.getString(mCursor.getColumnIndex(QuoteColumns.BIDPRICE)));

            remoteViews.setTextViewText(R.id.partOfSpeech, mPOS);
            remoteViews.setTextViewText(R.id.definitions, mDef);

            // Create intent to launch MainActivity
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,0);
            remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent);

            return remoteViews;
        }
    }
}


