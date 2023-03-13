package com.example.ygodb.ui.viewCardSet;

import android.os.AsyncTask;
import android.os.Handler;
import android.text.Editable;
import android.widget.EditText;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ygodb.abs.TextChangedListener;
import com.example.ygodb.backend.bean.OwnedCard;
import com.example.ygodb.ui.singleCard.SingleCardToListAdapter;

import java.util.ArrayList;

class ViewCardSet_CardSearchBarChangedListener extends TextChangedListener<EditText> {
    private final ViewCardSetViewModel viewCardsViewModel;
    private final SingleCardToListAdapter adapter;
    private LinearLayoutManager layout;

    private final Handler handler = new Handler();

    public ViewCardSet_CardSearchBarChangedListener(EditText searchBar, ViewCardSetViewModel viewCardsViewModel, SingleCardToListAdapter adapter, LinearLayoutManager layout) {
        super(searchBar);
        this.viewCardsViewModel = viewCardsViewModel;
        this.adapter = adapter;
        this.layout=layout;
    }

    @Override
    public void onTextChanged(EditText target, Editable s) {
        String cardNameSearch = s.toString().toUpperCase();

        if(viewCardsViewModel.getCardNameSearch().equals(cardNameSearch)){
            //nothing to update
            return;
        }

        viewCardsViewModel.setCardNameSearch(cardNameSearch);

        ArrayList<OwnedCard> cardsList = viewCardsViewModel.getCardsList();
        ArrayList<OwnedCard> filteredCardsList = viewCardsViewModel.getFilteredCardsList();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {

                    if(viewCardsViewModel.isCardNameMode()){
                        viewCardsViewModel.loadInitialCardNameData(cardNameSearch);
                    }
                    else {
                        filteredCardsList.clear();

                        for (OwnedCard current : cardsList) {
                            if (cardNameSearch.equals("") || current.cardName.toUpperCase().contains(cardNameSearch)) {
                                filteredCardsList.add(current);
                            }
                        }

                        viewCardsViewModel.sortData(filteredCardsList, viewCardsViewModel.getCurrentComparator());
                    }

                    handler.post(new Runnable() {
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
}
