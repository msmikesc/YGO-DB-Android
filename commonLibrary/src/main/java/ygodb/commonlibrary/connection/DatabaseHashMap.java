package ygodb.commonlibrary.connection;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ygodb.commonlibrary.bean.CardSet;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.utility.Util;

public class DatabaseHashMap {

	private DatabaseHashMap(){}

	private static Map<String, List<CardSet>> allCardRarities = null;

	private static Map<String, List<OwnedCard>> allOwnedCards = null;

	public static Map<String, List<CardSet>> getRaritiesInstance(SQLiteConnection db) throws SQLException {
		if (allCardRarities == null) {
			allCardRarities = db.getAllCardRarities();
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

	public static List<CardSet> getRaritiesOfCardInSetFromHashMap(String setNumber, SQLiteConnection db) throws SQLException {
		Map<String, List<CardSet>> data = DatabaseHashMap.getRaritiesInstance(db);

		List<CardSet> list = data.get(setNumber);

		if (list == null) {
			list = new ArrayList<>();
		}

		return list;
	}

	public static List<String> getCardRarityKeys(CardSet input){
		ArrayList<String> list = new ArrayList<>();

		list.add(getAllMatchingKey(input));
		list.add(getCardNameMismatchKey(input));
		list.add(getSetNameMismatchKey(input));
		list.add(getCardAndSetNameMismatchKey(input));
		list.add(getSetNumberOnlyKey(input));

		return list;
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

	public static String getCardAndSetNameMismatchKey(CardSet input){
		return input.getSetNumber() + input.getSetRarity();
	}

	public static String getSetNumberOnlyKey(CardSet input){
		return input.getSetNumber();
	}

	public static List<OwnedCard> getExistingOwnedRaritiesForCardFromHashMap(String setNumber, String priceBought,
																			 String dateBought, String folderName, String condition, String editionPrinting, SQLiteConnection db) throws SQLException {
		Map<String, List<OwnedCard>> data = DatabaseHashMap.getOwnedInstance(db);

		String key = setNumber +":"+ Util.normalizePrice(priceBought) +":"+ dateBought +":"+ folderName +":"+ condition
				+ editionPrinting;

		List<OwnedCard> list = data.get(key);

		if (list == null) {
			list = new ArrayList<>();
		}

		return list;
	}

}
