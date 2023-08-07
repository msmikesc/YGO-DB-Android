package com.example.ygodb.ui.viewSoldCards;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ygodb.R;
import com.example.ygodb.abs.AndroidUtil;
import ygodb.commonlibrary.bean.Rarity;
import ygodb.commonlibrary.bean.SoldCard;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.YGOLogger;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;

public class SoldCardToListAdapter extends RecyclerView.Adapter<SoldCardToListAdapter.ItemViewHolder> {
	private List<SoldCard> soldCards;
	private Drawable firstDrawableSmall;
	private Drawable limitedDrawableSmall;

	private Context context;

	public SoldCardToListAdapter(List<SoldCard> soldCards) {
		this.soldCards = soldCards;

		try {
			InputStream firstInputStreamSmall = AndroidUtil.getAppContext().getAssets().open(Const.FIRST_ICON_PNG);
			firstDrawableSmall = Drawable.createFromStream(firstInputStreamSmall, null);

			InputStream limitedInputStreamSmall = AndroidUtil.getAppContext().getAssets().open(Const.LIMITED_ICON_PNG);
			limitedDrawableSmall = Drawable.createFromStream(limitedInputStreamSmall, null);
		} catch (Exception e) {
			YGOLogger.logException(e);
		}
	}

	public void setSoldCards(List<SoldCard> soldCards) {
		this.soldCards = soldCards;
	}

	@NonNull
	@Override
	public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_singlecard, parent, false);

		context = parent.getContext();

		return new ItemViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ItemViewHolder viewHolder, int position) {

		SoldCard current = soldCards.get(position);

		LinearLayout buttonContainer = viewHolder.itemView.findViewById(R.id.incrementQuantityButtonContainer);


		viewHolder.plusButton.setVisibility(View.GONE);
		viewHolder.sellButton.setVisibility(View.GONE);
		buttonContainer.setVisibility(View.GONE);

		viewHolder.title.setText(current.getCardName());
		int textColor = AndroidUtil.getColorByColorVariant(current.getColorVariant());
		viewHolder.title.setTextColor(textColor);

		String setRarityText = AndroidUtil.getSetRarityDisplayWithColorText(current);
		viewHolder.cardRarity.setText(setRarityText);

		viewHolder.setNumber.setText(current.getSetNumber());
		viewHolder.setName.setMaxLines(2);

		viewHolder.setName.setText(current.getSetName());

		if (current.getPriceSold() != null) {
			double price = Double.parseDouble(current.getPriceSold());
			viewHolder.cardPrice.setText("$" + String.format(Locale.ROOT, "%.2f", price));
		}
		viewHolder.cardDate.setText(current.getDateSold());

		viewHolder.cardQuantity.setText(String.valueOf(current.getQuantity()));

		try {
			if (current.getEditionPrinting().contains(Const.CARD_PRINTING_CONTAINS_FIRST)) {
				viewHolder.firstEdition.setImageDrawable(firstDrawableSmall);
			} else if (current.getEditionPrinting().equalsIgnoreCase(Const.CARD_PRINTING_LIMITED)) {
				viewHolder.firstEdition.setImageDrawable(limitedDrawableSmall);
			} else {
				viewHolder.firstEdition.setImageDrawable(null);
			}
		} catch (Exception ex) {
			viewHolder.firstEdition.setImageDrawable(null);
		}

		try {
			// get input stream
			InputStream ims = AndroidUtil.getAppContext().getAssets().open("pics/" + current.getPasscode() + ".jpg");
			// load image as Drawable
			Drawable d = Drawable.createFromStream(ims, null);
			// set image to ImageView

			// Apply shader for holofoil pattern
			if (Rarity.androidShinyRarities.contains(current.getSetRarity())) {
				d = AndroidUtil.applyShader(context, R.drawable.holofoil, d, 161, 236);
			}
			else if("Ghost Rare".equals(current.getSetRarity())){
				d = AndroidUtil.convertToGrayscale(d);
			}

			viewHolder.cardImage.setImageDrawable(d);
		} catch (Exception ex) {
			viewHolder.cardImage.setImageDrawable(null);
		}

	}

	@Override
	public int getItemCount() {
		return soldCards.size();
	}

	public static class ItemViewHolder extends RecyclerView.ViewHolder {

		TextView title;
		TextView setNumber;
		TextView setName;
		TextView cardRarity;
		TextView cardPrice;
		TextView cardDate;
		TextView cardQuantity;
		ImageView cardImage;
		ImageView firstEdition;
		ImageButton plusButton;
		ImageButton sellButton;
		LinearLayout incrementQuantityButtonContainer;

		public ItemViewHolder(@NonNull View view) {
			super(view);

			title = view.findViewById(R.id.cardTitle);
			setNumber = view.findViewById(R.id.cardSetNumber);
			setName = view.findViewById(R.id.cardSetName);
			cardRarity = view.findViewById(R.id.cardRarity);
			cardPrice = view.findViewById(R.id.cardPrice);
			cardDate = view.findViewById(R.id.cardDateBought);
			cardQuantity = view.findViewById(R.id.cardQuantity);
			cardImage = view.findViewById(R.id.cardImage);
			firstEdition = view.findViewById(R.id.firststEditionIcon);
			plusButton = view.findViewById(R.id.plusButton);
			sellButton = view.findViewById(R.id.sellButton);
			incrementQuantityButtonContainer = view.findViewById(R.id.incrementQuantityButtonContainer);

		}

	}

}
