package com.example.ygodb.ui.viewsetboxes;

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
import com.example.ygodb.databinding.FragmentBoxLookupBinding;

public class BoxLookupFragment extends Fragment {

	private FragmentBoxLookupBinding binding;

	private LinearLayoutManager layout = null;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {

		//TODO support adding new by creating an entry by set code
		//TODO save updated box label

		ViewBoxSetViewModel viewBoxSetViewModel =
				new ViewModelProvider(AndroidUtil.getViewModelOwner()).get(ViewBoxSetViewModel.class);

		binding = FragmentBoxLookupBinding.inflate(inflater, container, false);
		View root = binding.getRoot();

		RecyclerView boxListViewResults = binding.boxListViewResults;

		SingleBoxToListAdapter adapter = new SingleBoxToListAdapter(
				viewBoxSetViewModel.getBoxList(), viewBoxSetViewModel);

		final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getContext());
		layout = linearLayoutManager;
		boxListViewResults.setLayoutManager(linearLayoutManager);
		boxListViewResults.setAdapter(adapter);

		binding.boxSearchEditText.addTextChangedListener(new BoxLookupSearchBarChangedListener(binding.boxSearchEditText, viewBoxSetViewModel, adapter, layout));

		viewBoxSetViewModel.getDbRefreshIndicator().observe(getViewLifecycleOwner(), aBoolean -> {
			if (aBoolean) {
				viewBoxSetViewModel.setDbRefreshIndicatorFalse();
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
	}
}