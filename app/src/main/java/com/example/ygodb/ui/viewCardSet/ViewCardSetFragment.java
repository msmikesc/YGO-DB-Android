package com.example.ygodb.ui.viewCardSet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ygodb.R;
import com.example.ygodb.databinding.FragmentViewcardsetBinding;
import com.example.ygodb.ui.addCards.AddCardsViewModel;
import com.example.ygodb.ui.singleCard.SingleCardToListAdapter;

public class ViewCardSetFragment extends Fragment {

    private FragmentViewcardsetBinding binding;

    private LinearLayoutManager layout = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ViewCardSetViewModel viewCardSetViewModel =
                new ViewModelProvider(this).get(ViewCardSetViewModel.class);

        AddCardsViewModel addCardsViewModel =
                new ViewModelProvider(getActivity()).get(AddCardsViewModel.class);

        binding = FragmentViewcardsetBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView cardsListView = binding.viewList;

        ArrayAdapter<String> autoCompleteAdapter=
                new ArrayAdapter<String>(getContext(),android.R.layout.simple_dropdown_item_1line,
                        viewCardSetViewModel.getSetNamesDropdownList());
        AutoCompleteTextView textView=(AutoCompleteTextView)root.findViewById(R.id.setSearch);
        textView.setThreshold(3);
        textView.setAdapter(autoCompleteAdapter);

        SingleCardToListAdapter adapter = new SingleCardToListAdapter(viewCardSetViewModel.getFilteredCardsList(), addCardsViewModel);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getContext());
        layout = linearLayoutManager;
        cardsListView.setLayoutManager(linearLayoutManager);
        cardsListView.setAdapter(adapter);

        binding.fab.setOnClickListener(new ViewCardSetSortButtonOnClickListener(binding.fab,getContext(), viewCardSetViewModel, adapter, layout));

        binding.cardSearch.addTextChangedListener(new ViewCardSet_CardSearchBarChangedListener(binding.cardSearch, viewCardSetViewModel, adapter, layout));

        binding.setSearch.addTextChangedListener(new ViewCardSet_SetSearchBarChangedListener(binding.setSearch, viewCardSetViewModel, adapter, layout));

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}