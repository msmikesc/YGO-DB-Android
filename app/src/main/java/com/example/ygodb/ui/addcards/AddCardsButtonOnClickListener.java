package com.example.ygodb.ui.addcards;

import android.content.Context;
import android.view.View;
import android.widget.PopupMenu;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ygodb.R;
import com.example.ygodb.abs.AndroidUtil;
import com.example.ygodb.ui.viewcardset.ViewCardSetViewModel;
import com.example.ygodb.ui.viewcards.ViewCardsViewModel;
import com.example.ygodb.ui.viewcardssummary.ViewCardsSummaryViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.concurrent.Executors;

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
        popupMenu.setOnMenuItemClickListener(menuItem -> {

            if(addCardsViewModel.getCardsList().isEmpty()){
                return true;
            }

            if (menuItem.getTitle().equals("Invert Editions") ) {
                Executors.newSingleThreadExecutor().execute(() -> {

                    addCardsViewModel.invertAllEditions();

                    view.post(adapter::notifyDataSetChanged);

                });
            }

            if (menuItem.getTitle().equals("Set Prices Zero") ) {
                Executors.newSingleThreadExecutor().execute(() -> {

                    addCardsViewModel.setAllPricesZero();

                    view.post(adapter::notifyDataSetChanged);

                });
            }

            if (menuItem.getTitle().equals("Set Estimated Prices") ) {
                Executors.newSingleThreadExecutor().execute(() -> {

                    addCardsViewModel.setAllPricesEstimate();

                    view.post(adapter::notifyDataSetChanged);

                });
            }

            if (menuItem.getTitle().equals("Set API Prices") ) {
                Executors.newSingleThreadExecutor().execute(() -> {

                    addCardsViewModel.setAllPricesAPI();

                    view.post(adapter::notifyDataSetChanged);

                });
            }

            if (menuItem.getTitle().equals("Save Cards") ) {
                Executors.newSingleThreadExecutor().execute(() -> {

                    addCardsViewModel.saveToDB();

                    view.post(adapter::notifyDataSetChanged);

                    ViewCardsViewModel viewCardsViewModel =
                            new ViewModelProvider(AndroidUtil.getViewModelOwner()).get(ViewCardsViewModel.class);
                    viewCardsViewModel.refreshViewDBUpdate();

                    ViewCardSetViewModel viewCardSetViewModel =
                            new ViewModelProvider(AndroidUtil.getViewModelOwner()).get(ViewCardSetViewModel.class);
                    viewCardSetViewModel.refreshViewDBUpdate();

                    ViewCardsSummaryViewModel viewCardsSummaryViewModel =
                            new ViewModelProvider(AndroidUtil.getViewModelOwner()).get(ViewCardsSummaryViewModel.class);
                    viewCardsSummaryViewModel.refreshViewDBUpdate();

                });
            }
            return true;
        });
        // Showing the popup menu
        popupMenu.show();
    }
}