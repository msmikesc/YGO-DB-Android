package ygodb.commonLibrary.connection;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ygodb.commonLibrary.bean.CardSet;
import ygodb.commonLibrary.bean.OwnedCard;
import ygodb.commonLibrary.utility.Util;

public class DatabaseHashMap {

	private static Map<String, ArrayList<CardSet>> allCardRarities = null;

	private static Map<String, ArrayList<OwnedCard>> allOwnedCards = null;

	public static Map<String, ArrayList<CardSet>> getRaritiesInstance(SQLiteConnection db) throws SQLException {
		if (allCardRarities == null) {
			allCardRarities = db.getAllCardRarities();
		}
		return allCardRarities;
	}

	public static void closeRaritiesInstance() {
		allCardRarities = null;
	}

	public static Map<String, ArrayList<OwnedCard>> getOwnedInstance(SQLiteConnection db) throws SQLException {
		if (allOwnedCards == null) {
			allOwnedCards = db.getAllOwnedCardsForHashMap();
		}
		return allOwnedCards;
	}

	public static void closeOwnedInstance() {
		allOwnedCards = null;
	}

	public static List<CardSet> getRaritiesOfCardInSetFromHashMap(String setNumber, SQLiteConnection db) throws SQLException {
		Map<String, ArrayList<CardSet>> data = DatabaseHashMap.getRaritiesInstance(db);

		ArrayList<CardSet> list = data.get(setNumber);

		if (list == null) {
			list = new ArrayList<>();
		}

		return list;
	}

	public static List<OwnedCard> getExistingOwnedRaritesForCardFromHashMap(String setNumber, String priceBought,
																			String dateBought, String folderName, String condition, String editionPrinting, SQLiteConnection db) throws SQLException {
		Map<String, ArrayList<OwnedCard>> data = DatabaseHashMap.getOwnedInstance(db);

		String key = setNumber + Util.normalizePrice(priceBought) + dateBought + folderName + condition
				+ editionPrinting;

		ArrayList<OwnedCard> list = data.get(key);

		if (list == null) {
			list = new ArrayList<>();
		}

		return list;
	}

}
