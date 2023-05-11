package com.example.ygodb.abs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;

import com.example.ygodb.MainActivity;
import com.example.ygodb.ui.viewCardSet.ViewCardSetViewModel;
import com.example.ygodb.ui.viewCards.ViewCardsViewModel;
import com.example.ygodb.ui.viewCardsSummary.ViewCardsSummaryViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class CopyDBInCallback implements ActivityResultCallback<ActivityResult> {
    private final MainActivity activity;

    public CopyDBInCallback(MainActivity activity) {
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

        InputStream file = null;

        try {
            file = activity.getContentResolver().openInputStream(chosenURI);
        } catch (FileNotFoundException e) {
            Snackbar.make(view, "Error: Exception " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            return;
        }

        if (file == null) {
            Snackbar.make(view, "Error: InputStream Null", Snackbar.LENGTH_LONG).show();
            return;
        }


        InputStream finalFile = file;
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    AndroidUtil.getDBInstance().copyDataBaseFromURI(finalFile);

                    SharedPreferences prefs = activity.getPreferences(Context.MODE_PRIVATE);

                    SharedPreferences.Editor editor = prefs.edit();

                    editor.putString("pref_db_location", chosenURI.toString());

                    ViewCardSetViewModel viewCardSetViewModel =
                            new ViewModelProvider(AndroidUtil.getViewModelOwner()).get(ViewCardSetViewModel.class);

                    ArrayList<String> setNamesArrayList = AndroidUtil.getDBInstance().getDistinctSetAndArchetypeNames();
                    viewCardSetViewModel.setNamesDropdownList = new String[setNamesArrayList.size()];
                    setNamesArrayList.toArray(viewCardSetViewModel.setNamesDropdownList);

                    ViewCardsViewModel viewCardsViewModel =
                            new ViewModelProvider(AndroidUtil.getViewModelOwner()).get(ViewCardsViewModel.class);
                    viewCardsViewModel.refreshViewDBUpdate();

                    viewCardSetViewModel.refreshViewDBUpdate();

                    ViewCardsSummaryViewModel viewCardsSummaryViewModel =
                            new ViewModelProvider(AndroidUtil.getViewModelOwner()).get(ViewCardsSummaryViewModel.class);
                    viewCardsSummaryViewModel.refreshViewDBUpdate();

                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(view, "DB Imported", Snackbar.LENGTH_LONG).show();
                        }
                    });
                } catch (IOException e) {
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(view, "Error: Exception " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });


    }
}
