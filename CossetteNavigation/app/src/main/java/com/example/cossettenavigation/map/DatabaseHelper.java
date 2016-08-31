package com.example.cossettenavigation.map;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * <h1>[Unused]</h1>
 */
public class DatabaseHelper extends SQLiteAssetHelper {

    private static final String DB_NAME = "map.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
}
