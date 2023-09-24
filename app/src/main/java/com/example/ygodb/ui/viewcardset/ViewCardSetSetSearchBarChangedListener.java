package com.example.ygodb.ui.viewcardset;

import android.text.Editable;
import android.widget.EditText;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.ygodb.abs.TextChangedListener;
import com.example.ygodb.ui.singlecard.SingleCardToListAdapter;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.utility.YGOLogger;

import java.util.List;
import java.util.concurrent.Executors;

public class ViewCardSetSetSearchBarChangedListener extends TextChangedListener<EditText> {
	private final ViewCardSetViewModel viewCardsViewModel;
	private final SingleCardToListAdapter adapter;
	private final LinearLayoutManager layout;

	public ViewCardSetSetSearchBarChangedListener(EditText searchBar, ViewCardSetViewModel viewCardsViewModel,
			SingleCardToListAdapter adapter, LinearLayoutManager layout) {
		super(searchBar);
		this.viewCardsViewModel = viewCardsViewModel;
		this.adapter = adapter;
		this.layout = layout;
	}

	@Override
	public void onTextChanged(EditText target, Editable s) {
		String setNameSearch = s.toString();

		if (viewCardsViewModel.getSetNameSearch().equals(setNameSearch)) {
			//nothing to update
			return;
		}

		viewCardsViewModel.setSetNameSearch(setNameSearch);
		long startTime = System.currentTimeMillis();

		Executors.newSingleThreadExecutor().execute(() -> {
			try {
				List<OwnedCard> results = null;
				List<OwnedCard> filteredResults = null;

				results = viewCardsViewModel.getInitialData(setNameSearch);
				filteredResults = viewCardsViewModel.getFilteredList(results, viewCardsViewModel.getCardNameSearch());

				viewCardsViewModel.sortData(filteredResults, viewCardsViewModel.getCurrentComparator());

				List<OwnedCard> finalResults = results;
				List<OwnedCard> finalFilteredResults = filteredResults;

				long lastSearchStartTime = viewCardsViewModel.getCurrentSearchStartTime();
				if(startTime >= lastSearchStartTime) {
					viewCardsViewModel.setCurrentSearchStartTime(startTime);
					handler.post(() -> {
						viewCardsViewModel.setCardsList(finalResults);
						viewCardsViewModel.setFilteredCardsList(finalFilteredResults);
						adapter.setOwnedCards(finalFilteredResults);

						layout.scrollToPositionWithOffset(0, 0);
						adapter.notifyDataSetChanged();
					});
				}
			} catch (Exception e) {
				YGOLogger.logException(e);
			}
		});
	}
}
