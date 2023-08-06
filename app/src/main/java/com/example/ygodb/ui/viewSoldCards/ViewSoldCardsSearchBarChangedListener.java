package com.example.ygodb.ui.viewSoldCards;

import android.text.Editable;
import android.widget.EditText;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.ygodb.abs.TextChangedListener;
import com.example.ygodb.ui.singlecard.SingleCardToListAdapter;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.bean.SoldCard;
import ygodb.commonlibrary.utility.YGOLogger;

import java.util.List;
import java.util.concurrent.Executors;

class ViewSoldCardsSearchBarChangedListener extends TextChangedListener<EditText> {
	private final ViewSoldCardsViewModel viewSoldCardsViewModel;
	private final SoldCardToListAdapter adapter;
	private final LinearLayoutManager layout;

	public ViewSoldCardsSearchBarChangedListener(EditText searchBar, ViewSoldCardsViewModel viewSoldCardsViewModel,
			SoldCardToListAdapter adapter, LinearLayoutManager layout) {
		super(searchBar);
		this.viewSoldCardsViewModel = viewSoldCardsViewModel;
		this.adapter = adapter;
		this.layout = layout;
	}

	@Override
	public void onTextChanged(EditText target, Editable s) {
		String cardNameSearch = s.toString();

		if (viewSoldCardsViewModel.getCardNameSearch().equals(cardNameSearch)) {
			//nothing to update
			return;
		}

		viewSoldCardsViewModel.setCardNameSearch(cardNameSearch);

		Executors.newSingleThreadExecutor().execute(() -> {
			try {
				List<SoldCard> newList =
						viewSoldCardsViewModel.loadMoreData(viewSoldCardsViewModel.getSortOrder(), ViewSoldCardsViewModel.LOADING_LIMIT, 0,
															cardNameSearch);

				handler.post(() -> {
					viewSoldCardsViewModel.setCardsList(newList);
					adapter.setSoldCards(newList);
					layout.scrollToPositionWithOffset(0, 0);
					adapter.notifyDataSetChanged();
				});
			} catch (Exception e) {
				YGOLogger.logException(e);
			}
		});
	}
}
