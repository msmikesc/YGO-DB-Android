package com.example.ygodb.ui.viewsetboxes;

import android.text.Editable;
import android.widget.EditText;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ygodb.abs.TextChangedListener;
import ygodb.commonlibrary.bean.SetBox;
import ygodb.commonlibrary.utility.YGOLogger;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

class BoxLookupSearchBarChangedListener extends TextChangedListener<EditText> {
	private final ViewBoxSetViewModel viewBoxSetViewModel;
	private final SingleBoxToListAdapter adapter;
	private final LinearLayoutManager layout;

	public BoxLookupSearchBarChangedListener(EditText searchBar, ViewBoxSetViewModel viewBoxSetViewModel, SingleBoxToListAdapter adapter,
			LinearLayoutManager layout) {
		super(searchBar);
		this.viewBoxSetViewModel = viewBoxSetViewModel;
		this.adapter = adapter;
		this.layout = layout;
	}

	@Override
	public void onTextChanged(EditText target, Editable s) {
		String textSearch = s.toString().toLowerCase(Locale.ROOT);

		if (textSearch.trim().length() < 3) {
			textSearch = "";
		}

		if (viewBoxSetViewModel.getCurrentSearchText().equals(textSearch)) {
			//nothing to update
			return;
		}

		viewBoxSetViewModel.setCurrentSearchText(textSearch);

		String finalTextSearch = textSearch;
		Executors.newSingleThreadExecutor().execute(() -> {
			try {
				List<SetBox> results = viewBoxSetViewModel.getSearchData(finalTextSearch);

				handler.post(() -> {
					viewBoxSetViewModel.setBoxList(results);
					adapter.setSetBoxes(results);

					layout.scrollToPositionWithOffset(0, 0);
					adapter.notifyDataSetChanged();
				});
			} catch (Exception e) {
				YGOLogger.logException(e);
			}
		});
	}
}
