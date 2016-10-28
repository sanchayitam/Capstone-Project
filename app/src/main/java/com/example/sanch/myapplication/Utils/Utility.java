package com.example.sanch.myapplication.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import com.example.sanch.myapplication.R;
import com.example.sanch.myapplication.model.Definition;

public class Utility {

  public static ArrayList<Definition> mRetrieved;
  public  MediaPlayer player;
  public  String audioUrl;
  public  Context context;

    /**
     * Returns true if the network is available or about to become available.
     *
     * @param c Context used to get the ConnectivityManager
     * @return
     */
   public static boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public  void playAudio(){
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
                        //  player = null;
                    }
                    }
                });
        } catch (Exception e){
            e.printStackTrace();
            return;
        }
    }

public static String NetworkConn(String token, Context context) {
        mRetrieved = new ArrayList<>();
        boolean isConnected = isNetworkAvailable(context);

         String queryURL = null;
         if(isConnected) {
        // fetch data
            queryURL = "http://www.dictionaryapi.com/api/v1/references/collegiate/xml/" + token + "?key=" + context.getString(R.string.dictionary_api_key);
        }
        else {
        // display error
            Definition errDef = new Definition("", context.getResources().getString(R.string.err_network), "", "", "", "");
            mRetrieved.clear();
            mRetrieved.add(errDef);
        }
        return  queryURL;
}

public class DataRetriever extends AsyncTask<String, Void, ArrayList<Definition>> {
    // try to fetch xml from Merriam-Webster
    @Override
    protected ArrayList<Definition> doInBackground(String... urls) {
        try {
                URL url = new URL(urls[0].toString());
                URLConnection conn = url.openConnection();
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(conn.getInputStream());
                return parse(doc);

        } catch (Exception e) {
                ArrayList<Definition> errDefs = new ArrayList<>();
                Definition errDef = new Definition("","Error retrieving definition" , "", "", "", "");
                errDefs.add(errDef);
                return errDefs;
        }
    }

    // send newly formed Definition object to listView
    @Override
    protected void onPostExecute(ArrayList<Definition> retrievedX) {
        mRetrieved = retrievedX;
        mRetrieved.addAll(retrievedX);
        if(mRetrieved.get(0).hasAudio()) {
            audioUrl = new String(mRetrieved.get(0).getAudioUrl());
            playAudio();
        }

    }
}

    public static ArrayList<Definition> parse(Document doc) {
        doc.getDocumentElement().normalize();
        ArrayList<Definition> definitions = new ArrayList<Definition>();

        // get suggestions if the word is not found and print them out
        NodeList suggestionList = doc.getElementsByTagName("suggestion");
        if (suggestionList.getLength() > 0) {
            String suggestText = "\nNo matches found. Did you mean:";
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
            String imgUrl = "";
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