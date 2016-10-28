package com.example.sanch.myapplication.service;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.util.Log;
import com.example.sanch.myapplication.VocabFragment;

 public  class NetworkChangeReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "NetworkChangeReceiver";
    private boolean isConnected = false;
    public NetworkChangeReceiver() { super();}

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(LOG_TAG, "Receieved notification about network status");
        isNetworkAvailable(context);
    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {

            Network[] networks = connectivity.getAllNetworks();
            NetworkInfo networkInfo;
            for (Network mNetwork : networks) {
                networkInfo = connectivity.getNetworkInfo(mNetwork);
                if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                        if (!isConnected) {
                            Log.v(LOG_TAG, "Now you are connected to Internet!");
                           // Toast.makeText(context, "Internet availablle via Broadcast receiver", Toast.LENGTH_SHORT).show();
                            isConnected = true;

                            // do your processing here ---
                            // if you need to post any data to the server or get
                            // status
                            // update from the server
                            Intent serviceIntent = new Intent(context,VocabFragment.class);
                            context.startService(serviceIntent);
                        }
                        return true;
                }
            }
        }
        Log.v(LOG_TAG, "You are not connected to Internet!");
     //   Toast.makeText(context, "Internet NOT availablle via Broadcast receiver", Toast.LENGTH_SHORT).show();
        isConnected = false;
        return false;
    }
 }