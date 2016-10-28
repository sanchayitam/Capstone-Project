package com.example.sanch.myapplication;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sanch.myapplication.Utils.RecyclerViewItemClickListener;
import com.example.sanch.myapplication.Utils.Utility;
import com.example.sanch.myapplication.adapters.WordAdapter;
import com.example.sanch.myapplication.adapters.WordCursorAdapter;
import com.example.sanch.myapplication.data.WordListDbContract;
import com.example.sanch.myapplication.model.WordItem;
import com.example.sanch.myapplication.service.NetworkChangeReceiver;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class VocabFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private WordAdapter wordAdapter;
    private ArrayList<String> mWords;
    String LOG_TAG = "VocabFragment";

    private NetworkChangeReceiver receiver = null;
    private RecyclerView mRecyclerView;
    ImageButton audioButton;
    ImageButton searchButton;
    private boolean mDualPane;
    private int mCurPosition = 0;
    AdView mAdView;
    EditText searchBar;
    TextView textView;
    static boolean isConnected = false;
    private TextView mTxtView;
    private CoordinatorLayout coordinatorLayout;
    static Context mContext;
    public static final String TAG = VocabFragment.class.getSimpleName();

    private static final int CURSOR_LOADER_ID = 0;
    private Cursor mCursor;
    private WordCursorAdapter CursorAdapter;

    static final String WORD_KEYS = "words";
    private static final String FAVORITES = "favorites";
    static final String SORT_KEY = "sorting key";
    private final String CUR_INDEX = "cur_index";
    private final String NONE = "none";
    private String mSort_key = NONE;

    private static final String[] WORDS_COLUMNS = {
            WordListDbContract.WordEntry.COLUMN_WORD,
    };

    public VocabFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Check to see if we have a frame in which to embed the details
        // fragment directly in the containing UI.
        View detailsFrame = getActivity().findViewById(R.id.vocab_detail_container);
        mDualPane = detailsFrame != null;
        if (savedInstanceState != null) {
            // Restore last state for checked position.
            mCurPosition = savedInstanceState.getInt(CUR_INDEX, 0);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = new Bundle();
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        coordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id
                .coordinatorLayout);
        NetworkChangeReceiver mReceiver;

        mTxtView = (TextView) rootView.findViewById(R.id.text_list_empty);
        mTxtView.setVisibility(View.GONE);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setVisibility(View.VISIBLE);

        wordAdapter = new WordAdapter(getActivity(), new ArrayList<String>());
        wordAdapter.setHasStableIds(true);
        mRecyclerView.setAdapter(wordAdapter);
        wordAdapter.notifyDataSetChanged();

        searchBar = (EditText) rootView.findViewById(R.id.searchBar);
        searchBar.getText().clear();

        textView = (TextView) rootView.findViewById(R.id.TextView_word_flash_card);

        audioButton = (ImageButton) rootView.findViewById(R.id.audioButton);
        audioButton.setVisibility(View.GONE);

        searchButton = (ImageButton) rootView.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                search(v);
            }
        });

        int columnCount = getResources().getInteger(R.integer.list_column_count); // no of columns

        mRecyclerView.setHasFixedSize(true);
        GridLayoutManager glm = new GridLayoutManager(getActivity(), columnCount, GridLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(glm);
        isConnected = Utility.isNetworkAvailable(rootView.getContext());
        //setRetainInstance(true);

        if (!isConnected) {
            updateEmptyView();
            networkSnackbar();
            Log.i("No connection", "try again");
        } else {
            if (savedInstanceState != null) {
                if (savedInstanceState.containsKey(SORT_KEY)) {
                    mSort_key = savedInstanceState.getString(SORT_KEY);
                }
                if (savedInstanceState.containsKey(WORD_KEYS)) {
                    mWords = (ArrayList<String>) savedInstanceState.get(WORD_KEYS);

                    if (mWords != null) {
                        wordAdapter.notifyDataSetChanged();
                        wordAdapter.setWordData(mWords);
                    }
                } else {
                    generateWordList(mSort_key);
                }
            } else {
                generateWordList(mSort_key);
            }
        }

        // Setup Card Clicks
        mRecyclerView.addOnItemTouchListener(
                new RecyclerViewItemClickListener(mContext, new RecyclerViewItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if (isConnected) {
                            if (mCursor != null && mCursor.getCount() != 0) {
                                mCursor.moveToPosition(position);

                                String theWord = mCursor.getString(mCursor.getColumnIndex(WordListDbContract.WordEntry.COLUMN_WORD));
                                ((Callback) getActivity()).onItemSelected(theWord, position, mSort_key);

                            } else {
                                if (wordAdapter.getItemCount() > 0)
                                    ((Callback) getActivity()).onItemSelected(wordAdapter.getItem(position), position, mSort_key);
                            }
                        } else {
                            updateEmptyView();
                            networkSnackbar();
                        }
                    }
                })
        );

        // Create a banner ad. The ad size and ad unit ID must be set before calling loadAd.

        mAdView = (AdView) rootView.findViewById(R.id.adView);
        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
        if (mSort_key == FAVORITES) {
            getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
        }
        updateEmptyView();
    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        // Destroy the AdView.
        if (mAdView != null) {
            mAdView.destroy();
        }
        if(receiver == null) {
            getActivity().unregisterReceiver(receiver);
        }
        super.onStop();
    }
    @Override
    public void onDestroy() {
        // Destroy the AdView.
        if (mAdView != null) {
            mAdView.destroy();
        }
        getActivity().unregisterReceiver(receiver);
        super.onDestroy();
    }

    public void generateWordList(String sort_key) {
        final String LOG_TAG = VocabFragment.class.getSimpleName();

        if (!isConnected) {
            updateEmptyView();
            networkSnackbar();
        } else {
            wordAdapter = new WordAdapter(getActivity(), new ArrayList<String>());
            wordAdapter.setHasStableIds(true);
            mRecyclerView.setAdapter(wordAdapter);
            wordAdapter.notifyDataSetChanged();

            if (!sort_key.contentEquals(FAVORITES)) {
                mTxtView.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                FetchWordsDB WordTask = new FetchWordsDB();
                WordTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    public void search(View view) {
        audioButton.setVisibility(View.GONE); // set audio button to invisible in case new word has no audio
         if (!isConnected) {
            updateEmptyView();
            networkSnackbar();
        } else {
            String theWord = new StringBuilder(searchBar.getText()).toString();
            int position = 0;
            ((Callback) getActivity()).onItemSelected(theWord, position, mSort_key);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (!mSort_key.contentEquals(NONE)) {
            savedInstanceState.putString(SORT_KEY, mSort_key);
        }
        if (mWords != null) {
            savedInstanceState.putStringArrayList(WORD_KEYS, mWords);
        }
        savedInstanceState.putInt(CUR_INDEX, mCurPosition);
    }

    public interface Callback {
        void onItemSelected(String word, int index, String sort_by);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // super.onRestoreInstanceState(savedInstanceState);
        mSort_key = savedInstanceState.getString(SORT_KEY);
        mWords = (ArrayList<String>) savedInstanceState.get(WORD_KEYS);
        mCurPosition = savedInstanceState.getInt(CUR_INDEX);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkChangeReceiver() {
            public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {

                Bundle extras = intent.getExtras();
                NetworkInfo info =  extras.getParcelable("networkInfo");
                NetworkInfo.State state = info.getState();
                Log.d("TEST Internet", info.toString() + " " + state.toString());

                if (state == NetworkInfo.State.CONNECTED) {
                    isConnected = true;
                    mTxtView.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                    generateWordList(mSort_key);
                } else {
                    isConnected = false;
                    updateEmptyView();
                    networkSnackbar();
                }
            }
        }
    };
        getActivity().registerReceiver(receiver, filter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.clear();
        inflater.inflate(R.menu.menu_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_sort_by_favorite: {
                 mSort_key = FAVORITES;
                 searchBar.getText().clear();

                 getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
                 if (mDualPane) {
                    setDualPaneView();
                 }
                 return true;
            }
            case R.id.action_generate_list: {
                 mCursor = null;
                 mSort_key = NONE;
                 searchBar.getText().clear();

                 generateWordList(mSort_key);
                 return true;
            }

            case R.id.action_settings:
                 return true;

            default:
                 return super.onOptionsItemSelected(item);
        }
    }

    public class FetchWordsDB extends AsyncTask<ArrayList<String>, Void, ArrayList<String>> {
        final String LOG_TAG = FetchWordsDB.class.getSimpleName();
        ArrayList<String> wordInfo = new ArrayList<>(10);

        @Override
        protected ArrayList<String> doInBackground(ArrayList<String>... params) {
            try {
                // final String url = "http://api.wordnik.com:80/v4/words.json/randomWords?hasDictionaryDef=true&minCorpusCount=0&maxCorpusCount=-1&minDictionaryCount=1&maxDictionaryCount=-1&limit=10&api_key=a2a73e7b926c924fad7001ca3111acd55af2ffabf50eb4ae5";
                String base_url = "http://api.wordnik.com:80/v4/words.json/randomWords?hasDictionaryDef=true&limit=10";
                String api_key= getResources().getString(R.string.random_wordlist_api_key);
                final String url = new String(base_url + "&api_key=" + api_key);

                OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(url)
                            .build();
                    Response response = client.newCall(request).execute();
                    // Read the input stream into a String
                    String inputStream = response.body().string();

                    if (inputStream == null) {
                        // Nothing to do.
                        return null;
                    }

                    wordInfo = parseJsonStr(inputStream, wordInfo);
                    return (CheckDictionary(wordInfo));
            } catch (IOException e) {
                    Log.e(LOG_TAG, "Error in generateWordList", e);
                    return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<String> words) {
            super.onPostExecute(words);

            if (words != null) {
                mWords = words;

                wordAdapter.setWordData(mWords);
                wordAdapter.notifyDataSetChanged();

                if (mDualPane) {
                 ((Callback) getActivity()).onItemSelected(wordAdapter.getItem(0), 0, mSort_key);
              }
            }
        }

        ArrayList<String> parseJsonStr(String wordListJson, ArrayList<String> wordInfo) {
            try {
                    //Attributes of Json String
                    final String M_ID = "id";
                    final String M_WORD = "word";

                    //Convert Json String to Json Object
                    JSONArray JsonArray = new JSONArray(wordListJson);
                    WordItem[] wordInfo1 = new WordItem[JsonArray.length()];
                    wordInfo = new ArrayList<>();
                    for (int j = 0; j < JsonArray.length(); j++) {
                        JSONObject post = JsonArray.optJSONObject(j);
                        wordInfo1[j] = new WordItem(post.getString(M_ID), post.getString(M_WORD));
                        wordInfo.add(j, wordInfo1[j].getWord());
                    }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return wordInfo;
        }

        private ArrayList<String> CheckDictionary(ArrayList<String> wordInfo) {
            ArrayList<String> wordpower = new ArrayList<>();

            if (wordInfo != null) {
                for (int i = 0, j = 0; i < wordInfo.size(); i++) {
                    try {
                            Uri.Builder builder = new Uri.Builder();
                            builder.scheme("http").authority("www.dictionaryapi.com").appendPath("api")
                                    .appendPath("v1").appendPath("references")
                                    .appendPath("collegiate").appendPath("xml").appendPath(wordInfo.get(i))
                                    .appendQueryParameter("key", getString(R.string.dictionary_api_key));
                            URL url1 = new URL(builder.toString());
                            Log.i("URL ....", builder.toString());

                            URLConnection conn = url1.openConnection();

                            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                            DocumentBuilder builder3 = factory.newDocumentBuilder();
                            Document doc = builder3.parse(conn.getInputStream());
                            doc.getDocumentElement().normalize();

                            // get suggestions if the word is not found and print them out
                            NodeList suggestionList = doc.getElementsByTagName("suggestion");

                            // get every entry for the word
                            NodeList entryList = doc.getElementsByTagName("entry");

                            int cnt = i;
                            if (suggestionList.getLength() > 0 || entryList.getLength() <= 0) {
                                wordInfo.remove(cnt);
                                cnt--;
                            } else {
                                    wordpower.add(wordInfo.get(cnt));
                            }
                    } catch (Exception e) {
                             Log.e(LOG_TAG, "Error ", e);
                    }
                }
            }
            return wordpower;
        }
    }

    public void networkSnackbar() {
        final Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "No internet connection!", Snackbar.LENGTH_LONG)
                .setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!isConnected) {
                            networkSnackbar();
                        }
                    }
                });

        // Changing message text color
        snackbar.setActionTextColor(Color.RED);

        // Changing action button text color
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);

        if (!isConnected)
            snackbar.show();
        else
            snackbar.dismiss();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        String sortOrder = WordListDbContract.WordEntry.COLUMN_WORD + " ASC";
        String select = "WordListDbContract.WordEntry.COLUMN_WORD";
        return new CursorLoader(getActivity(),
               WordListDbContract.WordEntry.CONTENT_URI,
               WORDS_COLUMNS,
               null,
               null,
               sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!isAdded()) {
            if (data != null) {
                data.close();
            }
            return;
        }
        mCursor = data;
        CursorAdapter = new WordCursorAdapter(getActivity(), mCursor);
        CursorAdapter.setHasStableIds(true);
        mRecyclerView.swapAdapter(CursorAdapter, false);
        CursorAdapter.notifyDataSetChanged();
        updateEmptyView();
        if(mDualPane) {
            if (CursorAdapter.getItemCount() == 0){
                Toast toastMsg = Toast.makeText(getActivity(), getString(R.string.favorite_List_empty), Toast.LENGTH_SHORT);
                toastMsg.show();
                ((Callback) getActivity()).onItemSelected(null, -1, mSort_key);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursor = null;
        mRecyclerView.setAdapter(null);
    }

    private void updateEmptyView() {
        int message;
        isConnected = Utility.isNetworkAvailable(getActivity());

        if (!isConnected) {
            mRecyclerView.setVisibility(View.GONE);
            message = R.string.empty_list_no_network;
            mTxtView.setVisibility(View.VISIBLE);
            mTxtView.setText(message);
        }
        else
        {
            mRecyclerView.setVisibility(View.VISIBLE);
            mTxtView.setVisibility(View.GONE);
        }
    }

    public void setDualPaneView() {
        if (mCursor != null && mCursor.getCount() > 0) {
            mCursor.moveToFirst();
            mRecyclerView.setVisibility(View.VISIBLE);
            mTxtView.setVisibility(View.GONE);
            String theWord = mCursor.getString(mCursor.getColumnIndex(WordListDbContract.WordEntry.COLUMN_WORD));
            ((Callback) getActivity()).onItemSelected(theWord, 0, mSort_key);
        }
       else {
            int message = R.string.favorite_List_empty;
            mTxtView.setText(message);
            mRecyclerView.setVisibility(View.GONE);
            mTxtView.setVisibility(View.VISIBLE);
            if (mDualPane)
                ((Callback) getActivity()).onItemSelected(null, -1, mSort_key);
        }
    }
}


