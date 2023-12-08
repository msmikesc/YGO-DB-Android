package com.example.ygodb.ui.viewsetboxes;

import android.text.Editable;
import android.widget.EditText;
import com.example.ygodb.model.TextChangedListener;
import ygodb.commonlibrary.bean.SetBox;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.utility.YGOLogger;

import java.util.concurrent.Executors;

class SingleBoxLabelChangedListener extends TextChangedListener<EditText> {
	private final SQLiteConnection db;

	public SingleBoxLabelChangedListener(EditText searchBar, SQLiteConnection db) {
		super(searchBar);
		this.db = db;
	}

	@Override
	public void onTextChanged(EditText target, Editable s) {
		String textSearch = s.toString();

		if (textSearch.isBlank()) {
			return;
		}

		// Retrieve the associated SetBox from the tag of the EditText
		SetBox setBox = (SetBox) target.getTag();

		if (setBox.getBoxLabel().equalsIgnoreCase(textSearch)) {
			//nothing to update
			return;
		}

		setBox.setBoxLabel(textSearch);

		Executors.newSingleThreadExecutor().execute(() -> {
			try {
				db.insertOrUpdateSetBoxByUUID(setBox);
			} catch (Exception e) {
				YGOLogger.logException(e);
			}
		});
	}
}
