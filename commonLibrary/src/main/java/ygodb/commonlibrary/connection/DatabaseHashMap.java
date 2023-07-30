package ygodb.commonlibrary.connection;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ygodb.commonlibrary.bean.CardSet;
import ygodb.commonlibrary.bean.GamePlayCard;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.utility.Util;

public class DatabaseHashMap {

	private DatabaseHashMap(){}

	private static Map<String, List<GamePlayCard>> allGamePlayCards = null;

	private static Map<String, List<CardSet>> allCardRarities = null;

	private static Map<String, List<OwnedCard>> allOwnedCards = null;

	public static Map<String, List<GamePlayCard>> getGamePlayCardsInstance(SQLiteConnection db) throws SQLException {
		if (allGamePlayCards == null) {
			allGamePlayCards = db.getAllGamePlayCardsForHashMap();
		}
		return allGamePlayCards;
	}

	public static void closeGamePlayCardInstance() {
		allGamePlayCards = null;
	}

	public static Map<String, List<CardSet>> getRaritiesInstance(SQLiteConnection db) throws SQLException {
		if (allCardRarities == null) {
			allCardRarities = db.getAllCardRaritiesForHashMap();
		}
		return allCardRarities;
	}

	public static void closeRaritiesInstance() {
		allCardRarities = null;
	}

	public static Map<String, List<OwnedCard>> getOwnedInstance(SQLiteConnection db) throws SQLException {
		if (allOwnedCards == null) {
			allOwnedCards = db.getAllOwnedCardsForHashMap();
		}
		return allOwnedCards;
	}

	public static void closeOwnedInstance() {
		allOwnedCards = null;
	}

	public static List<String> getGamePlayCardKeys(GamePlayCard input){
		ArrayList<String> list = new ArrayList<>();

		list.add(input.getCardName());
		list.add(String.valueOf(input.getPasscode()));

		return list;
	}

	public static List<String> getCardRarityKeys(CardSet input){
		ArrayList<String> list = new ArrayList<>();

		list.add(getAllMatchingKey(input));
		list.add(getCardNameMismatchKey(input));
		list.add(getSetNameMismatchKey(input));
		list.add(getCardAndSetNameMismatchKey(input));
		list.add(getSetNumberOnlyKey(input));
		list.add(getSetUrlKey(input));
		list.add(getAllMatchingKeyWithUrl(input));
		list.add(getSetNameMismatchKeyWithUrl(input));

		return list;
	}

	public static String getAllMatchingKeyWithUrl(CardSet input){
		return input.getSetNumber() + input.getSetRarity() + input.getSetName() + input.getCardName() + input.getSetUrl();
	}

	public static String getAllMatchingKey(CardSet input){
		return input.getSetNumber() + input.getSetRarity() + input.getSetName() + input.getCardName();
	}

	public static String getCardNameMismatchKey(CardSet input){
		return input.getSetNumber() + input.getSetRarity() + input.getSetName();
	}

	public static String getSetNameMismatchKey(CardSet input){
		return input.getSetNumber() + input.getSetRarity() + input.getCardName();
	}

	public static String getSetNameMismatchKeyWithUrl(CardSet input){
		return input.getSetNumber() + input.getSetRarity() + input.getCardName() + input.getSetUrl();
	}

	public static String getCardAndSetNameMismatchKey(CardSet input){
		return input.getSetNumber() + input.getSetRarity();
	}

	public static String getSetUrlKey(CardSet input){
		return input.getSetUrl();
	}

	public static String getSetNumberOnlyKey(CardSet input){
		return input.getSetNumber();
	}

	public static String getOwnedCardHashMapKey(OwnedCard input) {
		return input.getSetNumber() + ":" + Util.normalizePrice(input.getPriceBought()) + ":" + input.getDateBought()
				+ ":" + input.getFolderName() + ":" + input.getCondition() + ":" + input.getEditionPrinting();
	}

	public static List<OwnedCard> getExistingOwnedRaritiesForCardFromHashMap(OwnedCard comparison, SQLiteConnection db) throws SQLException {
		Map<String, List<OwnedCard>> data = DatabaseHashMap.getOwnedInstance(db);

		String key = getOwnedCardHashMapKey(comparison);

		List<OwnedCard> list = data.get(key);

		if (list == null) {
			list = new ArrayList<>();
		}

		return list;
	}

	public static CardSet getRarityHashMapMatcherInputNoURL(CardSet currentSetFromAPI) {
		CardSet matcherInput = new CardSet();
		matcherInput.setSetRarity(currentSetFromAPI.getSetRarity());
		matcherInput.setSetNumber(currentSetFromAPI.getSetNumber());
		matcherInput.setCardName(currentSetFromAPI.getCardName());
		matcherInput.setSetName(currentSetFromAPI.getSetName());
		return matcherInput;
	}
}
