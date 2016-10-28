/*
This file contains ContentProvider class. An application accesses the data from a content provider
 */
package com.example.sanch.myapplication.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import com.example.sanch.myapplication.data.WordListDbContract;
import com.example.sanch.myapplication.data.WordListDbHelper;

public class WordListProvider extends ContentProvider {
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private WordListDbHelper mOpenHelper;
    static final int WORDS = 100;

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = WordListDbContract.CONTENT_AUTHORITY;
        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, WordListDbContract.PATH_WORD_LIST, WORDS);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new WordListDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor retCursor = null;
        switch (sUriMatcher.match(uri)) {
            case WORDS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                                         WordListDbContract.WordEntry.TABLE_WORDS, projection, selection, selectionArgs,
                                         null, null, sortOrder);
                break;
            }
            default:

                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
       retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case WORDS:
                return WordListDbContract.WordEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri returnUri;

        switch (sUriMatcher.match(uri)) {
            case WORDS: {
                long _id = db.insert(WordListDbContract.WordEntry.TABLE_WORDS, null, values);
                if (_id > 0) {
                    returnUri = WordListDbContract.WordEntry.buildWordUri(_id);
                }
                else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if (null == selection) selection = "1";
        switch (sUriMatcher.match(uri)) {
            case WORDS:

                rowsDeleted = db.delete(WordListDbContract.WordEntry.TABLE_WORDS, selection, selectionArgs);
                break;
            default:

                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsUpdated;

        switch (sUriMatcher.match(uri)) {
            case WORDS:

                rowsUpdated = db.update(WordListDbContract.WordEntry.TABLE_WORDS, values, selection,
                        selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }
}
