package com.example.ygodb.ui.viewcards;

import android.text.Editable;
import android.widget.EditText;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ygodb.abs.TextChangedListener;
import ygodb.commonlibrary.bean.OwnedCard;
import com.example.ygodb.ui.singlecard.SingleCardToListAdapter;
import ygodb.commonlibrary.utility.YGOLogger;

import java.util.List;
import java.util.concurrent.Executors;

class ViewCardsSearchBarChangedListener extends TextChangedListener<EditText> {
    private final ViewCardsViewModel viewCardsViewModel;
    private final SingleCardToListAdapter adapter;
    private final LinearLayoutManager layout;

    public ViewCardsSearchBarChangedListener(EditText searchBar, ViewCardsViewModel viewCardsViewModel, SingleCardToListAdapter adapter, LinearLayoutManager layout) {
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

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<OwnedCard> newList = viewCardsViewModel.loadMoreData(viewCardsViewModel.getSortOrder(),
                        ViewCardsViewModel.LOADING_LIMIT, 0, cardNameSearch);

                handler.post(() -> {
                    viewCardsViewModel.setCardsList(newList);
                    adapter.setOwnedCards(newList);
                    layout.scrollToPositionWithOffset(0, 0);
                    adapter.notifyDataSetChanged();
                });
            } catch (Exception e) {
                YGOLogger.logException(e);
            }
        });
    }
}
