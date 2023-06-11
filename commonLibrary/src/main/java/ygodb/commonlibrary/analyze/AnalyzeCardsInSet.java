package ygodb.commonlibrary.analyze;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ygodb.commonlibrary.bean.AnalyzeData;
import ygodb.commonlibrary.bean.CardSet;
import ygodb.commonlibrary.bean.GamePlayCard;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.bean.SetMetaData;
import ygodb.commonlibrary.connection.SQLiteConnection;

public class AnalyzeCardsInSet {

	public List<AnalyzeData> runFor(String setName, SQLiteConnection db) throws SQLException {
		HashMap<String, AnalyzeData> h = new HashMap<>();

		String[] sets = setName.split(";");

		for (String individualSet : sets) {
			addAnalyzeDataForSet(h, individualSet, db);
		}

		return new ArrayList<>(h.values());
	}

	public void addAnalyzeDataForSet(Map<String, AnalyzeData> h, String setName, SQLiteConnection db) throws SQLException {
		ArrayList<GamePlayCard> list = db.getDistinctCardNamesAndGamePlayCardUUIDsInSetByName(setName);
		boolean archetypeMode = false;

		if (list.isEmpty()) {
			ArrayList<SetMetaData> setNames = db.getSetMetaDataFromSetCode(setName.toUpperCase(Locale.ROOT));

			if (setNames == null || setNames.isEmpty() ) {

				list = db.getDistinctCardNamesAndIdsByArchetype(setName);
				archetypeMode = true;
				if (list.isEmpty()) {
					return;
				}
			}
			else {
				setName = setNames.get(0).setName;
				list = db.getDistinctCardNamesAndGamePlayCardUUIDsInSetByName(setName);
			}
		}

		ArrayList<SetMetaData> setMetaData = db.getSetMetaDataFromSetName(setName);

		for (GamePlayCard currentCardSet : list) {

			String currentCard = currentCardSet.cardName;
			String gamePlayCardUUID = currentCardSet.gamePlayCardUUID;
			int passcode = currentCardSet.passcode;

			ArrayList<OwnedCard> cardsList = db.getNumberOfOwnedCardsByGamePlayCardUUID(gamePlayCardUUID);

			ArrayList<CardSet> rarityList;
			if(!archetypeMode) {
				rarityList = db.getRaritiesOfCardInSetByGamePlayCardUUID(gamePlayCardUUID, setName);
			}
			else{
				rarityList = db.getRaritiesOfCardByGamePlayCardUUID(gamePlayCardUUID);
			}

			if (cardsList.isEmpty()) {

				AnalyzeData currentData = new AnalyzeData();

				if (currentCard == null) {
					currentData.cardName = "No cards found for id:" + gamePlayCardUUID;
					currentData.quantity = -1;
				} else {
					currentData.cardName = currentCard;
					currentData.quantity = 0;
				}

				if(!archetypeMode){
					for (CardSet rarity : rarityList) {
						currentData.setRarities.add(rarity.setRarity);

						if(rarity.setName.equalsIgnoreCase(setName)){
							currentData.mainSetCardSets.add(rarity);
						}

					}
					currentData.cardPriceAverage = currentData.getLowestPriceFromMainSet();
				}
				else{
					BigDecimal origSetPrice = new BigDecimal(Integer.MAX_VALUE);
					currentData.cardPriceAverage = origSetPrice;
					for (CardSet rarity : rarityList) {
						currentData.setName.add(rarity.setName);
						currentData.setRarities.add(rarity.setRarity);

						if(rarity.setPrice == null){
							rarity.setPrice = "0";
						}

						BigDecimal setPrice = new BigDecimal(rarity.setPrice);
						BigDecimal zero = new BigDecimal(0);

						if (!(zero.equals(setPrice)) && currentData.cardPriceAverage.compareTo(setPrice) > 0){
							currentData.cardPriceAverage = setPrice;
						}
					}
					if(origSetPrice.equals(currentData.cardPriceAverage)){
						currentData.cardPriceAverage = new BigDecimal(0);
					}
				}

				currentData.gamePlayCardUUID = gamePlayCardUUID;
				currentData.passcode = passcode;

				if(!archetypeMode) {
					currentData.setNumber.add(rarityList.get(0).setNumber);
					currentData.cardType = rarityList.get(0).cardType;
					currentData.setName.add(setName);
					currentData.mainSetName = setName;
					currentData.mainSetCode = setMetaData.get(0).setCode;
				}
				addToHashMap(h, currentData);
			}

			for (OwnedCard current : cardsList) {
				AnalyzeData currentData = new AnalyzeData();

				currentData.cardName = current.cardName;
				currentData.quantity = current.quantity;

				if(!archetypeMode) {
					for (CardSet rarity : rarityList) {
						currentData.setRarities.add(rarity.setRarity);

						if (rarity.setName.equalsIgnoreCase(setName)) {
							currentData.mainSetCardSets.add(rarity);
						}
					}
				}
				else{
					for (CardSet rarity : rarityList) {
						currentData.setName.add(rarity.setName);
						currentData.setRarities.add(rarity.setRarity);
					}
				}

				currentData.gamePlayCardUUID = gamePlayCardUUID;
				currentData.passcode = passcode;
				if(!archetypeMode) {
					currentData.setNumber.add(rarityList.get(0).setNumber);
					currentData.cardType = rarityList.get(0).cardType;
					currentData.mainSetName = setName;
					currentData.mainSetCode = setMetaData.get(0).setCode;
				}
				Collections.addAll(currentData.setName, current.setName.split(","));
				currentData.cardPriceAverage = new BigDecimal(current.priceBought);
				addToHashMap(h, currentData);
			}
		}
	}

	private void addToHashMap(Map<String, AnalyzeData> h, AnalyzeData s) {

		AnalyzeData existing = h.get(s.cardName);

		if (existing == null) {
			h.put(s.cardName, s);
		} else {
			existing.setName.addAll(s.setName);
			existing.setNumber.addAll(s.setNumber);
			existing.setRarities.addAll(s.setRarities);
		}

	}
}
