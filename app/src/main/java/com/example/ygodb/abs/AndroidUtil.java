package com.example.ygodb.abs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import androidx.appcompat.app.AppCompatActivity;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import com.example.ygodb.MainActivity;
import com.example.ygodb.R;
import com.example.ygodb.impl.SQLiteConnectionAndroid;
import com.example.ygodb.ui.analyzesets.AnalyzeCardsViewModel;
import com.example.ygodb.ui.viewSoldCards.ViewSoldCardsViewModel;
import com.example.ygodb.ui.viewcards.ViewCardsViewModel;
import com.example.ygodb.ui.viewcardset.ViewCardSetViewModel;
import com.example.ygodb.ui.viewcardssummary.ViewCardsSummaryViewModel;
import com.example.ygodb.ui.viewsetboxes.ViewBoxSetViewModel;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.YGOLogger;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class AndroidUtil {

	private AndroidUtil() {
	}

	private static Context appContext = null;

	private static AppCompatActivity owner = null;

	public static void setAppContext(Context in) {
		appContext = in;
	}

	public static Context getAppContext() {
		return appContext;
	}

	public static void setViewModelOwner(AppCompatActivity in) {
		owner = in;
	}

	private static SQLiteConnectionAndroid dbInstance = null;

	public static SQLiteConnectionAndroid getDBInstance() {
		if (dbInstance == null) {
			dbInstance = new SQLiteConnectionAndroid();
		}

		return dbInstance;
	}

	public static AppCompatActivity getViewModelOwner() {
		return owner;
	}

	@SuppressLint("Range")
	public static String getFileName(Uri uri) {
		String result = null;
		if (uri.getScheme().equals("content")) {
			try (Cursor cursor = getAppContext().getContentResolver().query(uri, null, null, null, null)) {
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

	public static void updateViewsAfterDBLoad() {
		ViewCardSetViewModel viewCardSetViewModel = new ViewModelProvider(AndroidUtil.getViewModelOwner()).get(ViewCardSetViewModel.class);

		List<String> setNamesArrayList = null;
		try {
			setNamesArrayList = AndroidUtil.getDBInstance().getDistinctSetAndArchetypeNames();
		} catch (SQLException e) {
			YGOLogger.logException(e);
			throw new RuntimeException(e);
		}
		viewCardSetViewModel.updateSetNamesDropdownList(setNamesArrayList);
		viewCardSetViewModel.refreshViewDBUpdate();

		AnalyzeCardsViewModel analyzeCardsViewModel =
				new ViewModelProvider(AndroidUtil.getViewModelOwner()).get(com.example.ygodb.ui.analyzesets.AnalyzeCardsViewModel.class);

		analyzeCardsViewModel.updateSetNamesDropdownList(setNamesArrayList);
		analyzeCardsViewModel.refreshViewDBUpdate();

		ViewCardsViewModel viewCardsViewModel = new ViewModelProvider(AndroidUtil.getViewModelOwner()).get(ViewCardsViewModel.class);
		viewCardsViewModel.refreshViewDBUpdate();

		ViewCardsSummaryViewModel viewCardsSummaryViewModel =
				new ViewModelProvider(AndroidUtil.getViewModelOwner()).get(ViewCardsSummaryViewModel.class);
		viewCardsSummaryViewModel.refreshViewDBUpdate();

		ViewSoldCardsViewModel viewSoldCardsViewModel =
				new ViewModelProvider(AndroidUtil.getViewModelOwner()).get(ViewSoldCardsViewModel.class);
		viewSoldCardsViewModel.refreshViewDBUpdate();

		ViewBoxSetViewModel viewBoxSetViewModel = new ViewModelProvider(AndroidUtil.getViewModelOwner()).get(ViewBoxSetViewModel.class);
		viewBoxSetViewModel.refreshViewDBUpdate();
	}

	public static int getColorByColorVariant(String colorVariant) {
		if (colorVariant != null && !colorVariant.isEmpty() && !colorVariant.equals(Const.DEFAULT_COLOR_VARIANT)) {
			return switch (colorVariant.toUpperCase(Locale.ROOT)) {
				case "A" -> ContextCompat.getColor(AndroidUtil.getAppContext(), R.color.Gold);
				case "R" -> ContextCompat.getColor(AndroidUtil.getAppContext(), R.color.Crimson);
				case "G" -> ContextCompat.getColor(AndroidUtil.getAppContext(), R.color.LimeGreen);
				case "B" -> ContextCompat.getColor(AndroidUtil.getAppContext(), R.color.DeepSkyBlue);
				case "P" -> ContextCompat.getColor(AndroidUtil.getAppContext(), R.color.BlueViolet);
				case "S" -> ContextCompat.getColor(AndroidUtil.getAppContext(), R.color.AMCSilver);
				case "BRZ" -> ContextCompat.getColor(AndroidUtil.getAppContext(), R.color.Bronze);
				default -> ContextCompat.getColor(AndroidUtil.getAppContext(), R.color.White);
			};
		} else {
			return ContextCompat.getColor(AndroidUtil.getAppContext(), R.color.White);
		}
	}

	public static String getSetRarityDisplayWithColorText(OwnedCard current) {
		String setRarityText = current.getSetRarity();

		if (current.getColorVariant() != null && !current.getColorVariant().isEmpty() &&
				!current.getColorVariant().equals(Const.DEFAULT_COLOR_VARIANT)) {
			if (current.getColorVariant().equalsIgnoreCase("a")) {
				setRarityText = "Alt Art " + setRarityText;
			} else {
				setRarityText = current.getColorVariant().toUpperCase(Locale.ROOT) + " " + setRarityText;
			}
		}

		return setRarityText;
	}

	private static Dialog progressDialog = null;

	public static void showProgressDialog(Activity activity) {
		if(progressDialog == null) {
			progressDialog = new Dialog(activity, android.R.style.Theme_Translucent_NoTitleBar);
			progressDialog.setContentView(R.layout.layout_dialog_progress);
			progressDialog.setCancelable(false);
			progressDialog.show();
		}
	}

	public static void hideProgressDialog(){
		if(progressDialog != null){
			progressDialog.dismiss();
			progressDialog = null;
		}
	}


}
