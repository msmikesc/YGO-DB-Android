package com.example.ygodb.ui.addcards;

import com.example.ygodb.model.addorsell.AddOrSellViewModel;
import com.example.ygodb.util.AndroidUtil;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.Util;

import java.sql.SQLException;
import java.util.Date;

public class AddCardsViewModel extends AddOrSellViewModel<OwnedCard> {

	public AddCardsViewModel() {
		super();
	}

	protected String getKeyForOwnedCard(OwnedCard input) {

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
			newCard.setAltArtPasscode(current.getAltArtPasscode());


			if (current.getEditionPrinting() == null || current.getEditionPrinting().equals("")) {
				//assume ots unlimited, everything else 1st
				if (newCard.getSetName().contains("OTS")) {
					newCard.setEditionPrinting(Const.CARD_PRINTING_UNLIMITED);
				}
				else if(newCard.getSetName().contains("Lost Art")){
					newCard.setEditionPrinting(Const.CARD_PRINTING_LIMITED);
				}
				else {
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

}