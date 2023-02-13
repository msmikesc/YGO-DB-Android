package com.example.ygodb.ui.addCards;

import android.content.Context;
import android.os.AsyncTask;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ygodb.R;
import com.example.ygodb.abs.Util;
import com.example.ygodb.backend.bean.OwnedCard;
import com.example.ygodb.ui.singleCard.SummaryCardToListAdapter;
import com.example.ygodb.ui.viewCardSet.ViewCardSetViewModel;
import com.example.ygodb.ui.viewCards.ViewCardsViewModel;
import com.example.ygodb.ui.viewCardsSummary.ViewCardsSummaryViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

class AddCardsButtonOnClickListener implements View.OnClickListener {

    private final AddCardsViewModel addCardsViewModel;
    private final AddCardToListAdapter adapter;
    private LinearLayoutManager layout;
    private FloatingActionButton fab;
    private Context context;

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
        // Initializing the popup menu and giving the reference as current context
        PopupMenu popupMenu = new PopupMenu(context, fab);

        // Inflating popup menu from popup_menu.xml file
        popupMenu.getMenuInflater().inflate(R.menu.save_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                if (menuItem.getTitle().equals("Save Cards") && addCardsViewModel.getCardsList().size() > 0) {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {

                            addCardsViewModel.saveToDB();

                            ViewCardsViewModel viewCardsViewModel =
                                    new ViewModelProvider(Util.getViewModelOwner()).get(ViewCardsViewModel.class);
                            viewCardsViewModel.refreshViewDBUpdate();

                            ViewCardSetViewModel viewCardSetViewModel =
                                    new ViewModelProvider(Util.getViewModelOwner()).get(ViewCardSetViewModel.class);
                            viewCardSetViewModel.refreshViewDBUpdate();

                            ViewCardsSummaryViewModel viewCardsSummaryViewModel =
                                    new ViewModelProvider(Util.getViewModelOwner()).get(ViewCardsSummaryViewModel.class);
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
