package com.example.ygodb.ui.viewcardset;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.ygodb.R;
import com.example.ygodb.abs.OwnedCardNameComparator;
import com.example.ygodb.abs.OwnedCardPriceComparator;
import com.example.ygodb.abs.OwnedCardQuantityComparator;
import com.example.ygodb.abs.OwnedCardSetNumberComparator;
import com.example.ygodb.ui.addcards.AddCardsFragment;
import com.example.ygodb.ui.singlecard.SingleCardToListAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.utility.YGOLogger;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;

class ViewCardSetSortButtonOnClickListener implements View.OnClickListener {

	private final ViewCardSetViewModel viewCardsViewModel;
	private final SingleCardToListAdapter adapter;
	private final LinearLayoutManager layout;
	private final FloatingActionButton fab;
	private final Context context;

	public ViewCardSetSortButtonOnClickListener(FloatingActionButton fab, Context context, ViewCardSetViewModel viewCardsViewModel,
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

		// Inflating popup menu from popup_menu.xml file
		popupMenu.getMenuInflater().inflate(R.menu.sort_menu_set_list, popupMenu.getMenu());
		popupMenu.setOnMenuItemClickListener(menuItem -> {

			if ("Add Mode".contentEquals(menuItem.getTitle())) {

				Intent intent = new Intent(context, AddCardsFragment.class);
				context.startActivity(intent);
				return true;
			}

			String sortOption = viewCardsViewModel.getSortOption();

			Comparator<OwnedCard> currentComparator = viewCardsViewModel.getCurrentComparator();

			if (!sortOption.contentEquals(menuItem.getTitle())) {
				sortOption = (String) menuItem.getTitle();

				switch (sortOption) {
					case "Quantity" -> currentComparator = new OwnedCardQuantityComparator();
					case "Card Name" -> currentComparator = new OwnedCardNameComparator();
					case "Set Number" -> currentComparator = new OwnedCardSetNumberComparator();
					case "Price" -> currentComparator = new OwnedCardPriceComparator();
				}
				viewCardsViewModel.setSortOption(sortOption);
				viewCardsViewModel.setCurrentComparator(currentComparator);

				List<OwnedCard> filteredCardsList = viewCardsViewModel.getFilteredCardsList();

				Comparator<OwnedCard> finalCurrentComparator = currentComparator;
				Executors.newSingleThreadExecutor().execute(() -> {
					try {
						viewCardsViewModel.sortData(filteredCardsList, finalCurrentComparator);

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
