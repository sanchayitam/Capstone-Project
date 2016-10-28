/*
This file contains SQL Helper class which contains methods to create and maintain database and tables.
 */
package com.example.sanch.myapplication.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WordListDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
        private static final int DATABASE_VERSION = 1;
        static final String DATABASE_NAME = "word_list.db";

        public WordListDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

    @Override
        public void onCreate(SQLiteDatabase db) {
            final String SQL_CREATE_WORD_TABLE = "CREATE TABLE " + WordListDbContract.WordEntry.TABLE_WORDS + " (" +
                    WordListDbContract.WordEntry._ID + " \" INTEGER PRIMARY KEY AUTOINCREMENT,\" , " +
                    WordListDbContract.WordEntry.COLUMN_WORD + " TEXT " + " );";

        db.execSQL(SQL_CREATE_WORD_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + WordListDbContract.WordEntry.TABLE_WORDS);
            onCreate(db);
        }
}
