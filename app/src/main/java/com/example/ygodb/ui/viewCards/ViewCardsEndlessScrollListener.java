package com.example.ygodb.ui.viewCards;

import android.os.AsyncTask;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ygodb.abs.EndlessScrollListener;
import ygodb.commonLibrary.bean.OwnedCard;
import com.example.ygodb.ui.singleCard.SingleCardToListAdapter;

import java.util.List;

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
                    List<OwnedCard> moreCards =
                            viewCardsViewModel.loadMoreData(viewCardsViewModel.getSortOrder(),
                                    ViewCardsViewModel.LOADING_LIMIT,
                                    page * ViewCardsViewModel.LOADING_LIMIT,
                                    viewCardsViewModel.getCardNameSearch());
                    int curSize = adapter.getItemCount();

                    List<OwnedCard> cardsList = viewCardsViewModel.getCardsList();

                    cardsList.addAll(moreCards);

                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyItemRangeInserted(curSize, moreCards.size());
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


    }
}
