package com.example.ygodb.ui.viewCardSet;

import android.os.AsyncTask;
import android.os.Handler;
import android.text.Editable;
import android.widget.EditText;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ygodb.abs.TextChangedListener;
import com.example.ygodb.ui.singleCard.SingleCardToListAdapter;

class ViewCardSet_SetSearchBarChangedListener extends TextChangedListener<EditText> {
    private final ViewCardSetViewModel viewCardsViewModel;
    private final SingleCardToListAdapter adapter;
    private LinearLayoutManager layout;

    private final Handler handler = new Handler();

    public ViewCardSet_SetSearchBarChangedListener(EditText searchBar, ViewCardSetViewModel viewCardsViewModel, SingleCardToListAdapter adapter, LinearLayoutManager layout) {
        super(searchBar);
        this.viewCardsViewModel = viewCardsViewModel;
        this.adapter = adapter;
        this.layout=layout;
    }

    @Override
    public void onTextChanged(EditText target, Editable s) {
        String setNameSearch = s.toString();

        if(viewCardsViewModel.getSetNameSearch().equals(setNameSearch)){
            //nothing to update
            return;
        }

        viewCardsViewModel.setSetNameSearch(setNameSearch);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    viewCardsViewModel.loadInitialData(setNameSearch);

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
