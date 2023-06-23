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
				setName = setNames.get(0).getSetName();
				list = db.getDistinctCardNamesAndGamePlayCardUUIDsInSetByName(setName);
			}
		}

		ArrayList<SetMetaData> setMetaData = db.getSetMetaDataFromSetName(setName);

		for (GamePlayCard currentCardSet : list) {

			String currentCard = currentCardSet.getCardName();
			String gamePlayCardUUID = currentCardSet.getGamePlayCardUUID();
			int passcode = currentCardSet.getPasscode();

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
					currentData.setCardName("No cards found for id:" + gamePlayCardUUID);
					currentData.setQuantity(-1);
				} else {
					currentData.setCardName(currentCard);
					currentData.setQuantity(0);
				}

				if(!archetypeMode){
					for (CardSet rarity : rarityList) {
						currentData.getSetRarities().add(rarity.getSetRarity());

						if(rarity.getSetName().equalsIgnoreCase(setName)){
							currentData.getMainSetCardSets().add(rarity);
						}

					}
					currentData.setCardPriceAverage(currentData.getLowestPriceFromMainSet());
				}
				else{
					BigDecimal origSetPrice = new BigDecimal(Integer.MAX_VALUE);
					currentData.setCardPriceAverage(origSetPrice);
					for (CardSet rarity : rarityList) {
						currentData.getSetName().add(rarity.getSetName());
						currentData.getSetRarities().add(rarity.getSetRarity());

						if(rarity.getSetPrice() == null){
							rarity.setSetPrice("0");
						}

						BigDecimal setPrice = new BigDecimal(rarity.getSetPrice());
						BigDecimal zero = new BigDecimal(0);

						if (!(zero.equals(setPrice)) && currentData.getCardPriceAverage().compareTo(setPrice) > 0){
							currentData.setCardPriceAverage(setPrice);
						}
					}
					if(origSetPrice.equals(currentData.getCardPriceAverage())){
						currentData.setCardPriceAverage(new BigDecimal(0));
					}
				}

				currentData.setGamePlayCardUUID(gamePlayCardUUID);
				currentData.setPasscode(passcode);

				if(!archetypeMode) {
					currentData.getSetNumber().add(rarityList.get(0).getSetNumber());
					currentData.setCardType(rarityList.get(0).getCardType());
					currentData.getSetName().add(setName);
					currentData.setMainSetName(setName);
					currentData.setMainSetCode(setMetaData.get(0).getSetCode());
				}
				addToHashMap(h, currentData);
			}

			for (OwnedCard current : cardsList) {
				AnalyzeData currentData = new AnalyzeData();

				currentData.setCardName(current.getCardName());
				currentData.setQuantity(current.getQuantity());

				if(!archetypeMode) {
					for (CardSet rarity : rarityList) {
						currentData.getSetRarities().add(rarity.getSetRarity());

						if (rarity.getSetName().equalsIgnoreCase(setName)) {
							currentData.getMainSetCardSets().add(rarity);
						}
					}
				}
				else{
					for (CardSet rarity : rarityList) {
						currentData.getSetName().add(rarity.getSetName());
						currentData.getSetRarities().add(rarity.getSetRarity());
					}
				}

				currentData.setGamePlayCardUUID(gamePlayCardUUID);
				currentData.setPasscode(passcode);
				if(!archetypeMode) {
					currentData.getSetNumber().add(rarityList.get(0).getSetNumber());
					currentData.setCardType(rarityList.get(0).getCardType());
					currentData.setMainSetName(setName);
					currentData.setMainSetCode(setMetaData.get(0).getSetCode());
				}
				Collections.addAll(currentData.getSetName(), current.getSetName().split(","));
				currentData.setCardPriceAverage(new BigDecimal(current.getPriceBought()));
				addToHashMap(h, currentData);
			}
		}
	}

	private void addToHashMap(Map<String, AnalyzeData> h, AnalyzeData s) {

		AnalyzeData existing = h.get(s.getCardName());

		if (existing == null) {
			h.put(s.getCardName(), s);
		} else {
			existing.getSetName().addAll(s.getSetName());
			existing.getSetNumber().addAll(s.getSetNumber());
			existing.getSetRarities().addAll(s.getSetRarities());
		}

	}
}
