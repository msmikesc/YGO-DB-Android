package com.example.ygodb.ui.viewCardsSummary;

import android.content.Context;
import android.os.AsyncTask;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ygodb.R;
import ygodb.commonLibrary.bean.OwnedCard;
import com.example.ygodb.ui.singleCard.SummaryCardToListAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

class ViewCardSummarySortButtonOnClickListener implements View.OnClickListener {

    private final ViewCardsSummaryViewModel viewCardsViewModel;
    private final SummaryCardToListAdapter adapter;
    private final LinearLayoutManager layout;
    private final FloatingActionButton fab;
    private final Context context;

    public ViewCardSummarySortButtonOnClickListener(FloatingActionButton fab, Context context,
                                                    ViewCardsSummaryViewModel viewCardsViewModel,
                                                    SummaryCardToListAdapter adapter,
                                                    LinearLayoutManager layout) {
        this.viewCardsViewModel = viewCardsViewModel;
        this.adapter = adapter;
        this.layout = layout;
        this.fab =fab;
        this.context = context;
    }

    @Override
    public void onClick(View view) {
        // Initializing the popup menu and giving the reference as current context
        PopupMenu popupMenu = new PopupMenu(context, fab);

        // Inflating popup menu from popup_menu.xml file
        popupMenu.getMenuInflater().inflate(R.menu.sort_menu_summary, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

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
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                cardsList.clear();
                                List<OwnedCard> moreCards =
                                        viewCardsViewModel.loadMoreData(finalSortOrder,
                                                ViewCardsSummaryViewModel.LOADING_LIMIT,
                                                0, viewCardsViewModel.getCardNameSearch());
                                cardsList.addAll(moreCards);

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
