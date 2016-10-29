package com.example.sanch.myapplication;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import com.example.sanch.myapplication.adapters.CustomAdapter;
import com.example.sanch.myapplication.data.WordListDbContract;
import com.example.sanch.myapplication.model.Definition;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import android.widget.Toast;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class DetailActivityFragment extends Fragment  implements
        TextToSpeech.OnInitListener {

    public static final String TAG = DetailActivityFragment.class.getSimpleName();
    static final String DETAIL_VOCAB = "DETAIL_VOCAB";
    static final String SORT_BY = "SORT_BY";
    String LOG_TAG = "DetailActivityFragment";
    private static final String FAVORITES = "favorites";

    public String theWord;
    Context mContext;

    private Button mFavorite;
    private ListView mListView;
    private boolean to_delete = false;
    InterstitialAd mInterstitialAd;
    private CustomAdapter customAdapter;
    public  ArrayList<Definition> mRetrieved;
    public MediaPlayer player;
    private TextToSpeech tts;
    private ImageButton mAudioButton;
    public String audioUrl;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        rootView.setBackgroundColor(Color.WHITE);
        mContext = rootView.getContext();

        tts = new TextToSpeech(getContext(), DetailActivityFragment.this);

        mInterstitialAd = new InterstitialAd(rootView.getContext());
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_full_screen));

        mAudioButton = (ImageButton) rootView.findViewById(R.id.audioButton);
        mFavorite = (Button) rootView.findViewById(R.id.favorite_button);
        mListView = (ListView) rootView.findViewById(R.id.defList);
        mRetrieved = new ArrayList<>();
        customAdapter = new CustomAdapter(getContext(), mRetrieved);
        mListView.setAdapter(customAdapter);
       // setRetainInstance(true);
        // customAdapter.notifyDataSetChanged();

        final Bundle arguments = getArguments();

        if (arguments != null) {
            theWord = arguments.getString(DetailActivityFragment.DETAIL_VOCAB);
        }

        if (theWord == null) {
            return null;
        }
       // if (savedInstanceState == null) {

            if (theWord != null) {
                DataRetriever dataRetriever = new DataRetriever();
                String queryURL = NetworkConn(theWord, mContext);
                dataRetriever.execute(queryURL);
                mListView.setAdapter(customAdapter);
            }
        //}

            mAudioButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (theWord != null)
                    //  no audio file , use text to speech
                   if(audioUrl == null ) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            ttsGreater21(theWord);
                        } else {
                            ttsUnder20(theWord);
                        }
                   }
                   else{
                        playAudio();
                    }

                }
            });


           //On displaying the favorite movies , disable the button in the detail view
           if ((arguments.getString(DetailActivityFragment.SORT_BY)).contentEquals(FAVORITES)) {
               mFavorite.setVisibility(View.INVISIBLE);
               mFavorite.setText(R.string.delete_favorite);
           } else {
            // mFavorite.setVisibility(View.VISIBLE);
           }

           rootView.findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_SUBJECT, theWord);
                    intent.putExtra(Intent.EXTRA_TEXT, mRetrieved.get(0).getDefinitions());
                    startActivity(intent);
                }
           });

           Button wikipedia = (Button) rootView.findViewById(R.id.wikipedia_it);
           wikipedia.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri
                            .parse("http://en.wikipedia.org/wiki/" + theWord)));
                }
           });

            Button google = (Button) rootView.findViewById(R.id.google_it);
            google.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri
                            .parse("https://www.google.com/#q=definition+of+"
                                    + theWord + "&safe=off")));
                }
            });

            mFavorite.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                 /*   if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    } */

                    if( mFavorite.getText() == getString(R.string.delete_favorite)) {
                        to_delete = true;
                        int result = AsynDbUtility(theWord);

                        if (result > 0) {
                            Toast toastMsg = Toast.makeText(getActivity(), getString(R.string.delete_the_word), Toast.LENGTH_SHORT);
                            toastMsg.show();
                            to_delete = false;
                            mFavorite.setText(R.string.favorite);
                        }
                    } else {
                            if (mFavorite.getText() == getString(R.string.favorite)) {
                                cacheFavorite();
                            }
                            // enable delete button
                            if (to_delete) {
                                mFavorite.setText(R.string.delete_favorite);
                            }
                    }
                }
            });

            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    Log.i(LOG_TAG, "onAdClosed: ad closed...");
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    super.onAdFailedToLoad(errorCode);
                    Log.i(LOG_TAG, "onAdFailedToLoad: ad Failed to load. Reloading...");
                    //prefetch the next ad
                        requestNewInterstitial();
                }

                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    Log.i(LOG_TAG, "onAdLoaded: interstitial is ready!");
                    mInterstitialAd.show();
                }
            });

            //fetch new ad
     // for (int i = 0 ; i < max_ads ; i++) {
    //      requestNewInterstitial();
    //  }
        return rootView;
    }

    void requestNewInterstitial() {
        // Request a new ad if one isn't already loaded, hide the button, and kick off the timer.
        if (!mInterstitialAd.isLoading() && !mInterstitialAd.isLoaded()) {
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .build();
            mInterstitialAd.loadAd(adRequest);
        }
    }

    @SuppressWarnings("deprecation")
    private void ttsUnder20(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsGreater21(String text) {
        String utteranceId = this.hashCode() + "";
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // Accent!
            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            }
        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }

    /* The system calls this function when creating the fragment.
     *  We initialize essential components of the fragment that we want to
     * retain when the fragment is paused or stopped, then resumed. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public  void onStart() {
            requestNewInterstitial();
        super.onStart();
    }

    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if(player!=null) {
           player.release();
        }
    }

    @Override public void onStop() {
        tts.shutdown(); super.onStop();
    }

    /**
     * Caches the favorite movies in the database
     */
    private void cacheFavorite() {
        final Uri wordUri = WordListDbContract.WordEntry.CONTENT_URI;
        //Retrieving data from SQLite databases in Android is done using Cursors.
        final Cursor cursor = getActivity().getContentResolver().query(wordUri, null, null, null, null);
        if (cursor != null) {

            int result = AsynDbUtility(theWord);
            // nothing in database, add it
            //data to be inserted in the table
            if (result == 0) {
                final ContentValues wordDef = new ContentValues();

                wordDef.put(WordListDbContract.WordEntry.COLUMN_WORD, theWord);

                getActivity().getContentResolver()
                        .insert(WordListDbContract.WordEntry.CONTENT_URI, wordDef);
                Toast toastMsg = Toast.makeText(getActivity(), getString(R.string.added_to_favorites), Toast.LENGTH_SHORT);
                toastMsg.show();
                to_delete = true;
            }
            cursor.close();
        }
    }

    // check if word is in favorites
    public Integer AsynDbUtility(final String theWord) {
        int numRows = 0;
        Cursor cursor = getActivity().getContentResolver().query(
                WordListDbContract.WordEntry.CONTENT_URI,
                null,   // projection
                WordListDbContract.WordEntry.COLUMN_WORD + " = ?", // selection
                new String[]{theWord},   // selectionArgs
                null    // sort order
        );
        numRows = cursor.getCount();
        cursor.close();

        // if it is in favorites db, no insertions
        if (numRows == 1 && to_delete) {  //added 10/23
            getActivity().getContentResolver().delete(
                    WordListDbContract.WordEntry.CONTENT_URI,
                    WordListDbContract.WordEntry.COLUMN_WORD + " = ?",
                    new String[]{(theWord)}
            );
        }
        if(numRows > 0 && !to_delete) { //added 10/23
                    Toast toastMsg = Toast.makeText(getActivity(), getString(R.string.already_in_favorites), Toast.LENGTH_SHORT);
                    toastMsg.show();
        }
        return numRows;
    }

    public void playAudio() {
        try {
                player = new MediaPlayer();
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                player.setDataSource(audioUrl);
                player.prepare();
                player.start();
                player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer player) {
                        player.start();
                    }
                });
                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer player) {
                        if (player != null) {
                            player.reset();
                            player.release();
                        }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    public String NetworkConn(String token, Context context) {
        mRetrieved = new ArrayList<>();
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        String queryURL = null;
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data
            token = token.replaceAll(" ", "%20");
            queryURL = "http://www.dictionaryapi.com/api/v1/references/collegiate/xml/" + token + "?key=" + context.getString(R.string.dictionary_api_key);
        } else {
            // display error
            Definition errDef = new Definition("", getString(R.string.err_network), "", "", "", "");
            mRetrieved.clear();
            mRetrieved.add(errDef);
        }
        return queryURL;
    }

    public class DataRetriever extends AsyncTask<String, Void, ArrayList<Definition>> {
        int flag = 0;

        // try to fetch xml from Merriam-Webster
        @Override
        protected ArrayList<Definition> doInBackground(String... urls) {
            try {
                    URL url = new URL(urls[0].toString());
                    Log.i("url", urls[0].toString());
                    URLConnection conn = url.openConnection();
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document doc = builder.parse(conn.getInputStream());
                    return parse(doc, mContext);

            } catch (Exception e) {
                flag = 1;
                ArrayList<Definition> errDefs = new ArrayList<>();
                Definition errDef = new Definition("", mContext.getResources().getString(R.string.err_retrieving), "", "", "", "");
                errDefs.add(errDef);
                return errDefs;
            }
        }

        // send newly formed Definition object to listView
        @Override
        protected void onPostExecute(ArrayList<Definition> retrievedX) {
            mRetrieved.clear();
            if (retrievedX != null) {
                mRetrieved = retrievedX;
                customAdapter.setDefinitionData(mRetrieved);
                customAdapter.notifyDataSetChanged();
                if (flag == 0) {
                    mFavorite.setVisibility(View.VISIBLE);
                }

                if(mRetrieved.get(0).hasAudio()) {
                    audioUrl = new String(mRetrieved.get(0).getAudioUrl());
                }
            }
        }
    }

    public static ArrayList<Definition> parse(Document doc, Context context) {
        doc.getDocumentElement().normalize();
        ArrayList<Definition> definitions = new ArrayList<Definition>();

        // get suggestions if the word is not found and print them out
        NodeList suggestionList = doc.getElementsByTagName("suggestion");
        if (suggestionList.getLength() > 0) {
            String suggestText = context.getResources().getString(R.string.err_suggestion);;
            String suggestions = "";
            for (int i = 0; i < suggestionList.getLength(); i++) {
                Node suggestion = suggestionList.item(i);
                if (suggestion.getNodeType() == Node.ELEMENT_NODE) {
                    suggestions = suggestions + suggestion.getTextContent() + "\n";
                }
            }
            Definition sugDef = new Definition("", suggestText, "", "", suggestions, "");
            definitions.add(sugDef);
        }
        // get every entry for the word
        NodeList entryList = doc.getElementsByTagName("entry");

        // for each entry
        for (int i = 0; i < entryList.getLength(); i++) {
            Node node = entryList.item(i);
            String num = Integer.toString(i + 1);
            String word = "";
            String fl = "";
            String pr = "";
            String audioUrl = "";
            ArrayList<String> defs = new ArrayList<String>();
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;
                word = eElement.getAttribute("id");
                word = word.split("\\[")[0];

                NodeList defList = node.getChildNodes();

                //examine entry
                for (int j = 0; j < defList.getLength(); j++) {
                    Node child = defList.item(j);
                    if (child.getNodeType() == Node.ELEMENT_NODE) {

                        // get audio
                        if (child.getNodeName().equals("sound")) {
                            String baseUrl = "http://media.merriam-webster.com/soundc11/";
                            String wavName = child.getFirstChild().getTextContent();
                            String firstLetter = wavName.substring(0, 1) + "/";
                            audioUrl = baseUrl + firstLetter + wavName;
                        }

                        // get function label (part of speech)
                        if (child.getNodeName().equals("fl")) {
                           fl = child.getTextContent();
                        }
                        // get pronunciation
                        if (child.getNodeName().equals("pr")) {
                            pr = child.getTextContent();
                        }

                        // get each definition
                        if (child.getNodeName().equals("def")) {
                            NodeList dtList = child.getChildNodes();
                            for (int k = 0; k < dtList.getLength(); k++) {
                                Node dt = dtList.item(k);
                                if (dt.getNodeType() == Node.ELEMENT_NODE) {
                                    if (dt.getNodeName().equals("dt")) {
                                        if (!dt.getTextContent().equals("")) {
                                            defs.add(dt.getTextContent());
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }
            // assemble definition object
            String defList = "";
            if (!defs.isEmpty()) {
                for (int a = 0; a < defs.size(); a++) {
                    defList = defList + defs.get(a) + "\n";
                }
            }

            Definition def = new Definition(num, word, fl, pr, defList, audioUrl);
            definitions.add(def);
        }
        return definitions;
    }
}



