package com.example.ygodb.ui.viewSoldCards;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ygodb.model.PartialScrollEndlessScrollListener;
import com.example.ygodb.util.AndroidUtil;
import com.example.ygodb.model.EndlessScrollListener;
import com.example.ygodb.databinding.FragmentViewcardsBinding;
import ygodb.commonlibrary.utility.YGOLogger;

import java.util.concurrent.Executors;

public class ViewSoldCardsFragment extends Fragment {

	private FragmentViewcardsBinding binding;
	private LinearLayoutManager layout;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewSoldCardsViewModel viewSoldCardsViewModel =
				new ViewModelProvider(AndroidUtil.getViewModelOwner()).get(ViewSoldCardsViewModel.class);

		binding = FragmentViewcardsBinding.inflate(inflater, container, false);
		View root = binding.getRoot();

		RecyclerView cardsListView = binding.viewList;

		SoldCardToListAdapter adapter = new SoldCardToListAdapter(viewSoldCardsViewModel.getCardsList());

		final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
		layout = linearLayoutManager;
		cardsListView.setLayoutManager(linearLayoutManager);
		cardsListView.setAdapter(adapter);

		EndlessScrollListener scrollListener = new PartialScrollEndlessScrollListener<>(linearLayoutManager, viewSoldCardsViewModel,
																					  adapter);
		cardsListView.addOnScrollListener(scrollListener);

		binding.fab.setOnClickListener(
				new ViewSoldCardsSortButtonOnClickListener(binding.fab, getContext(), viewSoldCardsViewModel, adapter, layout));

		binding.cardSearch.setText(viewSoldCardsViewModel.getCardNameSearch());

		binding.cardSearch.addTextChangedListener(
				new ViewSoldCardsSearchBarChangedListener(binding.cardSearch, viewSoldCardsViewModel, adapter, layout));

		if (viewSoldCardsViewModel.getCardsList().isEmpty()) {
			Executors.newSingleThreadExecutor().execute(() -> {
				try {
					viewSoldCardsViewModel.getCardsList().addAll(viewSoldCardsViewModel.loadMoreData(viewSoldCardsViewModel.getSortOrder(),
																									 ViewSoldCardsViewModel.LOADING_LIMIT,
																									 0, null));

					root.post(adapter::notifyDataSetChanged);
				} catch (Exception e) {
					YGOLogger.logException(e);
				}
			});
		}

		viewSoldCardsViewModel.getDbRefreshIndicator().observe(getViewLifecycleOwner(), aBoolean -> {
			if (aBoolean) {
				viewSoldCardsViewModel.setDbRefreshIndicatorFalse();
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