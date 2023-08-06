package com.example.ygodb.ui.viewSoldCards;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ygodb.abs.EndlessScrollListener;
import com.example.ygodb.ui.singlecard.SingleCardToListAdapter;
import ygodb.commonlibrary.bean.SoldCard;
import ygodb.commonlibrary.utility.YGOLogger;

import java.util.List;
import java.util.concurrent.Executors;

class ViewSoldCardsEndlessScrollListener extends EndlessScrollListener {
	private final ViewSoldCardsViewModel viewSoldCardsViewModel;
	private final SoldCardToListAdapter adapter;

	public ViewSoldCardsEndlessScrollListener(LinearLayoutManager linearLayoutManager, ViewSoldCardsViewModel viewSoldCardsViewModel,
			SoldCardToListAdapter adapter) {
		super(linearLayoutManager);
		this.viewSoldCardsViewModel = viewSoldCardsViewModel;
		this.adapter = adapter;
	}

	@Override
	public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {

		Executors.newSingleThreadExecutor().execute(() -> {
			try {
				List<SoldCard> moreCards =
						viewSoldCardsViewModel.loadMoreData(viewSoldCardsViewModel.getSortOrder(), ViewSoldCardsViewModel.LOADING_LIMIT,
															page * ViewSoldCardsViewModel.LOADING_LIMIT,
															viewSoldCardsViewModel.getCardNameSearch());
				int curSize = adapter.getItemCount();

				List<SoldCard> cardsList = viewSoldCardsViewModel.getCardsList();

				cardsList.addAll(moreCards);

				view.post(() -> adapter.notifyItemRangeInserted(curSize, moreCards.size()));
			} catch (Exception e) {
				YGOLogger.logException(e);
			}
		});


	}
}
