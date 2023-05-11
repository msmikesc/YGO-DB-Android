package com.example.ygodb.ui.addCards;

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

class AddCardsButtonOnClickListener implements View.OnClickListener {

    private final AddCardsViewModel addCardsViewModel;
    private final AddCardToListAdapter adapter;
    private final LinearLayoutManager layout;
    private final FloatingActionButton fab;
    private final Context context;

    public AddCardsButtonOnClickListener(FloatingActionButton fab, Context context,
                                         AddCardsViewModel addCardsViewModel,
                                         AddCardToListAdapter adapter,
                                         LinearLayoutManager layout) {
        this.addCardsViewModel = addCardsViewModel;
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

                if(addCardsViewModel.getCardsList().size() < 1){
                    return true;
                }

                if (menuItem.getTitle().equals("Invert Editions") ) {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {

                            addCardsViewModel.invertAllEditions();

                            view.post(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                }
                            });

                        }
                    });
                }

                if (menuItem.getTitle().equals("Set Prices Zero") ) {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {

                            addCardsViewModel.setAllPricesZero();

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

                            addCardsViewModel.setAllPricesEstimate();

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

                            addCardsViewModel.setAllPricesAPI();

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

                            addCardsViewModel.saveToDB();

                            view.post(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                }
                            });

                            ViewCardsViewModel viewCardsViewModel =
                                    new ViewModelProvider(AndroidUtil.getViewModelOwner()).get(ViewCardsViewModel.class);
                            viewCardsViewModel.refreshViewDBUpdate();

                            ViewCardSetViewModel viewCardSetViewModel =
                                    new ViewModelProvider(AndroidUtil.getViewModelOwner()).get(ViewCardSetViewModel.class);
                            viewCardSetViewModel.refreshViewDBUpdate();

                            ViewCardsSummaryViewModel viewCardsSummaryViewModel =
                                    new ViewModelProvider(AndroidUtil.getViewModelOwner()).get(ViewCardsSummaryViewModel.class);
                            viewCardsSummaryViewModel.refreshViewDBUpdate();

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
