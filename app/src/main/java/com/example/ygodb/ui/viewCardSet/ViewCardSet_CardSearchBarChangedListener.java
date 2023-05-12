package com.example.ygodb.ui.viewCardSet;

import android.os.AsyncTask;
import android.os.Handler;
import android.text.Editable;
import android.widget.EditText;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ygodb.abs.TextChangedListener;
import ygodb.commonLibrary.bean.OwnedCard;
import com.example.ygodb.ui.singleCard.SingleCardToListAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class ViewCardSet_CardSearchBarChangedListener extends TextChangedListener<EditText> {
    private final ViewCardSetViewModel viewCardsViewModel;
    private final SingleCardToListAdapter adapter;
    private final LinearLayoutManager layout;

    private final Handler handler = new Handler();

    public ViewCardSet_CardSearchBarChangedListener(EditText searchBar, ViewCardSetViewModel viewCardsViewModel, SingleCardToListAdapter adapter, LinearLayoutManager layout) {
        super(searchBar);
        this.viewCardsViewModel = viewCardsViewModel;
        this.adapter = adapter;
        this.layout=layout;
    }

    @Override
    public void onTextChanged(EditText target, Editable s) {
        String cardNameSearch = s.toString().toLowerCase(Locale.ROOT);

        if(viewCardsViewModel.getCardNameSearch().equals(cardNameSearch)){
            //nothing to update
            return;
        }

        viewCardsViewModel.setCardNameSearch(cardNameSearch);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    List<OwnedCard> results = null;
                    List<OwnedCard> filteredResults = null;

                    if(viewCardsViewModel.isCardNameMode()){
                        results = viewCardsViewModel.getInitialCardNameData(cardNameSearch);
                        filteredResults = new ArrayList<>(results);
                    }
                    else {
                        results = viewCardsViewModel.getCardsList();
                        filteredResults = viewCardsViewModel.getFilteredList(viewCardsViewModel.getCardsList(), cardNameSearch);

                        viewCardsViewModel.sortData(filteredResults, viewCardsViewModel.getCurrentComparator());
                    }

                    List<OwnedCard> finalResults = results;
                    List<OwnedCard> finalFilteredResults = filteredResults;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            viewCardsViewModel.setCardsList(finalResults);
                            viewCardsViewModel.setFilteredCardsList(finalFilteredResults);
                            adapter.setOwnedCards(finalFilteredResults);

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
