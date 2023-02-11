package com.example.ygodb.ui.viewCardSet;

import android.content.Context;
import android.os.AsyncTask;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ygodb.R;
import com.example.ygodb.abs.OwnedCardNameComparator;
import com.example.ygodb.abs.OwnedCardPriceComparator;
import com.example.ygodb.abs.OwnedCardQuantityComparator;
import com.example.ygodb.abs.OwnedCardSetNumberComparator;
import com.example.ygodb.backend.bean.OwnedCard;
import com.example.ygodb.ui.singleCard.SingleCardToListAdapter;
import com.example.ygodb.ui.viewCards.ViewCardsViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Comparator;

class ViewCardSetSortButtonOnClickListener implements View.OnClickListener {

    private final ViewCardSetViewModel viewCardsViewModel;
    private final SingleCardToListAdapter adapter;
    private LinearLayoutManager layout;
    private FloatingActionButton fab;
    private Context context;

    public ViewCardSetSortButtonOnClickListener(FloatingActionButton fab, Context context,
                                                ViewCardSetViewModel viewCardsViewModel,
                                                SingleCardToListAdapter adapter,
                                                LinearLayoutManager layout) {
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
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                String sortOption = viewCardsViewModel.getSortOption();

                Comparator<OwnedCard> currentComparator = viewCardsViewModel.getCurrentComparator();

                if (!sortOption.equals(menuItem.getTitle())) {
                    sortOption = (String) menuItem.getTitle();

                    if (sortOption.equals("Quantity")) {
                        currentComparator = new OwnedCardQuantityComparator();
                    } else if (sortOption.equals("Card Name")) {
                        currentComparator = new OwnedCardNameComparator();
                    } else if (sortOption.equals("Set Number")) {
                        currentComparator = new OwnedCardSetNumberComparator();
                    } else if (sortOption.equals("Price")) {
                        currentComparator = new OwnedCardPriceComparator();
                    }
                    viewCardsViewModel.setSortOption(sortOption);
                    viewCardsViewModel.setCurrentComparator(currentComparator);

                    ArrayList<OwnedCard> filteredCardsList = viewCardsViewModel.getFilteredCardsList();

                    Comparator<OwnedCard> finalCurrentComparator = currentComparator;
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                viewCardsViewModel.sortData(filteredCardsList,
                                        finalCurrentComparator);

                                view.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        layout.scrollToPositionWithOffset(0, 0);
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
                return true;
            }
        });
        // Showing the popup menu
        popupMenu.show();
    }
}
