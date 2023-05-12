package com.example.ygodb.ui.viewCardsSummary;

import android.os.AsyncTask;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ygodb.abs.EndlessScrollListener;
import ygodb.commonLibrary.bean.OwnedCard;
import com.example.ygodb.ui.singleCard.SummaryCardToListAdapter;

import java.util.List;

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
                    List<OwnedCard> moreCards =
                            viewCardsViewModel.loadMoreData(viewCardsViewModel.getSortOrder(),
                                    ViewCardsSummaryViewModel.LOADING_LIMIT,
                                    page * ViewCardsSummaryViewModel.LOADING_LIMIT,
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
