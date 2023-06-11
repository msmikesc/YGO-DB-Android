package com.example.ygodb.abs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;

import com.example.ygodb.MainActivity;
import com.example.ygodb.ui.viewcardset.ViewCardSetViewModel;
import com.example.ygodb.ui.viewcards.ViewCardsViewModel;
import com.example.ygodb.ui.viewcardssummary.ViewCardsSummaryViewModel;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.Executors;

public class CopyDBInCallback implements ActivityResultCallback<ActivityResult> {
    private final MainActivity activity;

    public CopyDBInCallback(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onActivityResult(ActivityResult result) {

        DrawerLayout view = activity.getBinding().getRoot();

        if (result == null) {
            Snackbar.make(view, "Error: Result Null", BaseTransientBottomBar.LENGTH_LONG).show();
            return;
        }

        Intent contentChosen = result.getData();

        if (contentChosen == null) {
            Snackbar.make(view, "Error: Intent Null", BaseTransientBottomBar.LENGTH_LONG).show();
            return;
        }

        Uri chosenURI = contentChosen.getData();

        if (chosenURI == null) {
            Snackbar.make(view, "Error: URI Null", BaseTransientBottomBar.LENGTH_LONG).show();
            return;
        }

        String fileName = AndroidUtil.getFileName(chosenURI);

        if (fileName == null) {
            Snackbar.make(view, "Error: Filename Null", BaseTransientBottomBar.LENGTH_LONG).show();
            return;
        }

        if (!fileName.equals("YGO-DB.db")) {
            Snackbar.make(view, "Error: Filename wrong", BaseTransientBottomBar.LENGTH_LONG).show();
            return;
        }

        InputStream fileInputStream = null;

        try {
            fileInputStream = activity.getContentResolver().openInputStream(chosenURI);
        } catch (FileNotFoundException e) {
            Snackbar.make(view, "Error: Exception " + e.getMessage(), BaseTransientBottomBar.LENGTH_LONG).show();
            return;
        }

        if (fileInputStream == null) {
            Snackbar.make(view, "Error: InputStream Null", BaseTransientBottomBar.LENGTH_LONG).show();
            return;
        }


        InputStream finalFile = fileInputStream;
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                AndroidUtil.getDBInstance().copyDataBaseFromURI(finalFile);

                finalFile.close();

                SharedPreferences prefs = activity.getPreferences(Context.MODE_PRIVATE);

                SharedPreferences.Editor editor = prefs.edit();

                editor.putString("pref_db_location", chosenURI.toString());

                editor.apply();

                ViewCardSetViewModel viewCardSetViewModel =
                        new ViewModelProvider(AndroidUtil.getViewModelOwner()).get(ViewCardSetViewModel.class);

                ArrayList<String> setNamesArrayList = AndroidUtil.getDBInstance().getDistinctSetAndArchetypeNames();
                viewCardSetViewModel.updateSetNamesDropdownList(setNamesArrayList);

                viewCardSetViewModel.refreshViewDBUpdate();

                ViewCardsViewModel viewCardsViewModel =
                        new ViewModelProvider(AndroidUtil.getViewModelOwner()).get(ViewCardsViewModel.class);
                viewCardsViewModel.refreshViewDBUpdate();

                ViewCardsSummaryViewModel viewCardsSummaryViewModel =
                        new ViewModelProvider(AndroidUtil.getViewModelOwner()).get(ViewCardsSummaryViewModel.class);
                viewCardsSummaryViewModel.refreshViewDBUpdate();

                view.post(() -> Snackbar.make(view, "DB Imported", BaseTransientBottomBar.LENGTH_LONG).show());
            } catch (IOException e) {
                view.post(() -> Snackbar.make(view, "Error: Exception " + e.getMessage(), BaseTransientBottomBar.LENGTH_LONG).show());
            }
        });


    }
}
