package com.example.sanch.myapplication.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.sanch.myapplication.data.WordListDbContract;

public class WordItem implements Parcelable {
    private String id;
    private String word;

    public WordItem(String id , String word){
        this.id = id;
        this.word = word;
    }

    public WordItem(Cursor cursor) {
        // // The Android SQLite query method returns a Cursor object containing the results of the query.
        this.word = cursor.getString(cursor.getColumnIndex(WordListDbContract.WordEntry.COLUMN_WORD));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public WordItem(){
        super();
    }

    public String getId(){
        return id;
    }

    public String getWord() {
        return word;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(word);
    }

    public static final Creator<WordItem> CREATOR
            = new Creator<WordItem>() {
        public WordItem createFromParcel(Parcel in) {
            return new WordItem(in);
        }

        public WordItem[] newArray(int size) {
            return new WordItem[size];
        }
    };

    private WordItem(Parcel in) {
        id = in.readString();
        word = in.readString();
    }
}
