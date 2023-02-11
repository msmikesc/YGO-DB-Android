package com.example.ygodb.ui.viewCards;

import android.os.AsyncTask;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ygodb.abs.EndlessScrollListener;
import com.example.ygodb.backend.bean.OwnedCard;
import com.example.ygodb.ui.singleCard.SingleCardToListAdapter;

import java.util.ArrayList;

class ViewCardsEndlessScrollListener extends EndlessScrollListener {
    private final ViewCardsViewModel viewCardsViewModel;
    private final SingleCardToListAdapter adapter;

    public ViewCardsEndlessScrollListener(LinearLayoutManager linearLayoutManager, ViewCardsViewModel viewCardsViewModel, SingleCardToListAdapter adapter) {
        super(linearLayoutManager);
        this.viewCardsViewModel = viewCardsViewModel;
        this.adapter = adapter;
    }

    @Override
    public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<OwnedCard> moreCards =
                            viewCardsViewModel.loadMoreData(viewCardsViewModel.getSortOrder(),
                                    viewCardsViewModel.LOADING_LIMIT,
                                    page * viewCardsViewModel.LOADING_LIMIT,
                                    viewCardsViewModel.getCardNameSearch());
                    int curSize = adapter.getItemCount();

                    ArrayList<OwnedCard> cardsList = viewCardsViewModel.getCardsList();

                    cardsList.addAll(moreCards);

                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyItemRangeInserted(curSize, moreCards.size() - 1);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


    }
}
