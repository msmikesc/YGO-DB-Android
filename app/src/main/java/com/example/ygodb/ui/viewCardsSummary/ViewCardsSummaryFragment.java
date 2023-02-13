package com.example.ygodb.ui.viewCardsSummary;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ygodb.abs.EndlessScrollListener;
import com.example.ygodb.abs.Util;
import com.example.ygodb.databinding.FragmentViewcardsSummaryBinding;
import com.example.ygodb.ui.singleCard.SummaryCardToListAdapter;
import com.example.ygodb.ui.viewCards.ViewCardsViewModel;

public class ViewCardsSummaryFragment extends Fragment {

    private FragmentViewcardsSummaryBinding binding;
    private static LinearLayoutManager layout = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ViewCardsSummaryViewModel viewCardsViewModel =
                new ViewModelProvider(Util.getViewModelOwner()).get(ViewCardsSummaryViewModel.class);

        binding = FragmentViewcardsSummaryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView cardsListView = binding.viewList;

        SummaryCardToListAdapter adapter = new SummaryCardToListAdapter(viewCardsViewModel.getCardsList());

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getContext());
        layout = linearLayoutManager;
        cardsListView.setLayoutManager(linearLayoutManager);
        cardsListView.setAdapter(adapter);


        EndlessScrollListener scrollListener = new ViewCardSummaryEndlessScrollListener(linearLayoutManager, viewCardsViewModel, adapter);
        cardsListView.addOnScrollListener(scrollListener);

        binding.fab.setOnClickListener(new ViewCardSummarySortButtonOnClickListener(binding.fab,getContext(), viewCardsViewModel, adapter, layout));

        binding.cardSearch.addTextChangedListener(new ViewCardSummarySearchBarChangedListener(binding.cardSearch, viewCardsViewModel, adapter, layout));

        if(viewCardsViewModel.getCardsList().isEmpty()) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        viewCardsViewModel.getCardsList().addAll(viewCardsViewModel.loadMoreData(viewCardsViewModel.getSortOrder(),
                                ViewCardsViewModel.LOADING_LIMIT, 0, null));

                        root.post(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        viewCardsViewModel.getDbRefreshIndicator().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    viewCardsViewModel.setDbRefreshIndicatorFalse();
                    layout.scrollToPositionWithOffset(0, 0);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        layout = null;
    }
}