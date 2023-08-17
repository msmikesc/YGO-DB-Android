package com.example.ygodb.ui.viewcardssummary;

import android.content.Context;
import android.view.View;
import android.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.ygodb.R;
import com.example.ygodb.ui.singlecard.SummaryCardToListAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.utility.YGOLogger;

import java.util.List;
import java.util.concurrent.Executors;

class ViewCardSummarySortButtonOnClickListener implements View.OnClickListener {

	private final ViewCardsSummaryViewModel viewCardsViewModel;
	private final SummaryCardToListAdapter adapter;
	private final LinearLayoutManager layout;
	private final FloatingActionButton fab;
	private final Context context;

	public ViewCardSummarySortButtonOnClickListener(FloatingActionButton fab, Context context,
			ViewCardsSummaryViewModel viewCardsViewModel,
			SummaryCardToListAdapter adapter, LinearLayoutManager layout) {
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

		// Inflating popup menu from popup_menu.xml file
		popupMenu.getMenuInflater().inflate(R.menu.sort_menu_summary, popupMenu.getMenu());
		popupMenu.setOnMenuItemClickListener(menuItem -> {

			String sortOption = viewCardsViewModel.getSortOption();

			String sortOrder = viewCardsViewModel.getSortOrder();

			if (!sortOption.contentEquals(menuItem.getTitle())) {
				sortOption = (String) menuItem.getTitle();

				switch (sortOption) {
					case "Date Bought" -> sortOrder = "maxDate desc, cardName asc";
					case "Card Name" -> sortOrder = "cardName asc, dateBought desc";
					case "Quantity" -> sortOrder = "totalQuantity desc, cardName asc";
					case "Price" -> sortOrder = "avgPrice desc, cardName asc";
				}
				viewCardsViewModel.setSortOption(sortOption);
				viewCardsViewModel.setSortOrder(sortOrder);

				List<OwnedCard> cardsList = viewCardsViewModel.getCardsList();

				String finalSortOrder = sortOrder;
				Executors.newSingleThreadExecutor().execute(() -> {
					try {
						cardsList.clear();
						List<OwnedCard> moreCards =
								viewCardsViewModel.loadMoreData(finalSortOrder, ViewCardsSummaryViewModel.LOADING_LIMIT, 0,
																viewCardsViewModel.getCardNameSearch());
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
