package com.example.ygodb.abs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.ygodb.MainActivity;
import com.google.android.material.snackbar.Snackbar;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

public class CopyDBOutCallback implements ActivityResultCallback<ActivityResult> {
    private final MainActivity activity;

    public CopyDBOutCallback(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onActivityResult(ActivityResult result) {

        DrawerLayout view = activity.getBinding().getRoot();

        if (result == null) {
            Snackbar.make(view, "Error: Result Null", Snackbar.LENGTH_LONG).show();
            return;
        }

        Intent contentChosen = result.getData();

        if (contentChosen == null) {
            Snackbar.make(view, "Error: Intent Null", Snackbar.LENGTH_LONG).show();
            return;
        }

        Uri chosenURI = contentChosen.getData();

        if (chosenURI == null) {
            Snackbar.make(view, "Error: URI Null", Snackbar.LENGTH_LONG).show();
            return;
        }

        String fileName = AndroidUtil.getFileName(chosenURI);

        if (fileName == null) {
            Snackbar.make(view, "Error: Filename Null", Snackbar.LENGTH_LONG).show();
            return;
        }

        if (!fileName.equals("YGO-DB.db")) {
            Snackbar.make(view, "Error: Filename wrong", Snackbar.LENGTH_LONG).show();
            return;
        }

        OutputStream file = null;

        try {
            file = activity.getContentResolver().openOutputStream(chosenURI);
        } catch (FileNotFoundException e) {
            Snackbar.make(view, "Error: Exception " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            return;
        }

        if (file == null) {
            Snackbar.make(view, "Error: OutputStream Null", Snackbar.LENGTH_LONG).show();
            return;
        }

        try {
            AndroidUtil.getDBInstance().copyDataBaseToURI(file);

            SharedPreferences prefs = activity.getPreferences(Context.MODE_PRIVATE);

            SharedPreferences.Editor editor = prefs.edit();

            editor.putString("pref_db_location", chosenURI.toString());
            Snackbar.make(view, "DB Exported", Snackbar.LENGTH_LONG).show();

        } catch (IOException e) {
            Snackbar.make(view, "Error: Exception " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            return;
        }
    }
}
