package com.example.ygodb.ui.viewCardsSummary;

import android.content.Context;
import android.os.AsyncTask;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ygodb.R;
import com.example.ygodb.backend.bean.OwnedCard;
import com.example.ygodb.ui.singleCard.SummaryCardToListAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

class ViewCardSummarySortButtonOnClickListener implements View.OnClickListener {

    private final ViewCardsSummaryViewModel viewCardsViewModel;
    private final SummaryCardToListAdapter adapter;
    private LinearLayoutManager layout;
    private FloatingActionButton fab;
    private Context context;

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

                if (!sortOption.equals(menuItem.getTitle())) {
                    sortOption = (String) menuItem.getTitle();

                    if(sortOption.equals("Date Bought")){
                        sortOrder = "maxDate desc, cardName asc";
                    }
                    else if(sortOption.equals("Card Name")){
                        sortOrder = "cardName asc, dateBought desc";
                    }
                    else if(sortOption.equals("Quantity")){
                        sortOrder = "totalQuantity desc, cardName asc";
                    }
                    else if(sortOption.equals("Price")){
                        sortOrder = "avgPrice desc, cardName asc";
                    }
                    viewCardsViewModel.setSortOption(sortOption);
                    viewCardsViewModel.setSortOrder(sortOrder);

                    ArrayList<OwnedCard> cardsList = viewCardsViewModel.getCardsList();

                    String finalSortOrder = sortOrder;
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                cardsList.clear();
                                ArrayList<OwnedCard> moreCards =
                                        viewCardsViewModel.loadMoreData(finalSortOrder,
                                                viewCardsViewModel.LOADING_LIMIT,
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
