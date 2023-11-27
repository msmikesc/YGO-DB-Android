package com.example.ygodb.ui.viewcardset;

import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.ygodb.R;
import com.example.ygodb.abs.MenuStateComparator;
import com.example.ygodb.ui.singlecard.SingleCardToListAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.utility.YGOLogger;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;

public class ViewCardSetSortButtonOnClickListener implements View.OnClickListener {

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
		MenuStateComparator menuState = viewCardsViewModel.getMenuState();
		MenuItem menuItemCurrent = popupMenu.getMenu().getItem(menuState.getCurrentSelectionID());
		menuItemCurrent.setTitle(menuState.getCurrentSelectionText());

		popupMenu.setOnMenuItemClickListener(menuItem -> {
			menuState.clickOnMenuItem(menuItem.getOrder());
			Comparator<OwnedCard> currentComparator = viewCardsViewModel.getSortOption();
			List<OwnedCard> filteredCardsList = viewCardsViewModel.getFilteredCardsList();

			Executors.newSingleThreadExecutor().execute(() -> {
				try {
					viewCardsViewModel.sortData(filteredCardsList, currentComparator);

					view.post(() -> {
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
