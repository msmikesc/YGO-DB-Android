package com.example.ygodb.ui.sellcards;

import com.example.ygodb.model.addorsell.AddOrSellViewModel;
import com.example.ygodb.util.AndroidUtil;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.bean.SoldCard;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.Util;

import java.util.Date;

public class SellCardsViewModel extends AddOrSellViewModel<SoldCard> {

	public SellCardsViewModel() {
		super();
	}

	protected String getKeyForOwnedCard(OwnedCard input) {
		return input.getUuid();
	}

	public void saveToDB() {
		for (SoldCard current : cardsList) {

			AndroidUtil.getDBInstance().sellCards(current, current.getSellQuantity(), current.getPriceSold());

		}
		keyToPosition.clear();
		cardsList.clear();
	}

	public void addNewFromOwnedCard(OwnedCard current, int quantity) {
		//quantity ignored, only one at a time allowed
		addNewFromOwnedCard(current);
	}

	public void addNewFromOwnedCard(OwnedCard current) {

		if (current.getSetNumber() == null || current.getSetRarity() == null || current.getSetName() == null ||
				current.getCardName() == null) {
			return;
		}

		String key = getKeyForOwnedCard(current);

		Integer position = keyToPosition.get(key);

		SoldCard sellingCard = null;

		if (position != null) {
			sellingCard = cardsList.get(position);
		}
		if (sellingCard != null) {

			if (sellingCard.getQuantity() > sellingCard.getSellQuantity()) {
				sellingCard.setSellQuantity(sellingCard.getSellQuantity() + 1);
			}
		} else {
			sellingCard = new SoldCard();
			keyToPosition.put(key, cardsList.size());
			cardsList.add(sellingCard);
			sellingCard.setCardName(current.getCardName());
			sellingCard.setDateBought(current.getDateBought());
			sellingCard.setDateSold(sdf.format(new Date()));
			sellingCard.setGamePlayCardUUID(current.getGamePlayCardUUID());
			sellingCard.setSetRarity(current.getSetRarity());
			sellingCard.setSetName(current.getSetName());
			sellingCard.setQuantity(current.getQuantity());
			sellingCard.setSellQuantity(1);
			sellingCard.setRarityUnsure(Const.RARITY_UNSURE_FALSE);
			sellingCard.setSetPrefix(current.getSetPrefix());
			sellingCard.setSetNumber(current.getSetNumber());
			sellingCard.setColorVariant(current.getColorVariant());
			sellingCard.setUuid(current.getUuid());
			sellingCard.setPasscode(current.getPasscode());
			sellingCard.setAltArtPasscode(current.getAltArtPasscode());

			sellingCard.setPriceBought(current.getPriceBought());
			sellingCard.setEditionPrinting(current.getEditionPrinting());

			sellingCard.setAnalyzeResultsCardSets(current.getAnalyzeResultsCardSets());

			sellingCard.setPriceSold(Util.getAPIPriceFromRarity(sellingCard, AndroidUtil.getDBInstance()));

			sellingCard.setCreationDate(current.getCreationDate());

			if (current.getCondition() == null || current.getCondition().equals("")) {
				sellingCard.setCondition("NearMint");
			} else {
				sellingCard.setCondition(current.getCondition());
			}
		}
	}

}