package com.example.ygodb.ui.addcards;

import androidx.lifecycle.ViewModel;

import com.example.ygodb.abs.AndroidUtil;

import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.Util;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddCardsViewModel extends ViewModel {

	private final HashMap<String, Integer> keyToPosition;
	private final ArrayList<OwnedCard> cardsList;

	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

	public AddCardsViewModel() {
		cardsList = new ArrayList<>();
		keyToPosition = new HashMap<>();
	}

	private String getKeyForOwnedCard(OwnedCard input) {

		return input.getSetNumber() + input.getSetRarity() + input.getColorVariant();
	}


	public void saveToDB() throws SQLException {
		for (OwnedCard current : cardsList) {

			if (current.getSetPrefix() == null || current.getSetPrefix().equals("")) {
				current.setSetPrefix(Util.getPrefixFromSetNumber(current.getSetNumber()));
			}

			if (current.getPriceBought() == null) {
				current.setPriceBought(Const.ZERO_PRICE_STRING);
			}

			OwnedCard existingRecord = AndroidUtil.getDBInstance().getExistingOwnedCardByObject(current);

			if (existingRecord != null) {
				existingRecord.setQuantity(existingRecord.getQuantity() + current.getQuantity());
				current = existingRecord;
			}

			AndroidUtil.getDBInstance().insertOrUpdateOwnedCardByUUID(current);

		}
		keyToPosition.clear();
		cardsList.clear();
	}

	public void invertAllEditions() {
		for (OwnedCard o : cardsList) {
			if (o.getEditionPrinting().equals(Const.CARD_PRINTING_FIRST_EDITION)) {
				o.setEditionPrinting(Const.CARD_PRINTING_UNLIMITED);
			} else {
				o.setEditionPrinting(Const.CARD_PRINTING_FIRST_EDITION);
			}
		}
	}

	public List<OwnedCard> getCardsList() {
		return cardsList;
	}

	public void addNewFromOwnedCard(OwnedCard current, int quantity) {

		if (current.getSetNumber() == null || current.getSetRarity() == null || current.getSetName() == null ||
				current.getCardName() == null) {
			return;
		}

		String key = getKeyForOwnedCard(current);

		Integer position = keyToPosition.get(key);

		OwnedCard newCard = null;

		if (position != null) {
			newCard = cardsList.get(position);
		}
		if (newCard != null) {
			newCard.setQuantity(newCard.getQuantity() + quantity);
		} else {
			newCard = new OwnedCard();
			keyToPosition.put(key, cardsList.size());
			cardsList.add(newCard);
			newCard.setCardName(current.getCardName());
			newCard.setDateBought(sdf.format(new Date()));
			newCard.setGamePlayCardUUID(current.getGamePlayCardUUID());
			newCard.setSetRarity(current.getSetRarity());
			newCard.setQuantity(quantity);
			newCard.setRarityUnsure(0);
			newCard.setSetPrefix(current.getSetPrefix());
			newCard.setFolderName(Const.FOLDER_UNSYNCED);
			newCard.setSetNumber(current.getSetNumber());
			newCard.setColorVariant(current.getColorVariant());
			newCard.setAnalyzeResultsCardSets(current.getAnalyzeResultsCardSets());
			newCard.setPasscode(current.getPasscode());
			newCard.setSetName(current.getSetName());


			if (current.getEditionPrinting() == null || current.getEditionPrinting().equals("")) {
				//assume ots unlimited, everything else 1st
				if (newCard.getSetName().contains("OTS")) {
					newCard.setEditionPrinting(Const.CARD_PRINTING_UNLIMITED);
				} else {
					newCard.setEditionPrinting(Const.CARD_PRINTING_FIRST_EDITION);
				}

			} else {
				newCard.setEditionPrinting(current.getEditionPrinting());
			}

			newCard.setPriceBought(Util.getAPIPriceFromRarity(newCard, AndroidUtil.getDBInstance()));

			if (current.getCondition() == null || current.getCondition().equals("")) {
				newCard.setCondition("NearMint");
			} else {
				newCard.setCondition(current.getCondition());
			}
		}
	}

	public void setAllPricesEstimate() {
		for (OwnedCard current : cardsList) {
			current.setPriceBought(Util.getEstimatePriceFromRarity(current.getSetRarity()));
		}
	}

	public void setAllPricesAPI() {
		for (OwnedCard current : cardsList) {
			current.setPriceBought(Util.getAPIPriceFromRarity(current, AndroidUtil.getDBInstance()));
		}
	}

	public void setAllPricesZero() {
		for (OwnedCard current : cardsList) {
			current.setPriceBought(Const.ZERO_PRICE_STRING);
		}
	}

	public void removeNewFromOwnedCard(OwnedCard current) {

		String key = getKeyForOwnedCard(current);

		Integer position = keyToPosition.get(key);
		OwnedCard newCard = null;

		if (position != null) {
			newCard = cardsList.get(position);
		}
		if (newCard == null) {
			return;
		}

		for (Map.Entry<String, Integer> testEntry : keyToPosition.entrySet()) {

			String testKey = testEntry.getKey();

			Integer testPos = testEntry.getValue();

			if (testPos > position) {
				testPos--;
				keyToPosition.put(testKey, testPos);
			}
		}

		keyToPosition.remove(key);
		cardsList.remove(position.intValue());

	}

}