package com.example.ygodb.abs;

import android.content.Intent;
import android.net.Uri;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.ygodb.MainActivity;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import ygodb.commonlibrary.utility.YGOLogger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Executors;

public class CopyDBOutCallback implements ActivityResultCallback<ActivityResult> {
	private final MainActivity activity;

	public CopyDBOutCallback(MainActivity activity) {
		this.activity = activity;
	}

	@Override
	public void onActivityResult(ActivityResult result) {

		DrawerLayout view = activity.getBinding().getRoot();
		Uri chosenURI = null;

		try {
			chosenURI = getFileURIFromActivityResult(result);
		} catch (Exception e) {
			YGOLogger.logException(e);

			String message = e.getMessage();
			if (message == null) {
				message = "Unknown error";
			}

			Snackbar.make(view, message, BaseTransientBottomBar.LENGTH_LONG).show();
			return;
		}

		Uri finalChosenURI = chosenURI;

		Executors.newSingleThreadExecutor().execute(() -> exportDBFileToURI(finalChosenURI));

	}

	public Uri getFileURIFromActivityResult(ActivityResult result) throws IOException {

		if (result == null) {
			throw new IOException("Error: Result Null");
		}

		Intent contentChosen = result.getData();

		if (contentChosen == null) {
			throw new IOException("Error: Intent Null");
		}

		Uri chosenURI = contentChosen.getData();

		if (chosenURI == null) {
			throw new IOException("Error: URI Null");
		}

		String fileName = AndroidUtil.getFileName(chosenURI);

		if (fileName == null) {
			throw new IOException("Error: Filename Null");
		}

		if (!fileName.equals("YGO-DB.db")) {
			throw new IOException("Error: Filename wrong");
		}

		return chosenURI;
	}

	public void exportDBFileToURI(Uri chosenURI) {

		DrawerLayout view = activity.getBinding().getRoot();
		OutputStream file = null;

		try {
			file = activity.getContentResolver().openOutputStream(chosenURI);
			AndroidUtil.getDBInstance().copyDataBaseToURI(activity, file);

			Snackbar.make(view, "DB Exported", BaseTransientBottomBar.LENGTH_LONG).show();

		} catch (Exception e) {
			YGOLogger.logException(e);
			Snackbar.make(view, "Error: Exception " + e.getMessage(), BaseTransientBottomBar.LENGTH_LONG).show();
		} finally {
			if (file != null) {
				try {
					file.close();
				} catch (IOException e) {
					YGOLogger.logException(e);
				}
			}
		}
	}
}
