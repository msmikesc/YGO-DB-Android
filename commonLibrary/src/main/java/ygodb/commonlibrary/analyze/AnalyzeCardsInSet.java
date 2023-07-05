package ygodb.commonlibrary.analyze;

import ygodb.commonlibrary.bean.AnalyzeData;
import ygodb.commonlibrary.bean.CardSet;
import ygodb.commonlibrary.bean.GamePlayCard;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.bean.SetMetaData;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.utility.YGOLogger;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AnalyzeCardsInSet {

	public List<AnalyzeData> runFor(String setName, SQLiteConnection db) throws SQLException {
		HashMap<String, AnalyzeData> analyzeDataHashMap = new HashMap<>();

		String[] sets = setName.split(";");

		for (String individualSet : sets) {
			addInitialAnalyzeDataForSet(analyzeDataHashMap, individualSet, db);
		}

		for(AnalyzeData analyzeData: analyzeDataHashMap.values()){
			updateAnalyzeDataValues(analyzeData, db);
		}

		return new ArrayList<>(analyzeDataHashMap.values());
	}

	public void addInitialAnalyzeDataForSet(Map<String, AnalyzeData> analyzeDataHashMap, String requestedSetName, SQLiteConnection db) throws SQLException {
		ArrayList<GamePlayCard> list = db.getDistinctGamePlayCardsInSetByName(requestedSetName);
		boolean archetypeMode = false;

		if (list.isEmpty()) {
			ArrayList<SetMetaData> setNames = db.getSetMetaDataFromSetCode(requestedSetName.toUpperCase(Locale.ROOT));

			if (setNames == null || setNames.isEmpty() ) {

				list = db.getDistinctGamePlayCardsByArchetype(requestedSetName);
				archetypeMode = true;
				if (list.isEmpty()) {
					return;
				}
			}
			else {
				requestedSetName = setNames.get(0).getSetName();
				list = db.getDistinctGamePlayCardsInSetByName(requestedSetName);
			}
		}

		for (GamePlayCard currentGamePlayCard : list) {

			String gamePlayCardUUID = currentGamePlayCard.getGamePlayCardUUID();

			ArrayList<CardSet> rarityList;
			if(!archetypeMode) {
				rarityList = db.getRaritiesOfCardInSetByGamePlayCardUUID(gamePlayCardUUID, requestedSetName);
			}
			else{
				rarityList = db.getRaritiesOfCardByGamePlayCardUUID(gamePlayCardUUID);
			}

			AnalyzeData analyzeData= new AnalyzeData();
			analyzeData.setCardName(currentGamePlayCard.getCardName());
			analyzeData.setGamePlayCardUUID(gamePlayCardUUID);
			analyzeData.setPasscode(currentGamePlayCard.getPasscode());
			analyzeData.setGamePlayCard(currentGamePlayCard);
			analyzeData.setCardSets(rarityList);
			analyzeData.setCardType(currentGamePlayCard.getCardType());

			addInitialDataToHashMap(analyzeDataHashMap, analyzeData);
		}
	}

	private void addInitialDataToHashMap(Map<String, AnalyzeData> h, AnalyzeData s) {

		AnalyzeData existing = h.get(s.getGamePlayCardUUID());

		if (existing == null) {
			h.put(s.getGamePlayCardUUID(), s);
		} else {
			existing.getCardSets().addAll(s.getCardSets());
		}

	}

	private void updateAnalyzeDataValues(AnalyzeData analyzeData, SQLiteConnection db) throws SQLException {

		List<CardSet> rarityList = analyzeData.getCardSets();

		for(CardSet currentRarity: rarityList){
			analyzeData.getSetNumber().add(currentRarity.getSetNumber());
			analyzeData.getSetNames().add(currentRarity.getSetName());
			analyzeData.getSetRarities().add(currentRarity.getSetRarity());
		}
		setPriceSummaryToLowestFromRarityList(analyzeData, rarityList);

		ArrayList<OwnedCard> cardsList = db.getAnalyzeDataOwnedCardSummaryByGamePlayCardUUID(analyzeData.getGamePlayCardUUID());

		if (cardsList.isEmpty()) {
			analyzeData.setQuantity(0);
		}
		else if(cardsList.size() > 1 ){
			YGOLogger.error("More than 1 summary output from getAnalyzeDataOwnedCardSummaryByGamePlayCardUUID");
		}
		else{
			OwnedCard current = cardsList.get(0);

			analyzeData.setQuantity(current.getQuantity());

			analyzeData.getSetNames().clear();

			Collections.addAll(analyzeData.getSetNames(), current.getSetName().split(","));
		}
	}

	private static void setPriceSummaryToLowestFromRarityList(AnalyzeData currentData, List<CardSet> rarityList) {
		BigDecimal origSetPrice = new BigDecimal(Integer.MAX_VALUE);
		currentData.setCardPriceSummary(origSetPrice);
		//loop through all rarities and show the lowest price for summary
		for (CardSet rarity : rarityList) {

			BigDecimal setPrice = new BigDecimal(rarity.getLowestExistingPrice());
			BigDecimal zero = new BigDecimal(0);

			if ((zero.compareTo(setPrice) != 0) && currentData.getCardPriceSummary().compareTo(setPrice) > 0){
				currentData.setCardPriceSummary(setPrice);
			}
		}
		//If price has not changed due to no existing price data, default to 0
		if(origSetPrice.equals(currentData.getCardPriceSummary())){
			currentData.setCardPriceSummary(new BigDecimal(0));
		}
	}
}
