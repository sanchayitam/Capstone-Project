package com.example.sanch.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.sanch.myapplication.Utils.WotdNotification;
import com.example.sanch.myapplication.tracker.AnalyticsApplication;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class MainActivity extends AppCompatActivity  implements VocabFragment.Callback {
    // variable to denote a multi-pane UI
    private boolean mTwoPane;
    private static final String TAG = "MainActivity";
    Toolbar mToolbar;
    private Tracker mTracker;
    WotdNotification wotdNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        mTracker.enableExceptionReporting(true);
        mTracker.enableAdvertisingIdCollection(true);
        mTracker.enableAutoActivityTracking(true);

        // [START screen_view_hit]
        Log.i(TAG, "Setting screen name: " + getResources().getString(R.string.screen_name1));
        mTracker.setScreenName( getResources().getString(R.string.screen_name1));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        // [END screen_view_hit]

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        // Display icon in the toolbar
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.vocab_logo);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        //Added FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent Email = new Intent(Intent.ACTION_SEND);
                Email.setData(Uri.parse("mailto:")); // only email apps should handle this
                Email.setType("text/plain");
                Email.putExtra(Intent.EXTRA_TEXT, "Text goes here");
                Email.putExtra(Intent.EXTRA_SUBJECT, "Vocabulary -- Feedback");
                try {
                    Intent Mailer = Intent.createChooser(Email, "Send Feedback");
                    startActivity(Mailer);
                    // startActivity(Email);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getApplicationContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

            if (findViewById(R.id.vocab_detail_container) != null) {
                mTwoPane = true;

            } else {
                mTwoPane = false;
            }

        //send notifications
        wotdNotification = new WotdNotification(this);
        wotdNotification.create_notification();
        wotdNotification.notifyWearable();
    }

    @Override
    public void onItemSelected(String wordItem, int index, String sort_key) {
           if (mTwoPane) {
           Bundle arguments = new Bundle();
           arguments.putString(DetailActivityFragment.DETAIL_VOCAB, wordItem);
           arguments.putString(DetailActivityFragment.SORT_BY, sort_key);
           DetailActivityFragment fragment = new DetailActivityFragment();
           fragment.setArguments(arguments);

                if (index == 0) {
                    //add a fragment by specifying the fragment to add and the view in which to insert it.
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.vocab_detail_container, fragment, DetailActivityFragment.TAG)
                           .commit();
                } else {
                    //replace  whatever is in the container view with this fragment
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.vocab_detail_container, fragment, DetailActivityFragment.TAG)
                            .commit();
                    ;
                }
               //For Favorite Fragment
        } else {
               //for single pane
                Intent intent = new Intent(this, DetailActivity.class)
                .putExtra(DetailActivityFragment.DETAIL_VOCAB, wordItem)
                .putExtra(DetailActivityFragment.SORT_BY, sort_key);
                startActivity(intent);
        }
        // [START custom_event]
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action On FlashCard selection")
                .setAction("LookUp")
                .setLabel(wordItem)
                .build());
        // [END custom_event]

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}