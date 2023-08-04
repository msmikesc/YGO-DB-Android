package com.example.ygodb.ui.viewcardssummary;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ygodb.abs.EndlessScrollListener;
import ygodb.commonlibrary.bean.OwnedCard;
import com.example.ygodb.ui.singlecard.SummaryCardToListAdapter;
import ygodb.commonlibrary.utility.YGOLogger;

import java.util.List;
import java.util.concurrent.Executors;

class ViewCardSummaryEndlessScrollListener extends EndlessScrollListener {
	private final ViewCardsSummaryViewModel viewCardsViewModel;
	private final SummaryCardToListAdapter adapter;

	public ViewCardSummaryEndlessScrollListener(LinearLayoutManager linearLayoutManager, ViewCardsSummaryViewModel viewCardsViewModel,
			SummaryCardToListAdapter adapter) {
		super(linearLayoutManager);
		this.viewCardsViewModel = viewCardsViewModel;
		this.adapter = adapter;
	}

	@Override
	public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {

		Executors.newSingleThreadExecutor().execute(() -> {
			try {
				List<OwnedCard> moreCards = viewCardsViewModel.loadMoreData(viewCardsViewModel.getSortOrder(),
																			ViewCardsSummaryViewModel.LOADING_LIMIT,
																			page * ViewCardsSummaryViewModel.LOADING_LIMIT,
																			viewCardsViewModel.getCardNameSearch());
				int curSize = adapter.getItemCount();

				List<OwnedCard> cardsList = viewCardsViewModel.getCardsList();

				cardsList.addAll(moreCards);

				view.post(() -> adapter.notifyItemRangeInserted(curSize, moreCards.size()));
			} catch (Exception e) {
				YGOLogger.logException(e);
			}
		});


	}
}
