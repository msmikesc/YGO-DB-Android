package com.example.ygodb.ui.viewcardset;

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
import com.example.ygodb.abs.AndroidUtil;
import com.example.ygodb.databinding.FragmentViewcardsetBinding;
import com.example.ygodb.ui.addcards.AddCardsViewModel;
import com.example.ygodb.ui.singlecard.SingleCardToListAdapter;

public class ViewCardSetFragment extends Fragment {

	private FragmentViewcardsetBinding binding;

	private LinearLayoutManager layout = null;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewCardSetViewModel viewCardSetViewModel = new ViewModelProvider(AndroidUtil.getViewModelOwner()).get(ViewCardSetViewModel.class);

		AddCardsViewModel addCardsViewModel = new ViewModelProvider(AndroidUtil.getViewModelOwner()).get(AddCardsViewModel.class);

		binding = FragmentViewcardsetBinding.inflate(inflater, container, false);
		View root = binding.getRoot();

		RecyclerView cardsListView = binding.viewList;

		ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line,
																	  viewCardSetViewModel.getSetNamesDropdownList());
		AutoCompleteTextView textView = root.findViewById(R.id.setSearch);
		textView.setThreshold(3);
		textView.setAdapter(autoCompleteAdapter);

		SingleCardToListAdapter adapter =
				new SingleCardToListAdapter(viewCardSetViewModel.getFilteredCardsList(), addCardsViewModel, null, true);

		final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getContext());
		layout = linearLayoutManager;
		cardsListView.setLayoutManager(linearLayoutManager);
		cardsListView.setAdapter(adapter);

		binding.fab.setOnClickListener(
				new ViewCardSetSortButtonOnClickListener(binding.fab, getContext(), viewCardSetViewModel, adapter, layout));

		binding.cardSearch.addTextChangedListener(
				new ViewCardSetCardSearchBarChangedListener(binding.cardSearch, viewCardSetViewModel, adapter, layout));

		binding.setSearch.addTextChangedListener(
				new ViewCardSetSetSearchBarChangedListener(binding.setSearch, viewCardSetViewModel, adapter, layout));

		viewCardSetViewModel.getDbRefreshIndicator().observe(getViewLifecycleOwner(), aBoolean -> {
			if (aBoolean) {
				viewCardSetViewModel.setDbRefreshIndicatorFalse();
				layout.scrollToPositionWithOffset(0, 0);
				adapter.notifyDataSetChanged();
				autoCompleteAdapter.notifyDataSetChanged();
			}
		});

		return root;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}