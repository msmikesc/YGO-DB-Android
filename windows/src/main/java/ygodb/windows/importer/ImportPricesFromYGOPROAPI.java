package ygodb.windows.importer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ygodb.commonlibrary.bean.CardSet;
import ygodb.commonlibrary.connection.DatabaseHashMap;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.Util;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.windows.utility.WindowsUtil;

import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ImportPricesFromYGOPROAPI {

	private static final String OPEN = "\"";
	private static final String CLOSE = "\",";
	private static final String SEP = "\",\"";

	private final HashMap<String, List<String>> nameUpdateMap = new HashMap<>();

	private final HashSet<String> updatedKeysSet = new HashSet<>();
	private final HashMap<String, Integer> updatedMoreThanOnceKeysMap = new HashMap<>();

	public static void main(String[] args) throws SQLException, IOException {
		ImportPricesFromYGOPROAPI mainObj = new ImportPricesFromYGOPROAPI();

		SQLiteConnection db = WindowsUtil.getDBInstance();

		boolean successful = mainObj.run(db);
		if (!successful) {
			YGOLogger.info("Import Failed");
		} else {
			YGOLogger.info("Import Finished");
		}
		db.closeInstance();
	}

	public boolean run(SQLiteConnection db) throws SQLException, IOException {

		String setAPI = "https://db.ygoprodeck.com/api/v7/cardinfo.php?tcgplayer_data=true";

		try {

			URL url = new URL(setAPI);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.connect();

			// Getting the response code
			int responseCode = conn.getResponseCode();

			if (responseCode != 200) {
				YGOLogger.error("HttpResponseCode: " + responseCode);
				return false;
			} else {

				String inline = Util.getApiResponseFromURL(url);

				ObjectMapper objectMapper = new ObjectMapper();
				JsonNode jsonNode = objectMapper.readTree(inline);

				try (FileWriter writer = new FileWriter("C:\\Users\\Mike\\AndroidStudioProjects\\YGODB\\log\\lastPriceLoadJSON.txt", false)) {
					writer.write(inline);
				}

				YGOLogger.info("Finished reading from API");

				//start timer
				long startTime = System.currentTimeMillis();

				JsonNode cards = jsonNode.get(Const.YGOPRO_TOP_LEVEL_DATA);

				for (JsonNode current : cards) {

					String name = Util.getStringOrNull(current, Const.YGOPRO_CARD_NAME);

					JsonNode sets = null;
					Iterator<JsonNode> setIterator = null;

					sets = current.get(Const.YGOPRO_CARD_SETS);

					if (sets != null) {
						setIterator = sets.iterator();
						insertCardSetsForOneCard(setIterator, name, db);
					}

				}

				List<String> namesList = new ArrayList<>(nameUpdateMap.keySet());

				for (String setName : namesList) {
					YGOLogger.debug("Possibly need to handle set name issue count: " + nameUpdateMap.get(setName).size() + " " + setName);

					for (int j = 0; j < nameUpdateMap.get(setName).size(); j++) {
						YGOLogger.debug(nameUpdateMap.get(setName).get(j));
					}

				}

				//TODO address keys updated more than once
				// often only difference is in set url
				// color variants
				// limited, unlimited, and first edition entries existing at same time
				// card name erratas being wierd
				// alt arts
				for (String key : updatedMoreThanOnceKeysMap.keySet()) {
					YGOLogger.info("Key updated more than once:" + key);
				}

				long endTime = System.currentTimeMillis();
				YGOLogger.info("Time to load data to DB:" + Util.millisToShortDHMS(endTime - startTime));

			}
		} catch (Exception e) {
			YGOLogger.logException(e);
		}
		//end timer
		return true;
	}

	public void insertCardSetsForOneCard(Iterator<JsonNode> setIterator, String cardName, SQLiteConnection db)
			throws SQLException {

		while (setIterator.hasNext()) {

			JsonNode currentSet = setIterator.next();

			cardName = Util.checkForTranslatedCardName(cardName);

			insertSingleCardSet(cardName, db, currentSet);
		}
	}

	private void insertSingleCardSet(String cardName, SQLiteConnection db, JsonNode setNode) throws SQLException {
		String setNumber = null;
		String setName = null;
		String setRarity = null;
		String setPrice = null;
		String cardEdition = null;

		try {
			setNumber = Util.getStringOrNull(setNode, Const.YGOPRO_SET_CODE);
			setName = Util.getStringOrNull(setNode, Const.YGOPRO_SET_NAME);
			setRarity = Util.getStringOrNull(setNode, Const.YGOPRO_SET_RARITY);
			setPrice = Util.getStringOrNull(setNode, Const.YGOPRO_SET_PRICE);
			cardEdition = Util.getStringOrNull(setNode, Const.YGOPRO_CARD_EDITION);

			//String set_rarity_code = Util.getStringOrNull(setNode,"set_rarity_code");
			//String set_url = Util.getStringOrNull(setNode,"set_url");
		} catch (Exception e) {
			YGOLogger.info("issue found on " + cardName);
			return;
		}
		if (cardEdition == null) {
			cardEdition = "";
		}

		boolean isFirstEdition = cardEdition.contains(Const.CARD_PRINTING_CONTAINS_FIRST);

		setRarity = Util.checkForTranslatedRarity(setRarity);
		setName = Util.checkForTranslatedSetName(setName);
		setNumber = Util.checkForTranslatedSetNumber(setNumber);

		List<String> translatedList = Util.checkForTranslatedQuadKey(cardName, setNumber, setRarity, setName);
		cardName = translatedList.get(0);
		setNumber = translatedList.get(1);
		setRarity = translatedList.get(2);
		setName = translatedList.get(3);

		setPrice = Util.normalizePrice(setPrice);

		if (setPrice != null && !setPrice.equals(Const.ZERO_PRICE_STRING)) {
			int rowsUpdated = updatePriceUsingMultipleStrategiesWithHashmap(cardName, db, setNumber, setName, setRarity, setPrice, isFirstEdition);
			if (rowsUpdated != 1) {
				YGOLogger.info(OPEN + setNumber + SEP + cardName + SEP + setRarity +
						SEP + setName + CLOSE + setPrice + "," + rowsUpdated + " rows updated");
			}
		}
	}

	private int updatePriceUsingMultipleStrategiesWithHashmap(String cardName, SQLiteConnection db, String setNumber, String setName,
															  String setRarity, String setPrice, boolean isFirstEdition) throws SQLException {

		Map<String, List<CardSet>> rarityHashMap = DatabaseHashMap.getRaritiesInstance(db);
		CardSet matcherInput = new CardSet();
		matcherInput.setSetRarity(setRarity);
		matcherInput.setSetNumber(setNumber);
		matcherInput.setCardName(cardName);
		matcherInput.setSetName(setName);
		String cardLogIdentifier = OPEN + setNumber + SEP + cardName + SEP + setRarity + SEP + setName + CLOSE + setPrice;

		List<CardSet> existingRows = rarityHashMap.get(DatabaseHashMap.getAllMatchingKey(matcherInput));
		if (existingRows != null && !existingRows.isEmpty()) {
			if (existingRows.size() > 1) {
				YGOLogger.error("Somehow more than 1 matching row for an exact match???:" + cardLogIdentifier);
			}

			db.updateCardSetPriceBatchedWithCardAndSetName(setNumber, setRarity, setPrice, setName, cardName, isFirstEdition);

			for (CardSet set : existingRows) {
				addToSetAndMap(DatabaseHashMap.getAllMatchingKey(set) + isFirstEdition);
			}
			return existingRows.size();
		}

		existingRows = rarityHashMap.get(DatabaseHashMap.getSetNameMismatchKey(matcherInput));
		if (existingRows != null && !existingRows.isEmpty()) {
			if (existingRows.size() > 1) {
				YGOLogger.error("Multiple rows updated for set name mismatch from price API:" + cardLogIdentifier);
			}
			db.updateCardSetPriceBatchedWithCardName(setNumber, setRarity, setPrice, cardName, isFirstEdition);
			List<String> setNamesList = nameUpdateMap.computeIfAbsent(setName, k -> new ArrayList<>());
			setNamesList.add(cardName + " " + setNumber);

			for (CardSet set : existingRows) {
				addToSetAndMap(DatabaseHashMap.getAllMatchingKey(set) + isFirstEdition);
			}
			return existingRows.size();
		}

		existingRows = rarityHashMap.get(DatabaseHashMap.getCardNameMismatchKey(matcherInput));
		if (existingRows != null && !existingRows.isEmpty()) {
			if (existingRows.size() > 1) {
				YGOLogger.error("Multiple rows updated for card name mismatch from price API:" + OPEN + setNumber + SEP + cardName + SEP + setRarity +
						SEP + setName + CLOSE + setPrice);
			}
			YGOLogger.debug("Card name mismatch from price API:" + cardLogIdentifier);

			int rowsUpdated = db.updateCardSetPriceWithSetName(setNumber, setRarity, setPrice, setName, isFirstEdition);
			if (rowsUpdated != existingRows.size()) {
				YGOLogger.error("Actual rows updated did not equal predicted for card name mismatch" + cardLogIdentifier);
			}
			for (CardSet set : existingRows) {
				addToSetAndMap(DatabaseHashMap.getAllMatchingKey(set) + isFirstEdition);
			}
			return rowsUpdated;
		}

		existingRows = rarityHashMap.get(DatabaseHashMap.getCardAndSetNameMismatchKey(matcherInput));
		if (existingRows != null && !existingRows.isEmpty()) {
			if (existingRows.size() > 1) {
				YGOLogger.error("Multiple rows updated for card and set name mismatch from price API:" + cardLogIdentifier);
			}
			int rowsUpdated = db.updateCardSetPrice(setNumber, setRarity, setPrice, isFirstEdition);
			YGOLogger.info("Card name mismatch from price API:" + cardLogIdentifier);
			List<String> setNamesList = nameUpdateMap.computeIfAbsent(setName, k -> new ArrayList<>());
			setNamesList.add(cardName + " " + setNumber);
			if (rowsUpdated != existingRows.size()) {
				YGOLogger.error("Actual rows updated did not equal predicted for card and set name mismatch" + cardLogIdentifier);
			}
			for (CardSet set : existingRows) {
				addToSetAndMap(DatabaseHashMap.getAllMatchingKey(set) + isFirstEdition);
			}
			return rowsUpdated;
		}

		existingRows = rarityHashMap.get(DatabaseHashMap.getSetNumberOnlyKey(matcherInput));
		if (existingRows != null && existingRows.size() == 1) {
			int rowsUpdated = db.updateCardSetPrice(setNumber, setPrice, isFirstEdition);
			YGOLogger.info("Card rarity mismatch from price API:" + cardLogIdentifier);
			if (rowsUpdated != 1) {
				YGOLogger.error("Actual rows updated did not equal predicted for card rarity mismatch" + cardLogIdentifier);
			}
			for (CardSet set : existingRows) {
				addToSetAndMap(DatabaseHashMap.getAllMatchingKey(set) + isFirstEdition);
			}
			return rowsUpdated;
		}
		//multiple options or zero are possible for setNumber, so don't update anything
		return 0;
	}

	private void addToSetAndMap(String key) {
		if (updatedKeysSet.contains(key)) {
			updatedMoreThanOnceKeysMap.merge(key, 1, Integer::sum);
		} else {
			updatedKeysSet.add(key);
		}
	}
}