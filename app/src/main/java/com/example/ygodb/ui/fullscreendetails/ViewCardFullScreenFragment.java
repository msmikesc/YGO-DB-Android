package com.example.ygodb.ui.fullscreendetails;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.ygodb.databinding.FragmentCardfullscreenBinding;
import com.example.ygodb.util.AndroidUtil;
import ygodb.commonlibrary.bean.GamePlayCard;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.YGOLogger;

import java.io.InputStream;
import java.sql.SQLException;

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
		} catch (SQLException e) {
			YGOLogger.logException(e);
		}

		if(current != null) {

			binding.cardTitle.setText(current.getCardName());
			binding.cardPasscode.setText(String.valueOf(current.getPasscode()));
			binding.cardArchetype.setText(current.getArchetype());
			binding.cardAttack.setText(current.getAtk());
			binding.cardDefense.setText(current.getDef());
			binding.cardAttributeText.setText(current.getAttribute());
			binding.cardPendScale.setText(current.getScale());
			binding.cardSubtypeText.setText(current.getRace());
			binding.cardTypeText.setText(current.getCardType());

			String textBox = current.getDesc();

			if(!current.getCardType().contains("Normal")){
				textBox = insertNewLineAfterPeriod(textBox);
			}

			binding.cardTextBox.setText(textBox);

			String level = current.getLevel();
			String linkRating = current.getLinkVal();

			if(linkRating != null && !linkRating.isEmpty()){
				binding.cardLevelRankLinkRating.setText(linkRating);
			}
			else{
				binding.cardLevelRankLinkRating.setText(level);
			}

			try {
				// get input stream
				InputStream ims = AndroidUtil.getAppContext().getAssets().open("pics/" + current.getPasscode() + ".jpg");
				// load image as Drawable
				Drawable d = Drawable.createFromStream(ims, null);
				// set image to ImageView
				binding.cardImage.setImageDrawable(d);
			} catch (Exception ex) {
				binding.cardImage.setImageDrawable(null);
			}
		}


		return root;
	}

	public static String insertNewLineAfterPeriod(String paragraph) {
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