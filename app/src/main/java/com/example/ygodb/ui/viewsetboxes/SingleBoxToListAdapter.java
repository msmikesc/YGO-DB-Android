package com.example.ygodb.ui.viewsetboxes;

import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ygodb.R;
import com.example.ygodb.abs.AndroidUtil;
import ygodb.commonlibrary.bean.SetBox;

import java.util.List;

public class SingleBoxToListAdapter extends RecyclerView.Adapter<SingleBoxToListAdapter.ItemViewHolder> {
	private List<SetBox> setBoxes;

	public SingleBoxToListAdapter(List<SetBox> setBoxes) {
		this.setBoxes = setBoxes;

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
		viewHolder.boxLabel.setTag(current); // Set the SetBox as the tag of the EditText for the text changed listener

		viewHolder.removeTextWatcher();
		viewHolder.setName.setText(current.getSetName());
		viewHolder.boxLabel.setText(current.getBoxLabel());
		viewHolder.setPrefix.setText(current.getSetPrefix());
		viewHolder.addTextWatcher();
	}

	@Override
	public int getItemCount() {
		return setBoxes.size();
	}

	public static class ItemViewHolder extends RecyclerView.ViewHolder {

		TextView setName;
		EditText boxLabel;
		TextView setPrefix;

		TextWatcher textWatcher;

		public ItemViewHolder(@NonNull View view) {
			super(view);

			setName = view.findViewById(R.id.textViewSetName);
			boxLabel = view.findViewById(R.id.editTextBoxLabel);
			setPrefix = view.findViewById(R.id.textViewSetPrefix);

			textWatcher = new SingleBoxLabelChangedListener(boxLabel, AndroidUtil.getDBInstance());
			boxLabel.addTextChangedListener(textWatcher);

		}

		public void removeTextWatcher() {
			boxLabel.removeTextChangedListener(textWatcher);
		}

		// Method to add the TextWatcher
		public void addTextWatcher() {
			boxLabel.addTextChangedListener(textWatcher);
		}

	}

}
