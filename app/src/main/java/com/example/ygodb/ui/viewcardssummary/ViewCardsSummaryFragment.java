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
import com.example.ygodb.R;
import com.example.ygodb.databinding.FragmentViewcardsSummaryBinding;
import com.example.ygodb.model.EndlessScrollListener;
import com.example.ygodb.model.partialscroll.PartialScrollEndlessScrollListener;
import com.example.ygodb.model.partialscroll.PartialScrollSearchBarChangedListener;
import com.example.ygodb.model.partialscroll.PartialScrollSortButtonOnClickListener;
import com.example.ygodb.ui.viewcards.ViewCardsViewModel;
import com.example.ygodb.util.AndroidUtil;
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


		EndlessScrollListener scrollListener = new PartialScrollEndlessScrollListener<>(linearLayoutManager, viewCardsViewModel, adapter);
		cardsListView.addOnScrollListener(scrollListener);

		binding.fab.setOnClickListener(
				new PartialScrollSortButtonOnClickListener<>(binding.fab, getContext(), viewCardsViewModel, adapter, layout, R.menu.sort_menu_summary));

		binding.cardSearch.addTextChangedListener(
				new PartialScrollSearchBarChangedListener<>(binding.cardSearch, viewCardsViewModel, adapter, layout));

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