package com.example.ygodb.model.partialscroll;

import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ygodb.model.ItemsListAdapter;
import com.example.ygodb.model.popupmenu.MenuState;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import ygodb.commonlibrary.utility.YGOLogger;

import java.util.List;
import java.util.concurrent.Executors;

public class PartialScrollSortButtonOnClickListener<T, U extends RecyclerView.ViewHolder> implements View.OnClickListener {

	private final ViewCardsLoadPartialScrollViewModel<T> viewModel;
	private final ItemsListAdapter<T, U> adapter;
	private final LinearLayoutManager layout;
	private final FloatingActionButton fab;
	private final Context context;
	private final int menuIdentifier;

	public PartialScrollSortButtonOnClickListener(FloatingActionButton fab, Context context, ViewCardsLoadPartialScrollViewModel<T> viewModel,
			ItemsListAdapter<T, U> adapter, LinearLayoutManager layout, int menuIdentifier) {
		this.viewModel = viewModel;
		this.adapter = adapter;
		this.layout = layout;
		this.fab = fab;
		this.context = context;
		this.menuIdentifier = menuIdentifier;
	}

	@Override
	public void onClick(View view) {
		// Initializing the popup menu and giving the reference as current context
		PopupMenu popupMenu = new PopupMenu(context, fab);

		// Inflating popup menu from popup_menu.xml file
		popupMenu.getMenuInflater().inflate(menuIdentifier, popupMenu.getMenu());

		MenuState menuState = viewModel.getMenuState();
		MenuItem menuItemCurrent = popupMenu.getMenu().getItem(menuState.getCurrentSelectionID());
		menuItemCurrent.setTitle(menuState.getCurrentSelectionText());

		popupMenu.setOnMenuItemClickListener(menuItem -> {
			menuState.clickOnMenuItem(menuItem.getOrder());
			List<T> cardsList = viewModel.getCardsList();
			String finalSortOrder = menuState.getCurrentSelectionSql();
			Executors.newSingleThreadExecutor().execute(() -> {
				try {
					cardsList.clear();
					List<T> moreCards = viewModel.loadMoreData(finalSortOrder, ViewCardsLoadPartialScrollViewModel.LOADING_LIMIT, 0,
																	   viewModel.getCardNameSearch());
					cardsList.addAll(moreCards);

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
