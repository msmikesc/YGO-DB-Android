package ygodb.windows.importer;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.util.Pair;
import ygodb.commonlibrary.bean.GamePlayCard;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.bean.SetMetaData;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.Util;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.windows.utility.WindowsUtil;

import java.sql.SQLException;

public class ImportFromYGOPROAPI {

	public static void main(String[] args) throws SQLException, IOException {

		String setName = "Speed Duel GX: Duelists of Shadows";

		ImportFromYGOPROAPI mainObj = new ImportFromYGOPROAPI();

		SQLiteConnection db = WindowsUtil.getDBInstance();

		boolean successful = mainObj.run(db, setName);
		if (!successful) {
			YGOLogger.info("Import Failed");
		} else {
			YGOLogger.info("Import Finished");
		}
		db.closeInstance();
	}

	public boolean run(SQLiteConnection db, String setName) throws SQLException, IOException {

		setName = setName.trim();

		String setAPI = "https://db.ygoprodeck.com/api/v7/cardinfo.php?cardset=";

		String apiURL = setAPI + URLEncoder.encode(setName, StandardCharsets.UTF_8.name());

		try {

			boolean setsSuccessful = updateDBWithSetsFromAPI(setName, db);

			if (!setsSuccessful) {
				YGOLogger.error("updateDBWithSetsFromAPI was not successful");
			}

			URL url = new URL(apiURL);

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

				YGOLogger.info("Finished reading from API");

				JsonNode cards = jsonNode.get(Const.YGOPRO_TOP_LEVEL_DATA);

				Iterator<JsonNode> keySet = cards.iterator();

				ArrayList<OwnedCard> ownedCardsToCheck = db.getAllOwnedCardsWithoutPasscode();

				while (keySet.hasNext()) {

					JsonNode current = keySet.next();

					GamePlayCard inserted = insertGameplayCardFromYGOPRO(current, ownedCardsToCheck, db);

					JsonNode sets = null;
					Iterator<JsonNode> setIterator = null;
					sets = current.get(Const.YGOPRO_CARD_SETS);


					if (sets != null) {
						setIterator = sets.iterator();
						insertCardSetsForOneCard(setIterator, inserted.getCardName(), inserted.getGamePlayCardUUID(), db);
					}

				}

				Util.checkForIssuesWithSet(setName, db);
				Util.checkSetCounts(db);

			}
		} catch (Exception e) {
			YGOLogger.logException(e);
		}
		return true;
	}

	public static GamePlayCard insertGameplayCardFromYGOPRO(JsonNode current, List<OwnedCard> ownedCardsToCheck, SQLiteConnection db) throws SQLException {

		String name = Util.getStringOrNull(current, Const.YGOPRO_CARD_NAME);
		String type = Util.getStringOrNull(current, Const.YGOPRO_CARD_TYPE);
		Integer passcode = Util.getIntOrNegativeOne(current, Const.YGOPRO_CARD_PASSCODE);
		String desc = Util.getStringOrNull(current, Const.YGOPRO_CARD_TEXT);
		String attribute = Util.getStringOrNull(current, Const.YGOPRO_ATTRIBUTE);
		String race = Util.getStringOrNull(current, Const.YGOPRO_RACE);
		String linkValue = Util.getStringOrNull(current, Const.YGOPRO_LINK_VALUE);
		String level = Util.getStringOrNull(current, Const.YGOPRO_LEVEL_RANK);
		String scale = Util.getStringOrNull(current, Const.YGOPRO_PENDULUM_SCALE);
		String atk = Util.getStringOrNull(current, Const.YGOPRO_ATTACK);
		String def = Util.getStringOrNull(current, Const.YGOPRO_DEFENSE);
		String archetype = Util.getStringOrNull(current, Const.YGOPRO_ARCHETYPE);

		GamePlayCard gamePlayCard = new GamePlayCard();

		name = Util.checkForTranslatedCardName(name);
		passcode = Util.checkForTranslatedPasscode(passcode);

		gamePlayCard.setCardName(name);
		gamePlayCard.setCardType(type);
		gamePlayCard.setArchetype(archetype);
		gamePlayCard.setPasscode(passcode);

		gamePlayCard.setGamePlayCardUUID(db.getGamePlayCardUUIDFromPasscode(passcode));

		if (gamePlayCard.getGamePlayCardUUID() == null) {
			Pair<String, String> uuidAndName = Util.getGamePlayCardUUIDFromTitleOrGenerateNewWithSkillCheck(name, db);

			gamePlayCard.setGamePlayCardUUID(uuidAndName.getKey());
			gamePlayCard.setCardName(uuidAndName.getValue());
		}

		gamePlayCard.setDesc(desc);
		gamePlayCard.setAttribute(attribute);
		gamePlayCard.setRace(race);
		gamePlayCard.setLinkVal(linkValue);
		gamePlayCard.setScale(scale);
		gamePlayCard.setLevel(level);
		gamePlayCard.setAtk(atk);
		gamePlayCard.setDef(def);

		db.replaceIntoGamePlayCard(gamePlayCard);

		for (OwnedCard currentOwnedCard : ownedCardsToCheck) {
			if (currentOwnedCard.getGamePlayCardUUID().equals(gamePlayCard.getGamePlayCardUUID())) {
				currentOwnedCard.setPasscode(passcode);
				db.updateOwnedCardByUUID(currentOwnedCard);
			}
		}

		return gamePlayCard;
	}

	public static void insertCardSetsForOneCard(Iterator<JsonNode> setIterator, String name, String gamePlayCardUUID, SQLiteConnection db)
			throws SQLException {

		while (setIterator.hasNext()) {

			JsonNode currentSet = setIterator.next();

			String setCode = null;
			String setName = null;
			String setRarity = null;
			String setPrice = null;

			try {
				setCode = Util.getStringOrNull(currentSet, Const.YGOPRO_SET_CODE);
				setName = Util.getStringOrNull(currentSet, Const.YGOPRO_SET_NAME);
				setRarity = Util.getStringOrNull(currentSet, Const.YGOPRO_SET_RARITY);
				setPrice = Util.getStringOrNull(currentSet, Const.YGOPRO_SET_PRICE);
			} catch (Exception e) {
				YGOLogger.error("issue found on " + name);
				continue;
			}

			name = Util.checkForTranslatedCardName(name);
			setRarity = Util.checkForTranslatedRarity(setRarity);
			setName = Util.checkForTranslatedSetName(setName);
			setCode = Util.checkForTranslatedSetNumber(setCode);

			List<String> translatedList = Util.checkForTranslatedQuadKey(name, setCode, setRarity, setName);
			name = translatedList.get(0);
			setCode = translatedList.get(1);
			setRarity = translatedList.get(2);
			setName = translatedList.get(3);

			db.replaceIntoCardSetWithSoftPriceUpdate(setCode, setRarity, setName, gamePlayCardUUID, setPrice, name);

		}
	}

	public static boolean updateDBWithSetsFromAPI(String inputSetName, SQLiteConnection db) {
		String setAPI = "https://db.ygoprodeck.com/api/v7/cardsets.php";

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

				ArrayList<SetMetaData> list = db.getAllSetMetaDataFromSetData();
				ArrayList<String> dbSetNames = new ArrayList<>();

				for (SetMetaData current : list) {
					dbSetNames.add(current.getSetName());
				}

				for (JsonNode setNode : jsonNode) {
					handleSingleSetNode(inputSetName, db, dbSetNames, setNode);
				}

			}

		} catch (Exception e) {
			YGOLogger.logException(e);
		}
		return true;
	}

	private static void handleSingleSetNode(String inputSetName, SQLiteConnection db,
											List<String> dbSetNames, JsonNode setNode) throws SQLException {

		boolean isSpecificSet = inputSetName != null && !inputSetName.isBlank();
		String currentSetName = Util.getStringOrNull(setNode, Const.YGOPRO_SET_NAME);
		String setCode = Util.getStringOrNull(setNode, Const.YGOPRO_SET_CODE);
		int numOfCards = Util.getIntOrNegativeOne(setNode, Const.YGOPRO_TOTAL_CARDS_IN_SET);
		String tcgDate = Util.getStringOrNull(setNode, Const.YGOPRO_TCG_RELEASE_DATE);

		String newSetName = Util.checkForTranslatedSetName(currentSetName);

		if (!dbSetNames.contains(newSetName)
				&& (newSetName == null || !newSetName.equalsIgnoreCase(inputSetName))
				&& (!Const.IGNORED_MISSING_SETS.contains(newSetName))) {
			YGOLogger.info("Missing Set: " + newSetName);
		}

		if (!isSpecificSet) {
			ArrayList<SetMetaData> setMetaDataList = db.getSetMetaDataFromSetName(newSetName);

			if (setMetaDataList.isEmpty()) {
				db.replaceIntoCardSetMetaData(newSetName, setCode, numOfCards, tcgDate);
			}
		}
		if (isSpecificSet && inputSetName.equalsIgnoreCase(currentSetName)) {
			ArrayList<SetMetaData> setMetaDataList = db.getSetMetaDataFromSetName(newSetName);

			if (setMetaDataList.isEmpty()) {
				db.replaceIntoCardSetMetaData(newSetName, setCode, numOfCards, tcgDate);
			}
		}
	}
}
