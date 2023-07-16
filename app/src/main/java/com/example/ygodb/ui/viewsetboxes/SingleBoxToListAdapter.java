package com.example.ygodb.ui.viewsetboxes;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ygodb.R;
import com.example.ygodb.abs.AndroidUtil;
import com.example.ygodb.ui.addcards.AddCardsViewModel;
import com.example.ygodb.ui.sellcards.SellCardsViewModel;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.bean.SetBox;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.YGOLogger;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;

public class SingleBoxToListAdapter extends RecyclerView.Adapter<SingleBoxToListAdapter.ItemViewHolder> {
	private List<SetBox> setBoxes;

	private final ViewBoxSetViewModel viewBoxSetViewModel;

	public SingleBoxToListAdapter(List<SetBox> setBoxes,
								  ViewBoxSetViewModel viewBoxSetViewModel) {
		this.setBoxes = setBoxes;
		this.viewBoxSetViewModel = viewBoxSetViewModel;

	}

	public void setSetBoxes(List<SetBox> setBoxes) {
		this.setBoxes = setBoxes;
	}

	@NonNull
	@Override
	public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).
				inflate(R.layout.fragment_single_edit_box, parent, false);

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
