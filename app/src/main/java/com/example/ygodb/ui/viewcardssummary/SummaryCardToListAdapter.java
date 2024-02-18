package com.example.ygodb.ui.viewcardssummary;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ygodb.R;
import com.example.ygodb.model.ItemsListAdapter;
import com.example.ygodb.ui.fullscreendetails.ViewCardFullScreenFragment;
import com.example.ygodb.util.AndroidUtil;
import ygodb.commonlibrary.bean.OwnedCard;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;

public class SummaryCardToListAdapter extends ItemsListAdapter<OwnedCard,SummaryCardToListAdapter.ItemViewHolder> {

	private final FragmentActivity context;

	public SummaryCardToListAdapter(FragmentActivity context, List<OwnedCard> ownedCards) {
		super(ownedCards);
		this.context = context;
	}

	@NonNull
	@Override
	public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_summarycard, parent, false);
		return new ItemViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ItemViewHolder viewHolder, int position) {

		OwnedCard current = itemsList.get(position);

		viewHolder.title.setText(current.getCardName());
		viewHolder.setName.setText(current.getSetName());
		viewHolder.cardQuantity.setText(String.valueOf(current.getQuantity()));
		if (current.getPriceBought() != null) {
			double price = Double.parseDouble(current.getPriceBought());
			viewHolder.cardPrice.setText(String.format(Locale.ROOT, "$%.2f", price));
		}
		viewHolder.cardDate.setText(current.getDateBought());
		viewHolder.rarity.setText(current.getSetRarity());

		int imagePasscode = current.getPasscode();

		if (current.getAltArtPasscode() != null && current.getAltArtPasscode() != 0) {
			imagePasscode = current.getAltArtPasscode();
		}

		try {
			// get input stream
			InputStream ims = AndroidUtil.getAppContext().getAssets().open("pics/" + imagePasscode + ".jpg");
			// load image as Drawable
			Drawable d = Drawable.createFromStream(ims, null);
			// set image to ImageView
			viewHolder.cardImage.setImageDrawable(d);
		} catch (Exception ex) {
			viewHolder.cardImage.setImageDrawable(null);
		}

		viewHolder.cardImage.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view) {
				ViewCardFullScreenFragment fullCardScreen = new ViewCardFullScreenFragment();
				Bundle args = new Bundle();
				args.putSerializable("OwnedCard", current);
				fullCardScreen.setArguments(args);

				//fullCardScreen.show(context.getSupportFragmentManager(), "ViewCardFullScreenFragment");


				context.getSupportFragmentManager().beginTransaction()
						.replace(R.id.viewCardsSummaryScreen, fullCardScreen)
						.addToBackStack(null)
						.commit();


			}
		});
	}

	public static class ItemViewHolder extends RecyclerView.ViewHolder {

		final TextView title;
		final TextView setName;
		final TextView cardQuantity;
		final ImageView cardImage;
		final TextView cardPrice;
		final TextView cardDate;
		final TextView rarity;

		public ItemViewHolder(@NonNull View view) {
			super(view);

			title = view.findViewById(R.id.cardTitle);
			setName = view.findViewById(R.id.cardSetName);
			cardQuantity = view.findViewById(R.id.cardQuantity);
			cardPrice = view.findViewById(R.id.cardPrice);
			cardDate = view.findViewById(R.id.cardDateBought);
			cardImage = view.findViewById(R.id.cardImage);
			rarity = view.findViewById(R.id.cardRarity);

		}

	}

}