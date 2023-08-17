package com.example.ygodb.ui.viewSoldCards;

import android.content.Context;
import android.view.View;
import android.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.ygodb.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import ygodb.commonlibrary.bean.SoldCard;
import ygodb.commonlibrary.utility.YGOLogger;

import java.util.List;
import java.util.concurrent.Executors;

class ViewSoldCardsSortButtonOnClickListener implements View.OnClickListener {

	private final ViewSoldCardsViewModel viewSoldCardsViewModel;
	private final SoldCardToListAdapter adapter;
	private final LinearLayoutManager layout;
	private final FloatingActionButton fab;
	private final Context context;

	public ViewSoldCardsSortButtonOnClickListener(FloatingActionButton fab, Context context, ViewSoldCardsViewModel viewSoldCardsViewModel,
			SoldCardToListAdapter adapter, LinearLayoutManager layout) {
		this.viewSoldCardsViewModel = viewSoldCardsViewModel;
		this.adapter = adapter;
		this.layout = layout;
		this.fab = fab;
		this.context = context;
	}

	@Override
	public void onClick(View view) {
		// Initializing the popup menu and giving the reference as current context
		PopupMenu popupMenu = new PopupMenu(context, fab);

		// Inflating popup menu from popup_menu.xml file
		popupMenu.getMenuInflater().inflate(R.menu.sort_menu_sold, popupMenu.getMenu());
		popupMenu.setOnMenuItemClickListener(menuItem -> {

			String sortOption = viewSoldCardsViewModel.getSortOption();

			String sortOrder = viewSoldCardsViewModel.getSortOrder();

			if (!sortOption.contentEquals(menuItem.getTitle())) {
				sortOption = (String) menuItem.getTitle();

				switch (sortOption) {
					case "Date Sold" -> sortOrder = "dateSold desc, modificationDate desc";
					case "Card Name" -> sortOrder = "cardName asc, dateSold desc";
					case "Set Number" -> sortOrder = "setName asc, setNumber asc";
					case "Price Sold" -> sortOrder = "priceSold desc, cardName asc";
				}
				viewSoldCardsViewModel.setSortOption(sortOption);
				viewSoldCardsViewModel.setSortOrder(sortOrder);

				List<SoldCard> cardsList = viewSoldCardsViewModel.getCardsList();

				String finalSortOrder = sortOrder;
				Executors.newSingleThreadExecutor().execute(() -> {
					try {
						cardsList.clear();
						List<SoldCard> moreCards =
								viewSoldCardsViewModel.loadMoreData(finalSortOrder, ViewSoldCardsViewModel.LOADING_LIMIT, 0,
																	viewSoldCardsViewModel.getCardNameSearch());
						cardsList.addAll(moreCards);

						view.post(() -> {
							layout.scrollToPositionWithOffset(0, 0);
							adapter.notifyDataSetChanged();
						});
					} catch (Exception e) {
						YGOLogger.logException(e);
					}
				});
			}
			return true;
		});
		// Showing the popup menu
		popupMenu.show();
	}
}
