package com.example.ygodb.model.completedata;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.ygodb.R;
import com.example.ygodb.model.popupfiltermenu.FilterItemBean;
import com.example.ygodb.model.popupfiltermenu.FilterState;
import com.example.ygodb.model.popupsortmenu.MenuStateComparator;
import com.example.ygodb.ui.singlecard.SingleCardToListAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.utility.YGOLogger;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;

public class LoadCompleteDataFilterButtonOnClickListener implements View.OnClickListener {

	private final ViewCardsLoadCompleteDataViewModel viewCardsViewModel;
	private final SingleCardToListAdapter adapter;
	private final LinearLayoutManager layout;
	private final FloatingActionButton fab;
	private final Context context;

	protected Handler handler = new Handler(Looper.getMainLooper());

	public LoadCompleteDataFilterButtonOnClickListener(FloatingActionButton fab, Context context, ViewCardsLoadCompleteDataViewModel viewCardsViewModel,
			SingleCardToListAdapter adapter, LinearLayoutManager layout) {
		this.viewCardsViewModel = viewCardsViewModel;
		this.adapter = adapter;
		this.layout = layout;
		this.fab = fab;
		this.context = context;
	}

	@Override
	public void onClick(View view) {
		// Initializing the popup menu and giving the reference as current context
		PopupMenu popupMenu = new PopupMenu(context, fab);

		FilterState filterState = viewCardsViewModel.getFilterState();

		//inflate menu dynamically from state
		for(int i = 0; i < viewCardsViewModel.getRarityFiltersList().size(); i++){
			String current = viewCardsViewModel.getRarityFiltersList().get(i);
			popupMenu.getMenu().add(0, i, i, current);
		}

		if(filterState.getCurrentSelectionID() != null){
			MenuItem menuItemCurrent = popupMenu.getMenu().getItem(filterState.getCurrentSelectionID());
			menuItemCurrent.setTitle(filterState.getCurrentSelectionText());
		}

		popupMenu.setOnMenuItemClickListener(menuItem -> {
			filterState.clickOnMenuItem(menuItem.getOrder());
			viewCardsViewModel.setCurrentlySelectedRarityFilter(filterState.getCurrentSelectionFilterString());
			Comparator<OwnedCard> currentComparator = viewCardsViewModel.getSortOption();
			List<OwnedCard> fullCardsList = viewCardsViewModel.getCardsList();

			Executors.newSingleThreadExecutor().execute(() -> {
				try {
					List<OwnedCard> filteredCardsList =
							viewCardsViewModel.getFilteredList(fullCardsList, viewCardsViewModel.getCardNameSearch());

					viewCardsViewModel.sortData(filteredCardsList, currentComparator);

					handler.post(() -> {
						viewCardsViewModel.setFilteredCardsList(filteredCardsList);
						adapter.setItemsList(filteredCardsList);

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
