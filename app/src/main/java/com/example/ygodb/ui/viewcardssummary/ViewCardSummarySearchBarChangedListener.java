package com.example.ygodb.ui.viewcardssummary;

import android.text.Editable;
import android.widget.EditText;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.ygodb.abs.TextChangedListener;
import com.example.ygodb.ui.singlecard.SummaryCardToListAdapter;
import com.example.ygodb.ui.viewcards.ViewCardsViewModel;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.utility.YGOLogger;

import java.util.List;
import java.util.concurrent.Executors;

class ViewCardSummarySearchBarChangedListener extends TextChangedListener<EditText> {
	private final ViewCardsSummaryViewModel viewCardsViewModel;
	private final SummaryCardToListAdapter adapter;
	private final LinearLayoutManager layout;

	public ViewCardSummarySearchBarChangedListener(EditText searchBar, ViewCardsSummaryViewModel viewCardsViewModel,
			SummaryCardToListAdapter adapter, LinearLayoutManager layout) {
		super(searchBar);
		this.viewCardsViewModel = viewCardsViewModel;
		this.adapter = adapter;
		this.layout = layout;
	}

	@Override
	public void onTextChanged(EditText target, Editable s) {
		String cardNameSearch = s.toString();

		if (viewCardsViewModel.getCardNameSearch().equals(cardNameSearch)) {
			//nothing to update
			return;
		}

		viewCardsViewModel.setCardNameSearch(cardNameSearch);

		Executors.newSingleThreadExecutor().execute(() -> {
			try {
				List<OwnedCard> newList =
						viewCardsViewModel.loadMoreData(viewCardsViewModel.getSortOrder(), ViewCardsViewModel.LOADING_LIMIT, 0,
														cardNameSearch);

				handler.post(() -> {
					viewCardsViewModel.setCardsList(newList);
					adapter.setOwnedCards(newList);
					layout.scrollToPositionWithOffset(0, 0);
					adapter.notifyDataSetChanged();
				});
			} catch (Exception e) {
				YGOLogger.logException(e);
			}
		});
	}
}
