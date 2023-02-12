package com.example.ygodb.ui.addCards;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ygodb.databinding.FragmentAddcardsBinding;
import com.example.ygodb.databinding.FragmentViewcardsBinding;

public class AddCardsFragment extends Fragment {

    private FragmentAddcardsBinding binding;
    private LinearLayoutManager layout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AddCardsViewModel addCardsViewModel =
                new ViewModelProvider(getActivity()).get(AddCardsViewModel.class);

        binding = FragmentAddcardsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView cardsListView = binding.viewList;

        AddCardToListAdapter adapter = new AddCardToListAdapter(addCardsViewModel.getCardsList(), addCardsViewModel);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        layout = linearLayoutManager;
        cardsListView.setLayoutManager(linearLayoutManager);
        cardsListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        return root;
    }

}