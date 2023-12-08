package com.example.ygodb.model.partialscroll;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ygodb.model.EndlessScrollListener;
import ygodb.commonlibrary.utility.YGOLogger;

import java.util.List;
import java.util.concurrent.Executors;

public class PartialScrollEndlessScrollListener<T, U extends RecyclerView.ViewHolder> extends EndlessScrollListener {
	private final ViewCardsLoadPartialScrollViewModel<T> viewModel;
	private final RecyclerView.Adapter<U> adapter;

	public PartialScrollEndlessScrollListener(LinearLayoutManager linearLayoutManager, ViewCardsLoadPartialScrollViewModel<T> viewCardsViewModel,
			RecyclerView.Adapter<U> adapter) {
		super(linearLayoutManager);
		this.viewModel = viewCardsViewModel;
		this.adapter = adapter;
	}

	@Override
	public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {

		Executors.newSingleThreadExecutor().execute(() -> {
			try {
				List<T> moreCards =
						viewModel.loadMoreData(viewModel.getSortOrder(), ViewCardsLoadPartialScrollViewModel.LOADING_LIMIT,
											   page * ViewCardsLoadPartialScrollViewModel.LOADING_LIMIT, viewModel.getCardNameSearch());
				int curSize = adapter.getItemCount();

				List<T> cardsList = viewModel.getCardsList();

				cardsList.addAll(moreCards);

				view.post(() -> adapter.notifyItemRangeInserted(curSize, moreCards.size()));
			} catch (Exception e) {
				YGOLogger.logException(e);
			}
		});


	}
}
