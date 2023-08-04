package com.example.ygodb.ui.viewsetboxes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ygodb.R;
import ygodb.commonlibrary.bean.SetBox;

import java.util.List;

public class SingleBoxToListAdapter extends RecyclerView.Adapter<SingleBoxToListAdapter.ItemViewHolder> {
	private List<SetBox> setBoxes;

	private final ViewBoxSetViewModel viewBoxSetViewModel;

	public SingleBoxToListAdapter(List<SetBox> setBoxes, ViewBoxSetViewModel viewBoxSetViewModel) {
		this.setBoxes = setBoxes;
		this.viewBoxSetViewModel = viewBoxSetViewModel;

	}

	public void setSetBoxes(List<SetBox> setBoxes) {
		this.setBoxes = setBoxes;
	}

	@NonNull
	@Override
	public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_single_edit_box, parent, false);

		return new ItemViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ItemViewHolder viewHolder, int position) {

		SetBox current = setBoxes.get(position);

		viewHolder.setName.setText(current.getSetName());
		viewHolder.boxLabel.setText(current.getBoxLabel());
		viewHolder.setCode.setText(current.getSetCode());
	}

	@Override
	public int getItemCount() {
		return setBoxes.size();
	}

	public static class ItemViewHolder extends RecyclerView.ViewHolder {

		TextView setName;
		EditText boxLabel;
		TextView setCode;

		public ItemViewHolder(@NonNull View view) {
			super(view);

			setName = view.findViewById(R.id.textViewSetName);
			boxLabel = view.findViewById(R.id.editTextBoxLabel);
			setCode = view.findViewById(R.id.textViewSetCode);

		}

	}

}
