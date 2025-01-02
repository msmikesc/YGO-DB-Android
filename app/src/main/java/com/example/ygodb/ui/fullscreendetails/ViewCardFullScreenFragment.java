package com.example.ygodb.ui.fullscreendetails;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.ygodb.R;
import com.example.ygodb.databinding.FragmentCardfullscreenBinding;
import com.example.ygodb.util.AndroidUtil;
import ygodb.commonlibrary.bean.GamePlayCard;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.Util;
import ygodb.commonlibrary.utility.YGOLogger;

import java.io.InputStream;

public class ViewCardFullScreenFragment extends Fragment {

	private FragmentCardfullscreenBinding binding;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		binding = FragmentCardfullscreenBinding.inflate(inflater, container, false);
		View root = binding.getRoot();

		Bundle input = getArguments();

		GamePlayCard current = null;

		try {
			String gameplayCardUUID = input.getString(Const.GAME_PLAY_CARD_UUID);
			current = AndroidUtil.getDBInstance().getGamePlayCardByUUID(gameplayCardUUID);
		} catch (Exception e) {
			YGOLogger.logException(e);
		}

		if(current != null) {

			int passcode = current.getPasscode();

			binding.cardTitle.setText(current.getCardName());
			binding.cardPasscode.setText(String.valueOf(passcode));
			binding.cardArchetype.setText(current.getArchetype());
			binding.cardAttributeText.setText(current.getAttribute());
			binding.cardPendScaleText.setText(current.getScale());
			binding.cardSubtypeText.setText(current.getRace());
			binding.cardTypeText.setText(current.getCardType());

			if(current.getAtk() != null) {
				binding.cardAttack.setText("ATK/"+current.getAtk());
			}
			else{
				binding.cardAttack.setVisibility(View.GONE);
			}
			if(current.getDef()!= null) {
				binding.cardDefense.setText("DEF/"+current.getDef());
			}
			else{
				binding.cardDefense.setVisibility(View.GONE);
			}

			String cardAttributeIcon = null;
			String cardType = (current.getCardType() != null) ? current.getCardType() : "";
			switch (cardType) {
				case "Spell Card" -> {
					cardAttributeIcon = "images/SPELL.png";
					renderCardSpellTrapSubtypeIcon(current);
					binding.cardAttribute.setVisibility(View.GONE);
				}
				case "Trap Card" -> {
					cardAttributeIcon = "images/TRAP.png";
					renderCardSpellTrapSubtypeIcon(current);
					binding.cardAttribute.setVisibility(View.GONE);
				}
				case "Skill Card" -> {
					binding.cardAttribute.setVisibility(View.GONE);
					binding.cardIcon.setVisibility(View.GONE);
					binding.cardLevelRankLinkRating.setVisibility(View.GONE);
				}
				default -> {
					cardAttributeIcon = "images/" + current.getAttribute() + ".png";
					renderCardSubtypeIcon(current);
					try {
						InputStream ims = AndroidUtil.getAppContext().getAssets().open(cardAttributeIcon);
						Drawable d = Drawable.createFromStream(ims, null);
						binding.cardAttributeIcon.setImageDrawable(d);
					} catch (Exception ex) {
						binding.cardAttributeIcon.setImageDrawable(null);
					}
				}
			}

			try {
				InputStream ims = AndroidUtil.getAppContext().getAssets().open(cardAttributeIcon);
				Drawable d = Drawable.createFromStream(ims, null);
				binding.cardIcon.setImageDrawable(d);
			} catch (Exception ex) {
				binding.cardIcon.setImageDrawable(null);
			}

			String textBox = current.getDesc();

			if(!cardType.contains("Normal")){
				textBox = insertNewLineAfterPeriod(textBox);
			}

			binding.cardTextBox.setText(textBox);

			String level = current.getLevel();
			String linkRating = current.getLinkVal();

			String levelIconResource = null;

			if(linkRating != null && !linkRating.isEmpty()){
				binding.cardLevelRankLinkRatingText.setText(linkRating);
				levelIconResource = "images/Link_arrows.webp";
			}
			else if(level != null && !level.isEmpty()){
				binding.cardLevelRankLinkRatingText.setText(level);
				if(cardType.contains("XYZ")){
					levelIconResource = "images/Rank.png";
				}
				else{
					levelIconResource = "images/Star.png";
				}
			}
			else{
				binding.cardLevelRankLinkRating.setVisibility(View.GONE);
			}

			if(levelIconResource != null) {
				try {
					InputStream ims = AndroidUtil.getAppContext().getAssets().open(levelIconResource);
					Drawable d = Drawable.createFromStream(ims, null);
					binding.cardLevelRankLinkRatingIcon.setImageDrawable(d);
				} catch (Exception ex) {
					binding.cardLevelRankLinkRatingIcon.setImageDrawable(null);
				}
			}

			if(current.getScale() != null){
				try {
					InputStream ims = AndroidUtil.getAppContext().getAssets().open("images/Pendulum_Scales.webp");
					Drawable d = Drawable.createFromStream(ims, null);
					binding.cardPendScaleIcon.setImageDrawable(d);
				} catch (Exception ex) {
					binding.cardPendScaleIcon.setImageDrawable(null);
				}
			}

			try {
				InputStream ims = AndroidUtil.getAppContext().getAssets().open("pics/" + passcode + ".jpg");
				Drawable d = Drawable.createFromStream(ims, null);
				binding.cardImage.setImageDrawable(d);
			} catch (Exception ex) {
				binding.cardImage.setImageDrawable(null);
			}

			binding.cardImage.setOnClickListener(view -> {
				Bundle args = new Bundle();
				args.putInt(Const.PASSCODE, Util.checkForTranslatedYgoProImagePasscode(passcode));
				Navigation.findNavController(view).navigate(R.id.nav_ViewCardImageFullScreenFragment, args);
			});

		}


		return root;
	}

	private void renderCardSubtypeIcon(GamePlayCard current) {
		if( current.getRace() != null) {
			try {
				String subtypeText = current.getRace().replace(' ', '-');
				InputStream ims = AndroidUtil.getAppContext().getAssets().open("images/"+subtypeText+"-MD.webp");
				Drawable d = Drawable.createFromStream(ims, null);
				binding.cardSubTypeIcon.setImageDrawable(d);
			} catch (Exception ex) {
				binding.cardSubTypeIcon.setImageDrawable(null);
			}
		}
	}

	private void renderCardSpellTrapSubtypeIcon(GamePlayCard current) {
		if( current.getRace() != null) {
			try {
				String subtypeText = current.getRace();
				InputStream ims = AndroidUtil.getAppContext().getAssets().open("images/"+subtypeText+".png");
				Drawable d = Drawable.createFromStream(ims, null);
				binding.cardSubTypeIcon.setImageDrawable(d);
			} catch (Exception ex) {
				binding.cardSubTypeIcon.setImageDrawable(null);
			}
		}
	}

	public static String insertNewLineAfterPeriod(String paragraph) {

		if(paragraph == null){
			return "";
		}

		StringBuilder result = new StringBuilder();
		int i = 0;
		while (i < paragraph.length()) {
			char currentChar = paragraph.charAt(i);
			result.append(currentChar);
			if (currentChar == '.' && i + 1 < paragraph.length() && paragraph.charAt(i + 1) == ' ' &&
					(i + 2 < paragraph.length() && paragraph.charAt(i + 2) != '(') ){
				result.append("\n\n");
				i++; // Skipping the white space
			}
			if (currentChar == '\n' && i + 1 < paragraph.length()) {
				result.append("\n");
			}
			i++;
		}
		return result.toString();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}