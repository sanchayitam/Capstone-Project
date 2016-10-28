/*
This file contains Contract class which specifies the layout of the database scheme
 */
package com.example.sanch.myapplication.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

import static android.R.attr.id;


/** A contract class is a container for constants that define names for URIs, tables, and columns.
 *  The contract class allows you to use the same constants across all the other classes in the same package.
 *  WordListDbContract class defines table and column names for the movie database.
 */
public class WordListDbContract {
    public static final String CONTENT_AUTHORITY = "com.example.sanch.myapplication";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_WORD_LIST = "word_list";

    public WordListDbContract() {
    }

    /* Inner class that defines the table contents i.e. columns of the movie table */
    public static final class WordEntry implements BaseColumns {
        public static final String TABLE_WORDS = "word_list";

        public static final String COLUMN_WORD = "word";


        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(TABLE_WORDS).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_WORDS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_WORDS;
        public static final String DEFAULT_SORT = COLUMN_WORD + " DESC";

        /** Matches: /items/ */
        public static Uri buildDirUri() {
            return CONTENT_URI;
        }


        // for building URIs on insertion of data
        public static Uri buildWordUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
         //   return BASE_CONTENT_URI.buildUpon().appendPath(TABLE_WORDS).build();

        }
        public interface Query {
            String[] PROJECTION = {
                    WordEntry._ID,
                    WordEntry.COLUMN_WORD
            };

            int _ID = 0;
            int WORD = 1;

        }

    }
}
