package com.example.ygodb.ui.sellcards;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ygodb.R;
import com.example.ygodb.model.ItemsListAdapter;
import com.example.ygodb.util.AndroidUtil;
import ygodb.commonlibrary.bean.Rarity;
import ygodb.commonlibrary.bean.SoldCard;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.Util;
import ygodb.commonlibrary.utility.YGOLogger;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;

public class SellCardToListAdapter extends ItemsListAdapter<SoldCard, SellCardToListAdapter.ItemViewHolder> {
	private final SellCardsViewModel sellCardsViewModel;
	private Drawable firstDrawableSmall;

	private Context context;

	public SellCardToListAdapter(List<SoldCard> ownedCards, SellCardsViewModel sellCardsViewModel) {
		super(ownedCards);
		this.sellCardsViewModel = sellCardsViewModel;

		try {
			InputStream firstInputStreamSmall = AndroidUtil.getAppContext().getAssets().open(Const.FIRST_ICON_PNG);
			firstDrawableSmall = Drawable.createFromStream(firstInputStreamSmall, null);
		} catch (Exception e) {
			YGOLogger.logException(e);
		}
	}

	@NonNull
	@Override
	public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_sellsinglecard, parent, false);

		context = parent.getContext();

		return new ItemViewHolder(view);
	}

	public void onPlusButtonClick(ItemViewHolder viewHolder, SoldCard current) {

		if (current.getSellQuantity() >= current.getQuantity()) {
			current.setSellQuantity(current.getQuantity());
		} else {
			current.setSellQuantity(current.getSellQuantity() + 1);
		}
		viewHolder.cardQuantity.setText(String.valueOf(current.getSellQuantity()));
	}

	public void onMinusButtonClick(SoldCard current) {
		current.setSellQuantity(current.getSellQuantity() - 1);

		if (current.getSellQuantity() < 1) {
			sellCardsViewModel.removeNewFromOwnedCard(current);
		}

		this.notifyDataSetChanged();
	}

	public void onUpdatePrice(CharSequence priceBox, SoldCard current, int position) {
		String newPrice = priceBox.toString();

		try {
			newPrice = Util.normalizePrice(newPrice);

			if (current.getPriceSold() != null && current.getPriceSold().equals(newPrice)) {
				return;
			}

			current.setPriceSold(newPrice);
		} catch (Exception e) {
			//price typed in is junk

		}
		try {
			notifyItemChanged(position);
		} catch (Exception e) {
			//View disappeared or something with attempting to update
		}
	}

	@Override
	public void onBindViewHolder(@NonNull ItemViewHolder viewHolder, int position) {

		SoldCard current = itemsList.get(position);

		ImageButton button = viewHolder.itemView.findViewById(R.id.plusButton);
		button.setOnClickListener(view -> onPlusButtonClick(viewHolder, current));

		ImageButton button2 = viewHolder.itemView.findViewById(R.id.minusButton);
		button2.setOnClickListener(view -> onMinusButtonClick(current));

		viewHolder.setNumber.setText(current.getSetNumber());
		viewHolder.setName.setText(current.getSetName());

		viewHolder.title.setText(current.getCardName());
		int textColor = AndroidUtil.getColorByColorVariant(current.getColorVariant());
		viewHolder.title.setTextColor(textColor);

		String setRarityText = AndroidUtil.getSetRarityDisplayWithColorText(current);
		viewHolder.cardRarity.setText(setRarityText);

		if (current.getPriceSold() != null) {
			double price = Double.parseDouble(current.getPriceSold());
			viewHolder.cardPrice.setText("$");
			viewHolder.cardPriceTextBox.setText(String.format(Locale.ROOT, "%.2f", price));
		} else {
			viewHolder.cardPrice.setText("$");
			viewHolder.cardPriceTextBox.setText(Const.ZERO_PRICE_STRING);
		}

		viewHolder.cardPriceTextBox.setOnEditorActionListener((v, actionId, event) -> {
			if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
				onUpdatePrice(v.getText(), current, viewHolder.getAdapterPosition());
			}
			return false;
		});

		viewHolder.cardPriceTextBox.setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus && v instanceof TextView textView) {
				onUpdatePrice(textView.getText(), current, viewHolder.getAdapterPosition());
			}
		});


		viewHolder.cardDate.setText(current.getDateBought());
		viewHolder.cardQuantity.setText(String.valueOf(current.getSellQuantity()));

		if (current.getEditionPrinting().contains(Const.CARD_PRINTING_CONTAINS_FIRST)) {
			// set image to ImageView
			viewHolder.firstEdition.setImageDrawable(firstDrawableSmall);
		} else {
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

		viewHolder.cardImage.setOnClickListener(view -> {
			Bundle args = new Bundle();
			args.putString(Const.GAME_PLAY_CARD_UUID, current.getGamePlayCardUUID());
			Navigation.findNavController(view).navigate(R.id.nav_ViewCardFullScreenFragment, args);
		});

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
		final EditText cardPriceTextBox;

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
			cardPriceTextBox = view.findViewById(R.id.cardPriceTextBox);

		}

	}

}
