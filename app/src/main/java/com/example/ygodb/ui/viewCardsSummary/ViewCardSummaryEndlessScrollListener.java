package com.example.ygodb.ui.viewCardsSummary;

import android.os.AsyncTask;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ygodb.abs.EndlessScrollListener;
import ygodb.commonLibrary.bean.OwnedCard;
import com.example.ygodb.ui.singleCard.SummaryCardToListAdapter;

import java.util.ArrayList;

class ViewCardSummaryEndlessScrollListener extends EndlessScrollListener {
    private final ViewCardsSummaryViewModel viewCardsViewModel;
    private final SummaryCardToListAdapter adapter;

    public ViewCardSummaryEndlessScrollListener(LinearLayoutManager linearLayoutManager, ViewCardsSummaryViewModel viewCardsViewModel, SummaryCardToListAdapter adapter) {
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
