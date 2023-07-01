package com.example.ygodb.abs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.ygodb.MainActivity;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import ygodb.commonlibrary.utility.YGOLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
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

		Executors.newSingleThreadExecutor().execute(() -> {
			try {

				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

				File db = AndroidUtil.getDBInstance().getDatabaseFileReference();

				YGOLogger.error("File modified time:" + dateFormat.format(new Date(db.lastModified())));
				YGOLogger.error("File hashcode:" + getFileHash(db));

				InputStream fileInputStream = activity.getContentResolver().openInputStream(chosenURI);

				AndroidUtil.getDBInstance().copyDataBaseFromURI(fileInputStream);

				fileInputStream.close();

				db = AndroidUtil.getDBInstance().getDatabaseFileReference();

				YGOLogger.error("File modified time:" + dateFormat.format(new Date(db.lastModified())));
				YGOLogger.error("File hashcode:" + getFileHash(db));

				SharedPreferences prefs = activity.getPreferences(Context.MODE_PRIVATE);

				SharedPreferences.Editor editor = prefs.edit();

				editor.putString("pref_db_location", chosenURI.toString());

				editor.apply();

				AndroidUtil.updateViewsAfterDBLoad();

				view.post(() -> Snackbar.make(view, "DB Imported", BaseTransientBottomBar.LENGTH_LONG).show());
			} catch (Exception e) {
				YGOLogger.logException(e);
				view.post(() -> Snackbar.make(view, "Error: Exception " + e.getMessage(), BaseTransientBottomBar.LENGTH_LONG).show());
			}
		});
	}

	public static String getFileHash(File file) throws NoSuchAlgorithmException, IOException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] buffer = new byte[4096];
		try (FileInputStream fis = new FileInputStream(file);
			 DigestInputStream dis = new DigestInputStream(fis, md)) {
			while (dis.read(buffer) != -1) {
				// Reading the file content to compute the hash
			}
			byte[] hashBytes = md.digest();
			StringBuilder sb = new StringBuilder();
			for (byte hashByte : hashBytes) {
				sb.append(Integer.toString((hashByte & 0xff) + 0x100, 16).substring(1));
			}
			return sb.toString();
		}
	}
}
