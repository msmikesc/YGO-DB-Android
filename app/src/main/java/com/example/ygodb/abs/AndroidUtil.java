package com.example.ygodb.abs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ygodb.impl.SQLiteConnectionAndroid;

public class AndroidUtil {

    private AndroidUtil(){}

    private static Context appContext = null;

    private static AppCompatActivity owner = null;

    public static void setAppContext(Context in){
        appContext = in;
    }

    public static Context getAppContext(){
        return appContext;
    }

    public static void setViewModelOwner(AppCompatActivity in) {
        owner = in;
    }

    private static SQLiteConnectionAndroid dbInstance = null;

    public static SQLiteConnectionAndroid getDBInstance(){
        if (dbInstance == null){
            dbInstance = new SQLiteConnectionAndroid();
        }

        return dbInstance;
    }

    public static AppCompatActivity getViewModelOwner(){
        return owner;
    }

    @SuppressLint("Range")
    public static String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getAppContext().getContentResolver()
                    .query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }


}
