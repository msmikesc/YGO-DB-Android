package com.example.ygodb.backend.connection;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.example.ygodb.backend.bean.CardSet;
import com.example.ygodb.backend.bean.OwnedCard;

public class DatabaseHashMap {

	private static HashMap<String, ArrayList<CardSet>> allCardRarities = null;

	private static HashMap<String, ArrayList<OwnedCard>> allOwnedCards = null;

	public static HashMap<String, ArrayList<CardSet>> getRaritiesInstance() throws SQLException {
		if (allCardRarities == null) {
			allCardRarities = SQLiteConnection.getObj().getAllCardRarities();
		}
		return allCardRarities;
	}

	public static void closeRaritiesInstance() throws SQLException {
		allCardRarities = null;
	}

	public static HashMap<String, ArrayList<OwnedCard>> getOwnedInstance() throws SQLException {
		if (allOwnedCards == null) {
			allOwnedCards = SQLiteConnection.getObj().getAllOwnedCardsForHashMap();
		}
		return allOwnedCards;
	}

	public static void closeOwnedInstance() throws SQLException {
		allOwnedCards = null;
	}

	public static ArrayList<CardSet> getRaritiesOfCardInSetFromHashMap(String setNumber) throws SQLException {
		HashMap<String, ArrayList<CardSet>> data = DatabaseHashMap.getRaritiesInstance();

		ArrayList<CardSet> list = data.get(setNumber);

		if (list == null) {
			list = new ArrayList<CardSet>();
		}

		return list;
	}

	public static ArrayList<OwnedCard> getExistingOwnedRaritesForCardFromHashMap(String setNumber, String priceBought,
			String dateBought, String folderName, String condition, String editionPrinting) throws SQLException {
		HashMap<String, ArrayList<OwnedCard>> data = DatabaseHashMap.getOwnedInstance();

		String key = setNumber + Util.normalizePrice(priceBought) + dateBought + folderName + condition
				+ editionPrinting;

		ArrayList<OwnedCard> list = data.get(key);

		if (list == null) {
			list = new ArrayList<OwnedCard>();
		}

		return list;
	}

}
