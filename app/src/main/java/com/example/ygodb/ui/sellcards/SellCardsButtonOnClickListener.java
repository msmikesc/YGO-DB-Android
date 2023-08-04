package com.example.ygodb.ui.sellcards;

import android.content.Context;
import android.view.View;
import android.widget.PopupMenu;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ygodb.R;
import com.example.ygodb.abs.AndroidUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.concurrent.Executors;

class SellCardsButtonOnClickListener implements View.OnClickListener {

	private final SellCardsViewModel sellCardsViewModel;
	private final SellCardToListAdapter adapter;
	private final LinearLayoutManager layout;
	private final FloatingActionButton fab;
	private final Context context;

	public SellCardsButtonOnClickListener(FloatingActionButton fab, Context context, SellCardsViewModel sellCardsViewModel,
			SellCardToListAdapter adapter, LinearLayoutManager layout) {
		this.sellCardsViewModel = sellCardsViewModel;
		this.adapter = adapter;
		this.layout = layout;
		this.fab = fab;
		this.context = context;
	}

	@Override
	public void onClick(View view) {

		view.setFocusableInTouchMode(true);
		view.requestFocus();
		view.setFocusableInTouchMode(false);

		// Initializing the popup menu and giving the reference as current context
		PopupMenu popupMenu = new PopupMenu(context, fab);

		// Inflating popup menu from popup_menu.xml file
		popupMenu.getMenuInflater().inflate(R.menu.sell_menu, popupMenu.getMenu());
		popupMenu.setOnMenuItemClickListener(menuItem -> {

			if (sellCardsViewModel.getCardsList().isEmpty()) {
				return true;
			}

			if (menuItem.getTitle().equals("Set Prices Zero")) {
				Executors.newSingleThreadExecutor().execute(() -> {

					sellCardsViewModel.setAllPricesZero();

					view.post(adapter::notifyDataSetChanged);

				});
			}

			if (menuItem.getTitle().equals("Set Estimated Prices")) {
				Executors.newSingleThreadExecutor().execute(() -> {

					sellCardsViewModel.setAllPricesEstimate();

					view.post(adapter::notifyDataSetChanged);

				});
			}

			if (menuItem.getTitle().equals("Set API Prices")) {
				Executors.newSingleThreadExecutor().execute(() -> {

					sellCardsViewModel.setAllPricesAPI();

					view.post(adapter::notifyDataSetChanged);

				});
			}

			if (menuItem.getTitle().equals("Save Cards")) {
				Executors.newSingleThreadExecutor().execute(() -> {

					sellCardsViewModel.saveToDB();

					view.post(adapter::notifyDataSetChanged);

					AndroidUtil.updateViewsAfterDBLoad();


				});
			}
			return true;
		});
		// Showing the popup menu
		popupMenu.show();
	}
}
