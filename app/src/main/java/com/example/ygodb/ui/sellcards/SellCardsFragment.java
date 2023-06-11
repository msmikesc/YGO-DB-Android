package com.example.ygodb.ui.sellcards;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ygodb.abs.AndroidUtil;
import com.example.ygodb.databinding.FragmentSellcardsBinding;


public class SellCardsFragment extends Fragment {

    private FragmentSellcardsBinding binding;
    private LinearLayoutManager layout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SellCardsViewModel sellCardsViewModel =
                new ViewModelProvider(AndroidUtil.getViewModelOwner()).get(SellCardsViewModel.class);

        binding = FragmentSellcardsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView cardsListView = binding.viewList;

        SellCardToListAdapter adapter = new SellCardToListAdapter(sellCardsViewModel.getCardsList(), sellCardsViewModel);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        layout = linearLayoutManager;
        cardsListView.setLayoutManager(linearLayoutManager);
        cardsListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        binding.fab.setOnClickListener(new SellCardsButtonOnClickListener(binding.fab,getContext(), sellCardsViewModel, adapter, layout));

        return root;
    }

}