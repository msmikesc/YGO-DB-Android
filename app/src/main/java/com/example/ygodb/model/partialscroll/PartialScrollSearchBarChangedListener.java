package com.example.ygodb.model.partialscroll;

import android.text.Editable;
import android.widget.EditText;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ygodb.model.ItemsListAdapter;
import com.example.ygodb.model.TextChangedListener;
import ygodb.commonlibrary.utility.YGOLogger;

import java.util.List;
import java.util.concurrent.Executors;

public class PartialScrollSearchBarChangedListener<T, U extends RecyclerView.ViewHolder> extends TextChangedListener<EditText> {

	private final ViewCardsLoadPartialScrollViewModel<T> viewModel;
	private final ItemsListAdapter<T,U> adapter;
	private final LinearLayoutManager layout;

	public PartialScrollSearchBarChangedListener(EditText searchBar, ViewCardsLoadPartialScrollViewModel<T> viewModel,
			ItemsListAdapter<T,U> adapter, LinearLayoutManager layout) {
		super(searchBar);
		this.viewModel = viewModel;
		this.adapter = adapter;
		this.layout = layout;
	}

	@Override
	public void onTextChanged(EditText target, Editable s) {
		String cardNameSearch = s.toString();

		if (viewModel.getCardNameSearch().equals(cardNameSearch)) {
			//nothing to update
			return;
		}

		viewModel.setCardNameSearch(cardNameSearch);
		long startTime = System.currentTimeMillis();

		Executors.newSingleThreadExecutor().execute(() -> {
			try {
				List<T> newList =
						viewModel.loadMoreData(viewModel.getSortOrder(), ViewCardsLoadPartialScrollViewModel.LOADING_LIMIT, 0,
											   cardNameSearch, viewModel.getCurrentlySelectedRarityFilter());

				long lastSearchStartTime = viewModel.getCurrentSearchStartTime();
				if(startTime >= lastSearchStartTime) {
					viewModel.setCurrentSearchStartTime(startTime);
					handler.post(() -> {
						viewModel.setCardsList(newList);
						adapter.setItemsList(newList);
						viewModel.updateFilterStateFromRarityCollection(viewModel.getRarityListFor(viewModel.getCardNameSearch()));
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
