package ygodb.commonlibrary.importer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ygodb.commonlibrary.bean.CardSet;
import ygodb.commonlibrary.bean.GamePlayCard;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.connection.DatabaseHashMap;
import ygodb.commonlibrary.connection.PreparedStatementBatchWrapper;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.ApiUtil;
import ygodb.commonlibrary.utility.Util;
import ygodb.commonlibrary.utility.YGOLogger;

import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ImportPricesFromYGOPROAPI {

	private final HashMap<String, List<String>> setNameUpdateMap = new HashMap<>();

	private void addToSetNameUpdateMap(CardSet currentSetFromAPI) {
		List<String> setNamesList = setNameUpdateMap.computeIfAbsent(currentSetFromAPI.getSetName(), k -> new ArrayList<>());
		setNamesList.add(currentSetFromAPI.getCardName() + " " + currentSetFromAPI.getSetNumber());
	}

	private final HashSet<String> updatedKeysSet = new HashSet<>();
	private final HashMap<String, Integer> updatedMoreThanOnceKeysMap = new HashMap<>();
	private boolean shouldAddToUpdatedKeySetAndMap = true;

	private void addToSetAndMap(String key) {

		if (!shouldAddToUpdatedKeySetAndMap) {
			return;
		}

		if (updatedKeysSet.contains(key)) {
			updatedMoreThanOnceKeysMap.merge(key, 1, Integer::sum);
		} else {
			updatedKeysSet.add(key);
		}
	}

	private final Map<String, Set<String>> updatedURLsMap = new HashMap<>();

	private void addToUpdatedURLsMap(String key, String urlAdded) {

		Set<String> list = updatedURLsMap.computeIfAbsent(key, k -> new HashSet<>());

		list.add(urlAdded);
	}

	private final Map<String, PreparedStatementBatchWrapper> editionToPreparedStatementMap = new HashMap<>();

	private PreparedStatementBatchWrapper getStatementForEdition(String edition, SQLiteConnection db) throws SQLException {
		PreparedStatementBatchWrapper value = editionToPreparedStatementMap.get(edition);

		if (!edition.equals(Const.CARD_PRINTING_FIRST_EDITION)) {
			edition = Const.CARD_PRINTING_UNLIMITED;
		}

		if (value == null) {
			if (edition.equals(Const.CARD_PRINTING_FIRST_EDITION)) {

				value = db.getBatchedPreparedStatementUrlFirst();

				editionToPreparedStatementMap.put(edition, value);
			} else {
				value = db.getBatchedPreparedStatementUrlUnlimited();

				editionToPreparedStatementMap.put(edition, value);
			}
		}
		return value;
	}

	public boolean run(SQLiteConnection db, String lastPriceLoadFilename, boolean handleOptionalDBImports)
			throws SQLException, IOException {

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

				String inline = ApiUtil.getApiResponseFromURL(url);

				ObjectMapper objectMapper = new ObjectMapper();
				JsonNode jsonNode = objectMapper.readTree(inline);

				if (lastPriceLoadFilename != null) {
					try (FileWriter writer = new FileWriter(lastPriceLoadFilename, false)) {
						writer.write(inline);
					}
				}

				inline = null;

				YGOLogger.info("Finished reading from API");

				//start timer
				long startTime = System.currentTimeMillis();

				JsonNode gamePlayCardsNode = jsonNode.get(Const.YGOPRO_TOP_LEVEL_DATA);

				shouldAddToUpdatedKeySetAndMap = handleOptionalDBImports;

				if (handleOptionalDBImports) {
					addAllMissingGamePlayCards(db, gamePlayCardsNode);
					addAllMissingSetUrls(db, gamePlayCardsNode);
				}

				updateAllPricesForAllCards(db, gamePlayCardsNode);

				//log details recorded for future fixes
				List<String> namesList = new ArrayList<>(setNameUpdateMap.keySet());
				for (String setName : namesList) {
					YGOLogger.debug(
							"Possibly need to handle set name issue count: " + setNameUpdateMap.get(setName).size() + " " + setName);
					for (int j = 0; j < setNameUpdateMap.get(setName).size(); j++) {
						YGOLogger.debug(setNameUpdateMap.get(setName).get(j));
					}
				}

				//TODO address keys updated more than once
				// limited, unlimited, and first edition entries existing at same time
				for (String key : updatedMoreThanOnceKeysMap.keySet()) {
					YGOLogger.info("Key updated more than once:" + key);
				}

				long endTime = System.currentTimeMillis();
				YGOLogger.info("Time to load data to DB:" + Util.millisToShortDHMS(endTime - startTime));

				emptyMaps();
			}
		} catch (Exception e) {
			YGOLogger.logException(e);
		}
		return true;
	}

	private void emptyMaps() {
		setNameUpdateMap.clear();
		updatedKeysSet.clear();
		updatedMoreThanOnceKeysMap.clear();
		updatedURLsMap.clear();
		editionToPreparedStatementMap.clear();
		DatabaseHashMap.closeRaritiesInstance();
	}

	private void updateAllPricesForAllCards(SQLiteConnection db, JsonNode gamePlayCardsNode) throws SQLException {
		for (JsonNode currentGamePlayCardNode : gamePlayCardsNode) {

			JsonNode setsListNode = currentGamePlayCardNode.get(Const.YGOPRO_CARD_SETS);

			String cardName = ApiUtil.getStringOrNull(currentGamePlayCardNode, Const.YGOPRO_CARD_NAME);
			cardName = Util.checkForTranslatedCardName(cardName);

			if (setsListNode != null) {
				updateCardSetPricesForOneCard(setsListNode, cardName, db);
			}
		}
		for (PreparedStatementBatchWrapper statement : editionToPreparedStatementMap.values()) {
			statement.finalizeBatches();
		}
	}

	private void addAllMissingSetUrls(SQLiteConnection db, JsonNode gamePlayCardsNode) throws SQLException {
		for (JsonNode currentGamePlayCardNode : gamePlayCardsNode) {
			String cardName = ApiUtil.getStringOrNull(currentGamePlayCardNode, Const.YGOPRO_CARD_NAME);
			cardName = Util.checkForTranslatedCardName(cardName);

			JsonNode setsListNode = currentGamePlayCardNode.get(Const.YGOPRO_CARD_SETS);

			if (setsListNode != null) {
				for (JsonNode currentSetNode : setsListNode) {
					CardSet currentSetFromAPI = getCardSetFromSetNode(cardName, currentSetNode);
					recordEntriesWithMissingDBURL(currentSetFromAPI, db);
				}
			}
		}

		Map<String, List<CardSet>> rarityHashMap = DatabaseHashMap.getRaritiesInstance(db);

		for (Map.Entry<String, Set<String>> e : updatedURLsMap.entrySet()) {
			String key = e.getKey();
			Set<String> urlsSet = e.getValue();
			ArrayList<String> urlsList = new ArrayList<>(urlsSet);

			if (urlsSet.size() == 1) {
				YGOLogger.info("Ready to update set URL:" + key + ":" + urlsList.get(0));

				List<CardSet> listToUpdate = rarityHashMap.get(key);

				if (listToUpdate == null || listToUpdate.size() != 1) {
					YGOLogger.error("Hashmap missing entry before updating url:" + key);
				} else {
					CardSet updateTarget = listToUpdate.get(0);
					db.updateCardSetUrl(updateTarget.getSetNumber(), updateTarget.getSetRarity(), updateTarget.getSetName(),
										updateTarget.getCardName(), urlsList.get(0), null);
				}

			} else {
				YGOLogger.info("Multiple set urls to update for key:" + key);
				attemptToInsertColorVariantsForUrls(key, urlsList, db);
			}
		}
		DatabaseHashMap.closeRaritiesInstance();
	}

	private void addAllMissingGamePlayCards(SQLiteConnection db, JsonNode gamePlayCardsNode) throws SQLException {
		List<OwnedCard> ownedCardsToCheck = db.getAllOwnedCardsWithoutPasscode();
		for (JsonNode currentGamePlayCardNode : gamePlayCardsNode) {
			checkAndInsertMissingGamePlayCard(db, ownedCardsToCheck, currentGamePlayCardNode);
		}
		DatabaseHashMap.closeRaritiesInstance();
		DatabaseHashMap.closeGamePlayCardInstance();
	}

	private void checkAndInsertMissingGamePlayCard(SQLiteConnection db, List<OwnedCard> ownedCardsToCheck,
			JsonNode currentGamePlayCardNode)
			throws SQLException {

		Map<String, List<GamePlayCard>> gamePlayCardMap = DatabaseHashMap.getGamePlayCardsInstance(db);

		String name = ApiUtil.getStringOrNull(currentGamePlayCardNode, Const.YGOPRO_CARD_NAME);
		int passcode = ApiUtil.getIntOrNegativeOne(currentGamePlayCardNode, Const.YGOPRO_CARD_PASSCODE);
		name = Util.checkForTranslatedCardName(name);
		passcode = Util.checkForTranslatedPasscode(passcode);
		String logIdentifier = name + ":" + passcode;

		JsonNode setListNode = currentGamePlayCardNode.get(Const.YGOPRO_CARD_SETS);

		List<GamePlayCard> existingGamePlayCards = gamePlayCardMap.get(String.valueOf(passcode));
		if (existingGamePlayCards == null || existingGamePlayCards.isEmpty()) {
			existingGamePlayCards = gamePlayCardMap.get(name);
		}
		if (existingGamePlayCards != null && existingGamePlayCards.size() > 1) {
			YGOLogger.error("More than one matching gamePlayCard for:" + logIdentifier);
		}

		if (setListNode != null && (existingGamePlayCards == null || existingGamePlayCards.isEmpty())) {
			//no gameplay card, create
			YGOLogger.error("Creating gamePlayCard here for:" + logIdentifier);
			GamePlayCard inserted = ApiUtil.replaceIntoGameplayCardFromYGOPRO(currentGamePlayCardNode, ownedCardsToCheck, db);

			ApiUtil.insertOrIgnoreCardSetsForOneCard(setListNode, inserted.getCardName(), inserted.getGamePlayCardUUID(), db);
		}

		if (existingGamePlayCards != null && existingGamePlayCards.size() == 1 &&
				Const.ARCHETYPE_AUTOGENERATE.equals(existingGamePlayCards.get(0).getArchetype())) {
			//autogenerated value exists
			YGOLogger.error("updating autogenerated gamePlayCard here for:" + logIdentifier);
			ApiUtil.replaceIntoGameplayCardFromYGOPRO(currentGamePlayCardNode, ownedCardsToCheck, db);
		}
	}

	public void attemptToInsertColorVariantsForUrls(String key, List<String> urlsList, SQLiteConnection db) throws SQLException {
		if (key == null || key.isBlank() || urlsList == null || urlsList.size() < 2) {
			YGOLogger.error("Invalid input passed to attemptToInsertColorVariantsForUrls" + key + urlsList);
			return;
		}

		Map<String, List<CardSet>> rarityHashMap = DatabaseHashMap.getRaritiesInstance(db);
		List<CardSet> existingList = rarityHashMap.get(key);
		if (existingList == null || existingList.size() != 1) {
			YGOLogger.error("No existing row found for key" + key + urlsList);
			return;
		}
		CardSet existingEntry = existingList.get(0);

		Map<String, Integer> colorCount = new HashMap<>();
		Map<String, String> urlColors = new HashMap<>();

		for (String url : urlsList) {
			String color = Util.extractColorFromUrl(url);
			colorCount.put(color, colorCount.getOrDefault(color, 0) + 1);
			if (colorCount.get(color) > 1) {
				YGOLogger.error("Multiple URLs with the same color found. Cannot proceed.");
				return;
			}
			urlColors.put(url, color); // Store the identified color for each URL.
		}

		if (colorCount.size() != urlsList.size()) {
			YGOLogger.error("Some URLs don't have a unique color or default color. Cannot proceed.");
			return;
		}

		// At this point, all URLs have a unique color or a unique color with one default color.
		YGOLogger.info("All URLs have a unique color or a unique color with one default color. Proceeding...");
		insertAndUpdateConfirmedColorEntries(db, existingEntry, urlColors);
	}

	private void insertAndUpdateConfirmedColorEntries(SQLiteConnection db, CardSet existingEntry, Map<String, String> urlColors)
			throws SQLException {
		boolean firstEntry = true;
		String lastColor = null;
		String lastURL = null;
		boolean foundDefaultColor = false;

		for (Map.Entry<String, String> entry : urlColors.entrySet()) {
			String url = entry.getKey();
			String color = entry.getValue();
			YGOLogger.info("URL: " + url + ", Identified Color: " + color);

			if (firstEntry) {
				firstEntry = false;
				lastColor = color;
				lastURL = url;
			} else {
				if (Const.DEFAULT_COLOR_VARIANT.equals(color)) {
					foundDefaultColor = true;
				}

				handleUrlColorUpsert(db, existingEntry, color, url);
			}
		}

		handleUrlColorUpsertLast(db, existingEntry, lastColor, lastURL, foundDefaultColor);
	}

	private void handleUrlColorUpsert(SQLiteConnection db, CardSet existingEntry, String color, String url) throws SQLException {
		if (Const.DEFAULT_COLOR_VARIANT.equals(color)) {
			//update existing record with new url
			YGOLogger.info("Updating existing entry to URL:" + url);
			db.updateCardSetUrl(existingEntry.getSetNumber(), existingEntry.getSetRarity(), existingEntry.getSetName(),
								existingEntry.getCardName(), url, null);
		} else {
			//insert new record
			YGOLogger.info("adding new entry for " + color + " with URL:" + url);
			db.insertOrIgnoreIntoCardSet(existingEntry.getSetNumber(), existingEntry.getSetRarity(), existingEntry.getSetName(),
										 existingEntry.getGamePlayCardUUID(), existingEntry.getCardName(), color, url);
		}
	}

	private void handleUrlColorUpsertLast(SQLiteConnection db, CardSet existingEntry, String color, String url,
			boolean previouslyFoundDefault) throws SQLException {
		if (previouslyFoundDefault) {
			//insert new record
			YGOLogger.info("adding new entry for " + color + " with URL:" + url);
			db.insertOrIgnoreIntoCardSet(existingEntry.getSetNumber(), existingEntry.getSetRarity(), existingEntry.getSetName(),
										 existingEntry.getGamePlayCardUUID(), existingEntry.getCardName(), color, url);
		} else {
			//update existing record to whatever the remaining color is
			YGOLogger.info("Updating existing entry to " + color + " with URL:" + url);
			db.updateCardSetUrlAndColor(existingEntry.getSetNumber(), existingEntry.getSetRarity(), existingEntry.getSetName(),
										existingEntry.getCardName(), url, null, color);
		}

	}

	public void updateCardSetPricesForOneCard(JsonNode setsListNode, String cardName, SQLiteConnection db) throws SQLException {

		for (JsonNode currentSetNode : setsListNode) {
			CardSet currentSetFromAPI = getCardSetFromSetNode(cardName, currentSetNode);
			if (currentSetFromAPI != null) {
				updateSingleCardSetPrice(currentSetFromAPI, db);
			}
		}
	}

	private void updateSingleCardSetPrice(CardSet currentSetFromAPI, SQLiteConnection db) throws SQLException {

		if (Util.getSetUrlsThatDoNotExistInstance().contains(currentSetFromAPI.getSetUrl())) {
			return;
		}

		if (currentSetFromAPI.getSetPrice() != null && !currentSetFromAPI.getSetPrice().equals(Const.ZERO_PRICE_STRING)) {
			int rowsUpdated = updatePriceUsingMultipleStrategiesWithHashmap(currentSetFromAPI, db);
			if (rowsUpdated != 1) {
				YGOLogger.info(currentSetFromAPI.getCardLogIdentifier() + "," + rowsUpdated + " rows updated");
			}
		}

	}

	private static CardSet getCardSetFromSetNode(String cardName, JsonNode setNode) {
		String setNumber = null;
		String setName = null;
		String setRarity = null;
		String setPrice = null;
		String cardEdition = null;
		String setUrl = null;

		try {
			setNumber = ApiUtil.getStringOrNull(setNode, Const.YGOPRO_SET_CODE);
			setName = ApiUtil.getStringOrNull(setNode, Const.YGOPRO_SET_NAME);
			setRarity = ApiUtil.getStringOrNull(setNode, Const.YGOPRO_SET_RARITY);
			setPrice = ApiUtil.getStringOrNull(setNode, Const.YGOPRO_SET_PRICE);
			cardEdition = ApiUtil.getStringOrNull(setNode, Const.YGOPRO_CARD_EDITION);
			setUrl = ApiUtil.getStringOrNull(setNode, Const.YGOPRO_SET_URL);

			setRarity = Util.checkForTranslatedRarity(setRarity);
			setName = Util.checkForTranslatedSetName(setName);
			setNumber = Util.checkForTranslatedSetNumber(setNumber);
			List<String> translatedList = Util.checkForTranslatedQuadKey(cardName, setNumber, setRarity, setName);
			cardName = translatedList.get(0);
			setNumber = translatedList.get(1);
			setRarity = translatedList.get(2);
			setName = translatedList.get(3);
			setPrice = Util.normalizePrice(setPrice);

		} catch (Exception e) {
			YGOLogger.info("issue found on " + cardName);
			return null;
		}
		if (cardEdition == null) {
			cardEdition = "";
		}

		CardSet currentSetFromAPI = new CardSet();
		currentSetFromAPI.setSetUrl(setUrl);
		currentSetFromAPI.setSetNumber(setNumber);
		currentSetFromAPI.setSetPrice(setPrice);
		currentSetFromAPI.setCardName(cardName);
		currentSetFromAPI.setSetRarity(setRarity);
		currentSetFromAPI.setEditionPrinting(cardEdition);
		currentSetFromAPI.setSetName(setName);
		return currentSetFromAPI;
	}

	private void recordEntriesWithMissingDBURL(CardSet currentSetFromAPI, SQLiteConnection db) throws SQLException {
		if (currentSetFromAPI == null || currentSetFromAPI.getSetUrl() == null || currentSetFromAPI.getSetUrl().isBlank() ||
				Util.getSetUrlsThatDoNotExistInstance().contains(currentSetFromAPI.getSetUrl())) {
			return;
		}

		//keep track of all entries in sets for updating set url
		Map<String, List<CardSet>> rarityHashMap = DatabaseHashMap.getRaritiesInstance(db);
		List<CardSet> urlKeysMatched = rarityHashMap.get(currentSetFromAPI.getSetUrl());

		if (urlKeysMatched == null || urlKeysMatched.isEmpty()) {
			//no matching url key, log
			recordEntryWithConfirmedMissingDBURL(currentSetFromAPI, rarityHashMap);
		} else if (urlKeysMatched.size() > 1) {
			YGOLogger.error("More than one matching key for url:" + currentSetFromAPI.getSetUrl());
		}
		//if exactly one, leave it alone and don't log anything
	}

	private void recordEntryWithConfirmedMissingDBURL(CardSet currentSetFromAPI, Map<String, List<CardSet>> rarityHashMap) {
		CardSet matcherInput = DatabaseHashMap.getRarityHashMapMatcherInputNoURL(currentSetFromAPI);

		String allMatchUrlKey = DatabaseHashMap.getAllMatchingKeyWithUrl(matcherInput);

		List<CardSet> existingRows = rarityHashMap.get(allMatchUrlKey);

		if (existingRows != null && existingRows.size() == 1) {
			//DB existing entry found with all the exact same details with no set URL
			CardSet existingRowWithNullUrl = existingRows.get(0);
			addToUpdatedURLsMap(DatabaseHashMap.getAllMatchingKey(existingRowWithNullUrl), currentSetFromAPI.getSetUrl());
		} else {
			//DB exact match existing entry not found, check for a match with a different set name
			existingRows = rarityHashMap.get(DatabaseHashMap.getSetNameMismatchKeyWithUrl(matcherInput));

			if (existingRows != null && existingRows.size() == 1) {
				CardSet existingRowWithNullUrl = existingRows.get(0);
				addToUpdatedURLsMap(DatabaseHashMap.getAllMatchingKey(existingRowWithNullUrl), currentSetFromAPI.getSetUrl());
			} else {
				//No match at all found, log for manual fix
				int size = 0;
				if (existingRows != null) {
					size = existingRows.size();
				}

				YGOLogger.info("Found " + size + " matches for all matching url key:" + allMatchUrlKey + ":" +
									   currentSetFromAPI.getCardLogIdentifier());
			}
		}
	}

	private int updatePriceUsingMultipleStrategiesWithHashmap(CardSet currentSetFromAPI, SQLiteConnection db) throws SQLException {

		Integer rowsUpdated1 = attemptPriceUpdateUsingURLBatched(currentSetFromAPI, db);
		if (rowsUpdated1 != null) {
			return rowsUpdated1;
		}

		Integer rowsUpdated2 = attemptPriceUpdateUsingAllProperties(currentSetFromAPI, db);
		if (rowsUpdated2 != null) {
			return rowsUpdated2;
		}

		Integer rowsUpdated3 = attemptPriceUpdateSetNameMismatch(currentSetFromAPI, db);
		if (rowsUpdated3 != null) {
			return rowsUpdated3;
		}

		Integer rowsUpdated4 = attemptPriceUpdateCardNameMismatch(currentSetFromAPI, db);
		if (rowsUpdated4 != null) {
			return rowsUpdated4;
		}

		Integer rowsUpdated5 = attemptPriceUpdateCardAndSetNameMismatch(currentSetFromAPI, db);
		if (rowsUpdated5 != null) {
			return rowsUpdated5;
		}

		Integer rowsUpdated6 = attemptPriceUpdateSetNumberOnly(currentSetFromAPI, db);
		if (rowsUpdated6 != null) {
			return rowsUpdated6;
		}

		//multiple options or zero are possible for setNumber, so don't update anything
		return 0;
	}

	private Integer attemptPriceUpdateSetNumberOnly(CardSet currentSetFromAPI, SQLiteConnection db) throws SQLException {
		Map<String, List<CardSet>> rarityHashMap = DatabaseHashMap.getRaritiesInstance(db);
		boolean isFirstEdition = currentSetFromAPI.getEditionPrinting().contains(Const.CARD_PRINTING_CONTAINS_FIRST);
		CardSet matcherInput = DatabaseHashMap.getRarityHashMapMatcherInputNoURL(currentSetFromAPI);

		List<CardSet> existingRows = rarityHashMap.get(DatabaseHashMap.getSetNumberOnlyKey(matcherInput));
		if (existingRows != null && existingRows.size() == 1) {
			int rowsUpdated = db.updateCardSetPrice(currentSetFromAPI.getSetNumber(), currentSetFromAPI.getSetPrice(), isFirstEdition);
			YGOLogger.info("Card rarity mismatch from price API:" + currentSetFromAPI.getCardLogIdentifier());
			if (rowsUpdated != 1) {
				YGOLogger.error(
						"Actual rows updated did not equal predicted for card rarity mismatch" + currentSetFromAPI.getCardLogIdentifier());
			}
			for (CardSet set : existingRows) {
				addToSetAndMap(DatabaseHashMap.getAllMatchingKeyWithUrl(set) + isFirstEdition);
			}
			return rowsUpdated;
		}
		return null;
	}

	private Integer attemptPriceUpdateCardAndSetNameMismatch(CardSet currentSetFromAPI, SQLiteConnection db) throws SQLException {
		Map<String, List<CardSet>> rarityHashMap = DatabaseHashMap.getRaritiesInstance(db);
		boolean isFirstEdition = currentSetFromAPI.getEditionPrinting().contains(Const.CARD_PRINTING_CONTAINS_FIRST);
		CardSet matcherInput = DatabaseHashMap.getRarityHashMapMatcherInputNoURL(currentSetFromAPI);

		List<CardSet> existingRows;
		existingRows = rarityHashMap.get(DatabaseHashMap.getCardAndSetNameMismatchKey(matcherInput));
		if (existingRows != null && !existingRows.isEmpty()) {
			if (existingRows.size() > 1) {
				YGOLogger.error(
						"Multiple rows updated for card and set name mismatch from price API:" + currentSetFromAPI.getCardLogIdentifier());
			}
			int rowsUpdated = db.updateCardSetPrice(currentSetFromAPI.getSetNumber(), currentSetFromAPI.getSetRarity(),
													currentSetFromAPI.getSetPrice(), isFirstEdition);
			YGOLogger.info("Card name mismatch from price API:" + currentSetFromAPI.getCardLogIdentifier());
			addToSetNameUpdateMap(currentSetFromAPI);
			if (rowsUpdated != existingRows.size()) {
				YGOLogger.error("Actual rows updated did not equal predicted for card and set name mismatch" +
										currentSetFromAPI.getCardLogIdentifier());
			}
			for (CardSet set : existingRows) {
				addToSetAndMap(DatabaseHashMap.getAllMatchingKeyWithUrl(set) + isFirstEdition);
			}
			return rowsUpdated;
		}
		return null;
	}

	private Integer attemptPriceUpdateCardNameMismatch(CardSet currentSetFromAPI, SQLiteConnection db) throws SQLException {
		Map<String, List<CardSet>> rarityHashMap = DatabaseHashMap.getRaritiesInstance(db);
		boolean isFirstEdition = currentSetFromAPI.getEditionPrinting().contains(Const.CARD_PRINTING_CONTAINS_FIRST);
		CardSet matcherInput = DatabaseHashMap.getRarityHashMapMatcherInputNoURL(currentSetFromAPI);

		List<CardSet> existingRows;
		existingRows = rarityHashMap.get(DatabaseHashMap.getCardNameMismatchKey(matcherInput));
		if (existingRows != null && !existingRows.isEmpty()) {
			if (existingRows.size() > 1) {
				YGOLogger.error("Multiple rows updated for card name mismatch from price API:" + currentSetFromAPI.getCardLogIdentifier());
			}
			YGOLogger.debug("Card name mismatch from price API:" + currentSetFromAPI.getCardLogIdentifier());

			int rowsUpdated = db.updateCardSetPriceWithSetName(currentSetFromAPI.getSetNumber(), currentSetFromAPI.getSetRarity(),
															   currentSetFromAPI.getSetPrice(), currentSetFromAPI.getSetName(),
															   isFirstEdition);
			if (rowsUpdated != existingRows.size()) {
				YGOLogger.error(
						"Actual rows updated did not equal predicted for card name mismatch" + currentSetFromAPI.getCardLogIdentifier());
			}
			for (CardSet set : existingRows) {
				addToSetAndMap(DatabaseHashMap.getAllMatchingKeyWithUrl(set) + isFirstEdition);
			}
			return rowsUpdated;
		}
		return null;
	}

	private Integer attemptPriceUpdateSetNameMismatch(CardSet currentSetFromAPI, SQLiteConnection db) throws SQLException {
		boolean isFirstEdition = currentSetFromAPI.getEditionPrinting().contains(Const.CARD_PRINTING_CONTAINS_FIRST);
		Map<String, List<CardSet>> rarityHashMap = DatabaseHashMap.getRaritiesInstance(db);
		CardSet matcherInput = DatabaseHashMap.getRarityHashMapMatcherInputNoURL(currentSetFromAPI);

		List<CardSet> existingRows;
		existingRows = rarityHashMap.get(DatabaseHashMap.getSetNameMismatchKey(matcherInput));
		if (existingRows != null && !existingRows.isEmpty()) {
			if (existingRows.size() > 1) {
				YGOLogger.error("Multiple rows updated for set name mismatch from price API:" + currentSetFromAPI.getCardLogIdentifier());
			}
			int rowsUpdated = db.updateCardSetPriceWithCardName(currentSetFromAPI.getSetNumber(), currentSetFromAPI.getSetRarity(),
																currentSetFromAPI.getSetPrice(), currentSetFromAPI.getCardName(),
																isFirstEdition);
			if (rowsUpdated != existingRows.size()) {
				YGOLogger.error(
						"Actual rows updated did not equal predicted for set name mismatch" + currentSetFromAPI.getCardLogIdentifier());
			}

			addToSetNameUpdateMap(currentSetFromAPI);

			for (CardSet set : existingRows) {
				addToSetAndMap(DatabaseHashMap.getAllMatchingKeyWithUrl(set) + isFirstEdition);
			}
			return rowsUpdated;
		}
		return null;
	}

	private Integer attemptPriceUpdateUsingAllProperties(CardSet currentSetFromAPI, SQLiteConnection db) throws SQLException {
		Map<String, List<CardSet>> rarityHashMap = DatabaseHashMap.getRaritiesInstance(db);
		boolean isFirstEdition = currentSetFromAPI.getEditionPrinting().contains(Const.CARD_PRINTING_CONTAINS_FIRST);
		CardSet matcherInput = DatabaseHashMap.getRarityHashMapMatcherInputNoURL(currentSetFromAPI);

		List<CardSet> existingRows = rarityHashMap.get(DatabaseHashMap.getAllMatchingKey(matcherInput));
		if (existingRows != null && !existingRows.isEmpty()) {
			if (existingRows.size() > 1) {
				// more than 1 exact match, color or art variant
				YGOLogger.error("more than 1 exact match, color or art variant:" + currentSetFromAPI.getCardLogIdentifier());
			}

			int rowsUpdated = db.updateCardSetPriceWithCardAndSetName(currentSetFromAPI.getSetNumber(), currentSetFromAPI.getSetRarity(),
																	  currentSetFromAPI.getSetPrice(), currentSetFromAPI.getSetName(),
																	  currentSetFromAPI.getCardName(), isFirstEdition);
			if (rowsUpdated != existingRows.size()) {
				YGOLogger.error(
						"Actual rows updated did not equal predicted for all properties match" + currentSetFromAPI.getCardLogIdentifier());
			}

			for (CardSet set : existingRows) {
				addToSetAndMap(DatabaseHashMap.getAllMatchingKeyWithUrl(set) + isFirstEdition);
			}
			return rowsUpdated;
		}
		return null;
	}

	private Integer attemptPriceUpdateUsingURLBatched(CardSet currentSetFromAPI, SQLiteConnection db) throws SQLException {
		Map<String, List<CardSet>> rarityHashMap = DatabaseHashMap.getRaritiesInstance(db);
		boolean isFirstEdition = currentSetFromAPI.getEditionPrinting().contains(Const.CARD_PRINTING_CONTAINS_FIRST);

		if (currentSetFromAPI.getSetUrl() != null && !currentSetFromAPI.getSetUrl().isBlank()) {
			List<CardSet> existingRows = rarityHashMap.get(currentSetFromAPI.getSetUrl());
			if (existingRows != null && !existingRows.isEmpty()) {
				if (existingRows.size() > 1) {
					// more than 1 exact match, color or art variant
					YGOLogger.error("more than 1 exact match for set url:" + currentSetFromAPI.getSetUrl());
				}

				PreparedStatementBatchWrapper statement = getStatementForEdition(currentSetFromAPI.getEditionPrinting(), db);
				statement.addSingleValuesSet(List.of(currentSetFromAPI.getSetPrice(), currentSetFromAPI.getSetUrl()));

				for (CardSet set : existingRows) {
					addToSetAndMap(DatabaseHashMap.getAllMatchingKeyWithUrl(set) + isFirstEdition);
				}
				return existingRows.size();
			}
		}
		return null;
	}
}