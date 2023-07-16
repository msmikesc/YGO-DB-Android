package com.example.ygodb.abs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.os.Process;

import androidx.appcompat.app.AppCompatActivity;

import androidx.lifecycle.ViewModelProvider;
import com.example.ygodb.impl.SQLiteConnectionAndroid;
import com.example.ygodb.ui.viewSoldCards.ViewSoldCardsViewModel;
import com.example.ygodb.ui.viewcards.ViewCardsViewModel;
import com.example.ygodb.ui.viewcardset.ViewCardSetViewModel;
import com.example.ygodb.ui.viewcardssummary.ViewCardsSummaryViewModel;
import com.example.ygodb.ui.viewsetboxes.ViewBoxSetViewModel;

import java.util.ArrayList;

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

    public static void updateViewsAfterDBLoad(){
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

        ViewSoldCardsViewModel viewSoldCardsViewModel =
                new ViewModelProvider(AndroidUtil.getViewModelOwner()).get(ViewSoldCardsViewModel.class);
        viewSoldCardsViewModel.refreshViewDBUpdate();

        ViewBoxSetViewModel viewBoxSetViewModel =
                new ViewModelProvider(AndroidUtil.getViewModelOwner()).get(ViewBoxSetViewModel.class);
        viewBoxSetViewModel.refreshViewDBUpdate();
    }

    public static boolean checkForPermissionsToURI(Activity activity, Uri uri){
        if(activity == null || uri == null){
            return false;
        }

        // Check read access permission
        int readResult = activity.checkUriPermission(uri, Process.myPid(), Process.myUid(), Intent.FLAG_GRANT_READ_URI_PERMISSION);
        boolean hasReadPermission = (readResult == PackageManager.PERMISSION_GRANTED);

        // Check write access permission
        int writeResult = activity.checkUriPermission(uri, Process.myPid(), Process.myUid(), Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        boolean hasWritePermission = (writeResult == PackageManager.PERMISSION_GRANTED);

        return hasReadPermission && hasWritePermission;

    }


}
