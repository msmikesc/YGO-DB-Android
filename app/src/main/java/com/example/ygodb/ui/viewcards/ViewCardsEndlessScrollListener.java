package com.example.ygodb.ui.viewcards;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ygodb.abs.EndlessScrollListener;
import ygodb.commonlibrary.bean.OwnedCard;
import com.example.ygodb.ui.singlecard.SingleCardToListAdapter;
import ygodb.commonlibrary.utility.YGOLogger;

import java.util.List;
import java.util.concurrent.Executors;

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

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<OwnedCard> moreCards =
                        viewCardsViewModel.loadMoreData(viewCardsViewModel.getSortOrder(),
                                ViewCardsViewModel.LOADING_LIMIT,
                                page * ViewCardsViewModel.LOADING_LIMIT,
                                viewCardsViewModel.getCardNameSearch());
                int curSize = adapter.getItemCount();

                List<OwnedCard> cardsList = viewCardsViewModel.getCardsList();

                cardsList.addAll(moreCards);

                view.post(() -> adapter.notifyItemRangeInserted(curSize, moreCards.size()));
            } catch (Exception e) {
                YGOLogger.logException(e);
            }
        });


    }
}
