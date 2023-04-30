package com.example.ygodb.ui.sellCards;

import android.content.Context;
import android.os.AsyncTask;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ygodb.R;
import com.example.ygodb.abs.AndroidUtil;
import com.example.ygodb.ui.viewCardSet.ViewCardSetViewModel;
import com.example.ygodb.ui.viewCards.ViewCardsViewModel;
import com.example.ygodb.ui.viewCardsSummary.ViewCardsSummaryViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

class SellCardsButtonOnClickListener implements View.OnClickListener {

    private final SellCardsViewModel sellCardsViewModel;
    private final SellCardToListAdapter adapter;
    private LinearLayoutManager layout;
    private FloatingActionButton fab;
    private Context context;

    public SellCardsButtonOnClickListener(FloatingActionButton fab, Context context,
                                          SellCardsViewModel sellCardsViewModel,
                                          SellCardToListAdapter adapter,
                                          LinearLayoutManager layout) {
        this.sellCardsViewModel = sellCardsViewModel;
        this.adapter = adapter;
        this.layout = layout;
        this.fab =fab;
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
        popupMenu.getMenuInflater().inflate(R.menu.save_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                if(sellCardsViewModel.getCardsList().size() < 1){
                    return true;
                }

                if (menuItem.getTitle().equals("Set Prices Zero") ) {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {

                            sellCardsViewModel.setAllPricesZero();

                            view.post(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                }
                            });

                        }
                    });
                }

                if (menuItem.getTitle().equals("Set Estimated Prices") ) {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {

                            sellCardsViewModel.setAllPricesEstimate();

                            view.post(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                }
                            });

                        }
                    });
                }

                if (menuItem.getTitle().equals("Set API Prices") ) {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {

                            sellCardsViewModel.setAllPricesAPI();

                            view.post(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                }
                            });

                        }
                    });
                }

                if (menuItem.getTitle().equals("Save Cards") ) {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {

                            sellCardsViewModel.saveToDB();

                            ViewCardsViewModel viewCardsViewModel =
                                    new ViewModelProvider(AndroidUtil.getViewModelOwner()).get(ViewCardsViewModel.class);
                            viewCardsViewModel.refreshViewDBUpdate();

                            ViewCardSetViewModel viewCardSetViewModel =
                                    new ViewModelProvider(AndroidUtil.getViewModelOwner()).get(ViewCardSetViewModel.class);
                            viewCardSetViewModel.refreshViewDBUpdate();

                            ViewCardsSummaryViewModel viewCardsSummaryViewModel =
                                    new ViewModelProvider(AndroidUtil.getViewModelOwner()).get(ViewCardsSummaryViewModel.class);
                            viewCardsSummaryViewModel.refreshViewDBUpdate();

                            view.post(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                }
                            });

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
