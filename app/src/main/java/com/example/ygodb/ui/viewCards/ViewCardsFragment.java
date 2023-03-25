package com.example.ygodb.ui.viewCards;

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

import com.example.ygodb.abs.Util;
import com.example.ygodb.databinding.FragmentViewcardsBinding;
import com.example.ygodb.abs.EndlessScrollListener;
import com.example.ygodb.ui.addCards.AddCardsViewModel;
import com.example.ygodb.ui.sellCards.SellCardsViewModel;
import com.example.ygodb.ui.singleCard.SingleCardToListAdapter;

import java.util.ArrayList;

public class ViewCardsFragment extends Fragment {

    private FragmentViewcardsBinding binding;
    private LinearLayoutManager layout;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ViewCardsViewModel viewCardsViewModel =
                new ViewModelProvider(Util.getViewModelOwner()).get(ViewCardsViewModel.class);

        AddCardsViewModel addCardsViewModel =
                new ViewModelProvider(Util.getViewModelOwner()).get(AddCardsViewModel.class);

        SellCardsViewModel sellCardsViewModel =
                new ViewModelProvider(Util.getViewModelOwner()).get(SellCardsViewModel.class);

        binding = FragmentViewcardsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView cardsListView = binding.viewList;

        SingleCardToListAdapter adapter = new SingleCardToListAdapter(
                viewCardsViewModel.getCardsList(), addCardsViewModel, sellCardsViewModel);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        layout = linearLayoutManager;
        cardsListView.setLayoutManager(linearLayoutManager);
        cardsListView.setAdapter(adapter);

        EndlessScrollListener scrollListener = new ViewCardsEndlessScrollListener(linearLayoutManager, viewCardsViewModel, adapter);
        cardsListView.addOnScrollListener(scrollListener);

        binding.fab.setOnClickListener(new ViewCardsSortButtonOnClickListener(binding.fab,getContext(), viewCardsViewModel, adapter, layout));

        binding.cardSearch.addTextChangedListener(new ViewCardsSearchBarChangedListener(binding.cardSearch, viewCardsViewModel, adapter, layout));

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
        layout=null;
    }

}