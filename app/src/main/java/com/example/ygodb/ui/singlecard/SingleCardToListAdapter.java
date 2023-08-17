package com.example.ygodb.ui.singlecard;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ygodb.R;
import com.example.ygodb.abs.AndroidUtil;
import com.example.ygodb.ui.addcards.AddCardsViewModel;
import com.example.ygodb.ui.sellcards.SellCardsViewModel;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.bean.Rarity;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.YGOLogger;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;

public class SingleCardToListAdapter extends RecyclerView.Adapter<SingleCardToListAdapter.ItemViewHolder> {
	private List<OwnedCard> ownedCards;

	private final AddCardsViewModel addCardsViewModel;
	private final SellCardsViewModel sellCardsViewModel;
	private final boolean isManyPlusButtons;
	private Drawable firstDrawableSmall;
	private Drawable limitedDrawableSmall;

	private Context context;

	public SingleCardToListAdapter(List<OwnedCard> ownedCards, AddCardsViewModel addCardsViewModel, SellCardsViewModel sellCardsViewModel,
			boolean isManyPlusButtons) {
		this.ownedCards = ownedCards;
		this.addCardsViewModel = addCardsViewModel;
		this.sellCardsViewModel = sellCardsViewModel;
		this.isManyPlusButtons = isManyPlusButtons;

		try {
			InputStream firstInputStreamSmall = AndroidUtil.getAppContext().getAssets().open(Const.FIRST_ICON_PNG);
			firstDrawableSmall = Drawable.createFromStream(firstInputStreamSmall, null);

			InputStream limitedInputStreamSmall = AndroidUtil.getAppContext().getAssets().open(Const.LIMITED_ICON_PNG);
			limitedDrawableSmall = Drawable.createFromStream(limitedInputStreamSmall, null);
		} catch (Exception e) {
			YGOLogger.logException(e);
		}
	}

	public void setOwnedCards(List<OwnedCard> ownedCards) {
		this.ownedCards = ownedCards;
	}

	@NonNull
	@Override
	public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_singlecard, parent, false);

		context = parent.getContext();

		return new ItemViewHolder(view);
	}

	public void onPlusButtonClick(OwnedCard current, int quantity) {
		if (addCardsViewModel != null) {
			addCardsViewModel.addNewFromOwnedCard(current, quantity);
		}
	}

	public void onSellButtonClick(OwnedCard current) {
		if (sellCardsViewModel != null) {
			sellCardsViewModel.addNewFromOwnedCard(current);
		}
	}

	@Override
	public void onBindViewHolder(@NonNull ItemViewHolder viewHolder, int position) {

		OwnedCard current = ownedCards.get(position);

		LinearLayout buttonContainer = viewHolder.itemView.findViewById(R.id.incrementQuantityButtonContainer);

		if (addCardsViewModel != null) {
			if (isManyPlusButtons) {
				viewHolder.plusButton.setVisibility(View.GONE);
				buttonContainer.setVisibility(View.VISIBLE);
				LayoutInflater inflater = LayoutInflater.from(buttonContainer.getContext());

				buttonContainer.removeAllViews();

				for (int i = 1; i <= 6; i++) {
					FrameLayout buttonLayout =
							(FrameLayout) inflater.inflate(R.layout.labeled_increment_quantity_button, buttonContainer, false);
					ImageButton button = buttonLayout.findViewById(R.id.incrementButton);
					TextView incrementLabel = buttonLayout.findViewById(R.id.incrementLabel);

					final int increment = i; // Capture the current value of 'i' for each button

					incrementLabel.setText(String.valueOf(increment));

					button.setOnClickListener(v -> onPlusButtonClick(current, increment));

					buttonContainer.addView(buttonLayout);
				}
			} else {
				viewHolder.plusButton.setVisibility(View.VISIBLE);
				buttonContainer.setVisibility(View.GONE);
				ImageButton singleButton = viewHolder.itemView.findViewById(R.id.plusButton);
				singleButton.setOnClickListener(view -> onPlusButtonClick(current, 1));
			}
		} else {
			viewHolder.plusButton.setVisibility(View.GONE);
			buttonContainer.setVisibility(View.GONE);
		}

		if (sellCardsViewModel != null) {
			ImageButton sellButton = viewHolder.itemView.findViewById(R.id.sellButton);
			sellButton.setOnClickListener(view -> onSellButtonClick(current));
		}

		viewHolder.title.setText(current.getCardName());
		int textColor = AndroidUtil.getColorByColorVariant(current.getColorVariant());
		viewHolder.title.setTextColor(textColor);

		String setRarityText = AndroidUtil.getSetRarityDisplayWithColorText(current);
		viewHolder.cardRarity.setText(setRarityText);

		viewHolder.setNumber.setText(current.getSetNumber());
		viewHolder.setName.setMaxLines(2);

		if (sellCardsViewModel == null) {
			viewHolder.sellButton.setVisibility(View.GONE);
		} else {
			viewHolder.sellButton.setVisibility(View.VISIBLE);
		}

		viewHolder.setName.setText(current.getSetName());

		if (current.getPriceBought() != null) {
			double price = Double.parseDouble(current.getPriceBought());
			viewHolder.cardPrice.setText("$" + String.format(Locale.ROOT, "%.2f", price));
		}
		viewHolder.cardDate.setText(current.getDateBought());

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

			// Apply shader for holofoil pattern
			if (Rarity.androidShinyRarities.contains(current.getSetRarity())) {
				d = AndroidUtil.applyShader(context, R.drawable.holofoil, d, 161, 236);
			} else if ("Ghost Rare".equals(current.getSetRarity())) {
				AndroidUtil.convertToGrayscale(d);
			}

			viewHolder.cardImage.setImageDrawable(d);
		} catch (Exception ex) {
			viewHolder.cardImage.setImageDrawable(null);
		}

	}

	@Override
	public int getItemCount() {
		return ownedCards.size();
	}

	public static class ItemViewHolder extends RecyclerView.ViewHolder {

		final TextView title;
		final TextView setNumber;
		final TextView setName;
		final TextView cardRarity;
		final TextView cardPrice;
		final TextView cardDate;
		final TextView cardQuantity;
		final ImageView cardImage;
		final ImageView firstEdition;
		final ImageButton plusButton;
		final ImageButton sellButton;
		final LinearLayout incrementQuantityButtonContainer;

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
