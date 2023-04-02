package com.example.ygodb.ui.viewCardsSummary;

import android.os.AsyncTask;
import android.os.Handler;
import android.text.Editable;
import android.widget.EditText;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ygodb.abs.TextChangedListener;
import com.example.ygodb.backend.bean.OwnedCard;
import com.example.ygodb.ui.singleCard.SingleCardToListAdapter;
import com.example.ygodb.ui.singleCard.SummaryCardToListAdapter;
import com.example.ygodb.ui.viewCards.ViewCardsViewModel;

import java.util.ArrayList;

class ViewCardSummarySearchBarChangedListener extends TextChangedListener<EditText> {
    private final ViewCardsSummaryViewModel viewCardsViewModel;
    private final SummaryCardToListAdapter adapter;
    private LinearLayoutManager layout;

    private final Handler handler = new Handler();

    public ViewCardSummarySearchBarChangedListener(EditText searchBar, ViewCardsSummaryViewModel viewCardsViewModel, SummaryCardToListAdapter adapter, LinearLayoutManager layout) {
        super(searchBar);
        this.viewCardsViewModel = viewCardsViewModel;
        this.adapter = adapter;
        this.layout=layout;
    }

    @Override
    public void onTextChanged(EditText target, Editable s) {
        String cardNameSearch = s.toString();

        if(viewCardsViewModel.getCardNameSearch().equals(cardNameSearch)){
            //nothing to update
            return;
        }

        viewCardsViewModel.setCardNameSearch(cardNameSearch);

        ArrayList<OwnedCard> cardsList = viewCardsViewModel.getCardsList();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<OwnedCard> newList = viewCardsViewModel.loadMoreData(viewCardsViewModel.getSortOrder(),
                            ViewCardsViewModel.LOADING_LIMIT, 0, cardNameSearch);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            viewCardsViewModel.setCardsList(newList);
                            adapter.setOwnedCards(newList);
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
}
