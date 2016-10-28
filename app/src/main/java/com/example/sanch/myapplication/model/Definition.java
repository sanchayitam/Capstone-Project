package com.example.sanch.myapplication.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.sanch.myapplication.data.WordListDbContract;

public class Definition implements Parcelable {
    private String entryNumber;
    private String word;
    private String partOfSpeech;
    private String pronunciation;
    private String definitions;
    private String audioUrl;

    public Definition(String entryNumber, String word, String partOfSpeech, String pronunciation, String definitions, String audioUrl) {
        this.entryNumber = entryNumber;
        this.word = word;
        this.partOfSpeech = partOfSpeech;
        this.pronunciation = pronunciation;
        this.definitions = definitions;
        this.audioUrl = audioUrl;
    }

    public Definition(Cursor cursor) {
        // // The Android SQLite query method returns a Cursor object containing the results of the query.
        this.word = cursor.getString(cursor.getColumnIndex(WordListDbContract.WordEntry.COLUMN_WORD));
    }

    public String getEntryNumber() {
        return entryNumber;
    }

    public String getWord() {
        return word;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public String getPronunciation() {
        return pronunciation;
    }

    public String getDefinitions() {
        return definitions;
    }

    public String getAudioUrl()  {
        return audioUrl;
    }


    public boolean hasAudio() {
        if(audioUrl.equals(""))
            return false;
        else
            return true;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(entryNumber);
        dest.writeString(word);
        dest.writeString(partOfSpeech);
        dest.writeString(pronunciation);
        dest.writeString(definitions);
        dest.writeString(audioUrl);
    }

    public static final Creator<Definition> CREATOR
            = new Creator<Definition>() {
        public Definition createFromParcel(Parcel in) {
            return new Definition(in);
        }

        public Definition[] newArray(int size) {
            return new Definition[size];
        }
    };

    private Definition(Parcel in) {
        entryNumber = in.readString();
        word = in.readString();
        partOfSpeech = in.readString();
        pronunciation = in.readString();
        definitions = in.readString();
        audioUrl = in.readString();
    }
}
