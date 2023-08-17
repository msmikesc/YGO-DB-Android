package com.example.ygodb.ui.viewsetboxes;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import com.example.ygodb.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

class BoxLookupFABOnClickListener implements View.OnClickListener {

	private final ViewBoxSetViewModel viewBoxSetViewModel;
	private final SingleBoxToListAdapter adapter;
	private final FloatingActionButton fab;
	private final Context context;
	private final EditText searchEditText;

	public BoxLookupFABOnClickListener(FloatingActionButton fab, Context context, ViewBoxSetViewModel viewBoxSetViewModel,
			SingleBoxToListAdapter adapter, EditText searchEditText) {
		this.viewBoxSetViewModel = viewBoxSetViewModel;
		this.adapter = adapter;
		this.searchEditText = searchEditText;
		this.fab = fab;
		this.context = context;
	}

	@Override
	public void onClick(View view) {
		// Initializing the popup menu and giving the reference as current context
		PopupMenu popupMenu = new PopupMenu(context, fab);

		// Inflating popup menu from popup_menu.xml file
		popupMenu.getMenuInflater().inflate(R.menu.set_boxes_menu, popupMenu.getMenu());
		popupMenu.setOnMenuItemClickListener(menuItem -> {
			String menuOption = (String) menuItem.getTitle();

			if (menuOption.equalsIgnoreCase("Add New From Search Box")) {
				boolean results = viewBoxSetViewModel.attemptToAddNewFromSetPrefix(searchEditText.getText().toString());

				if (results) {
					adapter.notifyDataSetChanged();
				} else {
					Snackbar.make(view, "Unable to add set box for that set code", BaseTransientBottomBar.LENGTH_LONG).show();
				}
			}

			return true;
		});
		// Showing the popup menu
		popupMenu.show();
	}
}
