package com.example.ygodb.ui.analyzesets;

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
import com.example.ygodb.model.completedata.LoadCompleteDataCardSearchBarChangedListener;
import com.example.ygodb.model.completedata.LoadCompleteDataSetSearchBarChangedListener;
import com.example.ygodb.model.completedata.LoadCompleteDataSortButtonOnClickListener;
import com.example.ygodb.ui.singlecard.SingleCardToListAdapter;
import com.example.ygodb.util.AndroidUtil;

public class AnalyzeCardsFragment extends Fragment {

	private FragmentViewcardsetBinding binding;

	private LinearLayoutManager layout = null;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		AnalyzeCardsViewModel analyzeCardsViewModel =
				new ViewModelProvider(AndroidUtil.getViewModelOwner()).get(AnalyzeCardsViewModel.class);

		binding = FragmentViewcardsetBinding.inflate(inflater, container, false);
		View root = binding.getRoot();

		RecyclerView cardsListView = binding.viewList;

		ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line,
																	  analyzeCardsViewModel.getSetNamesDropdownList());
		AutoCompleteTextView textView = root.findViewById(R.id.setSearch);
		textView.setThreshold(3);
		textView.setAdapter(autoCompleteAdapter);

		SingleCardToListAdapter adapter = new SingleCardToListAdapter(getActivity(), analyzeCardsViewModel.getFilteredCardsList(), null, null, false);

		final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getContext());
		layout = linearLayoutManager;
		cardsListView.setLayoutManager(linearLayoutManager);
		cardsListView.setAdapter(adapter);

		binding.cardSearch.setText(analyzeCardsViewModel.getCardNameSearch());
		binding.setSearch.setText(analyzeCardsViewModel.getSetNameSearch());

		binding.fab.setOnClickListener(
				new LoadCompleteDataSortButtonOnClickListener(binding.fab, getContext(), analyzeCardsViewModel, adapter, layout));

		binding.fabFilterRarity.hide();

		binding.cardSearch.addTextChangedListener(
				new LoadCompleteDataCardSearchBarChangedListener(binding.cardSearch, analyzeCardsViewModel, adapter, layout));

		binding.setSearch.addTextChangedListener(
				new LoadCompleteDataSetSearchBarChangedListener(binding.setSearch, analyzeCardsViewModel, adapter, layout));

		analyzeCardsViewModel.getDbRefreshIndicator().observe(getViewLifecycleOwner(), aBoolean -> {
			if (aBoolean) {
				analyzeCardsViewModel.setDbRefreshIndicatorFalse();
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