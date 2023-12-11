package com.example.ygodb.model.partialscroll;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.ygodb.model.popupfiltermenu.FilterState;
import com.example.ygodb.ui.singlecard.SingleCardToListAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.utility.YGOLogger;

import java.util.List;
import java.util.concurrent.Executors;

public class PartialScrollFilterButtonOnClickListener<T extends OwnedCard> implements View.OnClickListener {

	private final ViewCardsLoadPartialScrollViewModel<T> viewModel;
	private final SingleCardToListAdapter adapter;
	private final LinearLayoutManager layout;
	private final FloatingActionButton fab;
	private final Context context;

	protected Handler handler = new Handler(Looper.getMainLooper());

	public PartialScrollFilterButtonOnClickListener(FloatingActionButton fab, Context context, ViewCardsLoadPartialScrollViewModel<T> viewModel,
			SingleCardToListAdapter adapter, LinearLayoutManager layout) {
		this.viewModel = viewModel;
		this.adapter = adapter;
		this.layout = layout;
		this.fab = fab;
		this.context = context;
	}

	@Override
	public void onClick(View view) {
		// Initializing the popup menu and giving the reference as current context
		PopupMenu popupMenu = new PopupMenu(context, fab);

		FilterState filterState = viewModel.getFilterState();

		//inflate menu dynamically from state
		for(int i = 0; i < viewModel.getRarityFiltersList().size(); i++){
			String current = viewModel.getRarityFiltersList().get(i);
			popupMenu.getMenu().add(0, i, i, current);
		}

		if(filterState.getCurrentSelectionID() != null){
			MenuItem menuItemCurrent = popupMenu.getMenu().getItem(filterState.getCurrentSelectionID());
			menuItemCurrent.setTitle(filterState.getCurrentSelectionText());
		}

		popupMenu.setOnMenuItemClickListener(menuItem -> {
			filterState.clickOnMenuItem(menuItem.getOrder());
			viewModel.setCurrentlySelectedRarityFilter(filterState.getCurrentSelectionFilterString());
			List<T> cardsList = viewModel.getCardsList();
			Executors.newSingleThreadExecutor().execute(() -> {
				try {
					List<T> moreCards = viewModel.loadMoreData(
							viewModel.getSortOrder(), ViewCardsLoadPartialScrollViewModel.LOADING_LIMIT, 0,
							viewModel.getCardNameSearch(), viewModel.getCurrentlySelectedRarityFilter());

					view.post(() -> {
						cardsList.clear();
						cardsList.addAll(moreCards);
						layout.scrollToPositionWithOffset(0, 0);
						adapter.notifyDataSetChanged();
					});
				} catch (Exception e) {
					YGOLogger.logException(e);
				}
			});

			return true;
		});
		// Showing the popup menu
		popupMenu.show();
	}
}
