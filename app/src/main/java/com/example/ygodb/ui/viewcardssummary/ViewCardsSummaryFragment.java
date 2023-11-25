package com.example.ygodb.ui.viewcardssummary;

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
import com.example.ygodb.abs.EndlessScrollListener;
import com.example.ygodb.databinding.FragmentViewcardsSummaryBinding;
import com.example.ygodb.ui.singlecard.SummaryCardToListAdapter;
import com.example.ygodb.ui.viewcards.ViewCardsViewModel;
import ygodb.commonlibrary.utility.YGOLogger;

import java.util.concurrent.Executors;

public class ViewCardsSummaryFragment extends Fragment {

	private FragmentViewcardsSummaryBinding binding;
	private LinearLayoutManager layout = null;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewCardsSummaryViewModel viewCardsViewModel =
				new ViewModelProvider(AndroidUtil.getViewModelOwner()).get(ViewCardsSummaryViewModel.class);

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

		binding.fab.setOnClickListener(
				new ViewCardSummarySortButtonOnClickListener(binding.fab, getContext(), viewCardsViewModel, adapter, layout));

		binding.cardSearch.addTextChangedListener(
				new ViewCardSummarySearchBarChangedListener(binding.cardSearch, viewCardsViewModel, adapter, layout));

		binding.cardSearch.setText(viewCardsViewModel.getCardNameSearch());

		if (viewCardsViewModel.getCardsList().isEmpty()) {
			Executors.newSingleThreadExecutor().execute(() -> {
				try {
					viewCardsViewModel.getCardsList()
							.addAll(viewCardsViewModel.loadMoreData(viewCardsViewModel.getSortOrder(), ViewCardsViewModel.LOADING_LIMIT, 0,
																	null));

					root.post(adapter::notifyDataSetChanged);
				} catch (Exception e) {
					YGOLogger.logException(e);
				}
			});
		}

		viewCardsViewModel.getDbRefreshIndicator().observe(getViewLifecycleOwner(), aBoolean -> {
			if (aBoolean) {
				viewCardsViewModel.setDbRefreshIndicatorFalse();
				layout.scrollToPositionWithOffset(0, 0);
				adapter.notifyDataSetChanged();
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