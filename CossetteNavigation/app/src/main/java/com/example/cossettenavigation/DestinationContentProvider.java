package com.example.cossettenavigation;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * <h1>[Unused]</h1>
 */
public class DestinationContentProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        //db initialization here instead?
        //changed to true?
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String table = "zones";
        String[] columns = new String[] { "_ID", "name" };
        Cursor result = SearchActivity.db.query(table, columns, selection, selectionArgs, null, null, sortOrder, null);
        return result;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }


    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
