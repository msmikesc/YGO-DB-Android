package com.example.ygodb.util;

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
import java.util.concurrent.Executors;

public class CopyDBInCallback implements ActivityResultCallback<ActivityResult> {
	private final MainActivity activity;

	public CopyDBInCallback(MainActivity activity) {
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
		AndroidUtil.showProgressDialog(activity);
		Executors.newSingleThreadExecutor().execute(() -> importDBFromURI(finalChosenURI));
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

	public void importDBFromURI(Uri chosenURI) {
		DrawerLayout view = activity.getBinding().getRoot();
		try {
			String responseMessage = AndroidUtil.getDBInstance().copyDataBaseFromURI(activity, chosenURI);

			AndroidUtil.updateViewsAfterDBLoad();

			view.post(() -> {
				AndroidUtil.hideProgressDialog();
				Snackbar.make(view, responseMessage, BaseTransientBottomBar.LENGTH_LONG).show();
			});
		} catch (Exception e) {
			YGOLogger.logException(e);
			view.post(() -> {
				AndroidUtil.hideProgressDialog();
				Snackbar.make(view, "Error: Exception " + e.getMessage(), BaseTransientBottomBar.LENGTH_LONG).show();
			});
		}
	}
}
