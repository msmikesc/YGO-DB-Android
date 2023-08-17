package com.example.ygodb.abs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.OpenableColumns;

import androidx.appcompat.app.AppCompatActivity;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
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
			if (colorVariant.contains(":")) {
				String[] parts = colorVariant.split(":");
				if (parts.length == 2) {
					String color = parts[0].toUpperCase(Locale.ROOT);

					return getColorForColorVariant(color);
				}
			} else {
				String color = colorVariant.toUpperCase(Locale.ROOT);
				if (color.equals("A")) {
					return ContextCompat.getColor(AndroidUtil.getAppContext(), R.color.Gold);
				} else {
					return getColorForColorVariant(color);
				}
			}
		}

		return ContextCompat.getColor(AndroidUtil.getAppContext(), R.color.White);
	}

	private static int getColorForColorVariant(String color) {
		return switch (color) {
			case "R" -> ContextCompat.getColor(AndroidUtil.getAppContext(), R.color.Crimson);
			case "G" -> ContextCompat.getColor(AndroidUtil.getAppContext(), R.color.LimeGreen);
			case "B" -> ContextCompat.getColor(AndroidUtil.getAppContext(), R.color.DeepSkyBlue);
			case "P" -> ContextCompat.getColor(AndroidUtil.getAppContext(), R.color.BlueViolet);
			case "S" -> ContextCompat.getColor(AndroidUtil.getAppContext(), R.color.AMCSilver);
			case "BRZ" -> ContextCompat.getColor(AndroidUtil.getAppContext(), R.color.Bronze);
			case "ORIGINAL" -> ContextCompat.getColor(AndroidUtil.getAppContext(), R.color.Gold);
			default -> ContextCompat.getColor(AndroidUtil.getAppContext(), R.color.White);
		};
	}

	//TODO add all cards from a static set speed duel

	public static String getSetRarityDisplayWithColorText(OwnedCard current) {
		String setRarityText = current.getSetRarity();
		String colorVariant = current.getColorVariant();

		if (colorVariant != null && !colorVariant.isEmpty() && !colorVariant.equals(Const.DEFAULT_COLOR_VARIANT)) {
			if (colorVariant.contains(":")) {
				String[] parts = colorVariant.split(":");
				if (parts.length == 2) {
					String color = parts[0];
					String altArt = parts[1];

					if (altArt.equalsIgnoreCase("a")) {
						setRarityText = "Alt Art " + setRarityText;
					}
					setRarityText = color.toUpperCase(Locale.ROOT) + " " + setRarityText;
				}
			} else {
				if (colorVariant.equalsIgnoreCase("a")) {
					setRarityText = "Alt Art " + setRarityText;
				} else {
					setRarityText = colorVariant.toUpperCase(Locale.ROOT) + " " + setRarityText;
				}
			}
		}

		return setRarityText;
	}

	private static Dialog progressDialog = null;

	public static void showProgressDialog(Activity activity) {
		if (progressDialog == null) {
			progressDialog = new Dialog(activity, android.R.style.Theme_Translucent_NoTitleBar);
			progressDialog.setContentView(R.layout.layout_dialog_progress);
			progressDialog.setCancelable(false);
			progressDialog.show();
		}
	}

	public static void hideProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}

	public static void convertToGrayscale(Drawable drawable) {
		ColorMatrix matrix = new ColorMatrix();
		matrix.setSaturation(0);

		ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);

		drawable.setColorFilter(filter);

	}

	public static Drawable applyShader(Context context, int shaderResourceId, Drawable drawable, int width, int height) {
		try {
			// Load the holofoil pattern image as a Bitmap

			Bitmap shaderBitmap = getBitmapFromVectorDrawable(context, shaderResourceId);

			// Convert the drawable to a Bitmap
			Bitmap originalBitmap = drawableToBitmap(drawable, width, height);

			// Scale the holofoil pattern to match the size of the original drawable
			shaderBitmap = Bitmap.createScaledBitmap(shaderBitmap, width, height, true);

			// Create a new Bitmap with the same size as the original drawable
			Bitmap finalBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

			// Create a Canvas to draw on the new Bitmap
			Canvas canvas = new Canvas(finalBitmap);

			// Draw the original drawable on the Canvas
			drawable.setBounds(0, 0, width, height);
			drawable.draw(canvas);

			// Set the shader effect
			Paint paint = new Paint();
			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

			paint.setAlpha(128);

			// Draw the holofoil pattern with the shader effect on the Canvas
			canvas.drawBitmap(shaderBitmap, 0, 0, paint);

			// Release the resources used by the Bitmaps
			shaderBitmap.recycle();
			originalBitmap.recycle();

			// Create a new Drawable from the final Bitmap and return it
			return new BitmapDrawable(context.getResources(), finalBitmap);
		} catch (Exception e) {
			YGOLogger.error("Unable to apply shader:");
			YGOLogger.logException(e);
			return null;
		}
	}

	private static Bitmap drawableToBitmap(Drawable drawable, int width, int height) {
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas);
		return bitmap;
	}


	public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
		Drawable drawable = ContextCompat.getDrawable(context, drawableId);

		if (drawable == null) {
			throw new IllegalArgumentException("Drawable was null");
		}

		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);

		return bitmap;
	}


}
