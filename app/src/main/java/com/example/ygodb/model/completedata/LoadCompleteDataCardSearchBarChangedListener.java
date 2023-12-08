package com.example.ygodb.model.completedata;

import android.text.Editable;
import android.widget.EditText;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.ygodb.model.TextChangedListener;
import com.example.ygodb.ui.singlecard.SingleCardToListAdapter;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.utility.YGOLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class LoadCompleteDataCardSearchBarChangedListener extends TextChangedListener<EditText> {
	private final ViewCardsLoadCompleteDataViewModel viewCardsViewModel;
	private final SingleCardToListAdapter adapter;
	private final LinearLayoutManager layout;

	public LoadCompleteDataCardSearchBarChangedListener(EditText searchBar, ViewCardsLoadCompleteDataViewModel viewCardsViewModel,
			SingleCardToListAdapter adapter, LinearLayoutManager layout) {
		super(searchBar);
		this.viewCardsViewModel = viewCardsViewModel;
		this.adapter = adapter;
		this.layout = layout;
	}

	@Override
	public void onTextChanged(EditText target, Editable s) {
		String cardNameSearch = s.toString().toLowerCase(Locale.ROOT);

		if (viewCardsViewModel.getCardNameSearch().equals(cardNameSearch)) {
			//nothing to update
			return;
		}

		long startTime = System.currentTimeMillis();
		viewCardsViewModel.setCardNameSearch(cardNameSearch);

		Executors.newSingleThreadExecutor().execute(() -> {
			try {
				List<OwnedCard> results = null;
				List<OwnedCard> filteredResults = null;

				if (viewCardsViewModel.isCardNameMode()) {
					results = viewCardsViewModel.getInitialCardNameData(cardNameSearch);
					filteredResults = new ArrayList<>(results);
				} else {
					results = viewCardsViewModel.getCardsList();
					filteredResults = viewCardsViewModel.getFilteredList(viewCardsViewModel.getCardsList(), cardNameSearch);

					viewCardsViewModel.sortData(filteredResults, viewCardsViewModel.getSortOption());
				}

				List<OwnedCard> finalResults = results;
				List<OwnedCard> finalFilteredResults = filteredResults;

				long lastSearchStartTime = viewCardsViewModel.getCurrentSearchStartTime();
				if(startTime >= lastSearchStartTime){
					viewCardsViewModel.setCurrentSearchStartTime(startTime);
					handler.post(() -> {
						viewCardsViewModel.setCardsList(finalResults);
						viewCardsViewModel.setFilteredCardsList(finalFilteredResults);
						adapter.setCardsList(finalFilteredResults);

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
