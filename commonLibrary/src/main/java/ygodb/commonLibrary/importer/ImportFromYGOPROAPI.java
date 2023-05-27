package ygodb.commonLibrary.importer;

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
import ygodb.commonLibrary.bean.GamePlayCard;
import ygodb.commonLibrary.bean.OwnedCard;
import ygodb.commonLibrary.bean.SetMetaData;
import ygodb.commonLibrary.connection.SQLiteConnection;
import ygodb.commonLibrary.constant.Const;
import ygodb.commonLibrary.utility.Util;

import java.sql.SQLException;

public class ImportFromYGOPROAPI {

	public void run(SQLiteConnection db, String setName) throws SQLException, IOException {

		setName = setName.trim();

		String setAPI = "https://db.ygoprodeck.com/api/v7/cardinfo.php?cardset=";

		String apiURL = setAPI + URLEncoder.encode(setName, StandardCharsets.UTF_8.name());

		try {

			updateDBWithSetsFromAPI(setName, db);

			URL url = new URL(apiURL);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.connect();

			// Getting the response code
			int responsecode = conn.getResponseCode();

			if (responsecode != 200) {
				throw new RuntimeException("HttpResponseCode: " + responsecode);
			} else {

				String inline = Util.getApiResponseFromURL(url);

				ObjectMapper objectMapper = new ObjectMapper();
				JsonNode jsonNode = objectMapper.readTree(inline);

				System.out.println("Finished reading from API");

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
						insertCardSetsForOneCard(setIterator, inserted.cardName, inserted.gamePlayCardUUID, db);
					}

				}

				Util.checkForIssuesWithSet(setName, db);
				Util.checkSetCounts(db);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static GamePlayCard insertGameplayCardFromYGOPRO(JsonNode current, List<OwnedCard> ownedCardsToCheck, SQLiteConnection db) throws SQLException {

		String name = Util.getStringOrNull(current, Const.YGOPRO_CARD_NAME);
		String type = Util.getStringOrNull(current, Const.YGOPRO_CARD_TYPE);
		Integer passcode = Util.getIntOrNegativeOne(current, Const.YGOPRO_CARD_PASSCODE);
		String desc = Util.getStringOrNull(current, Const.YGOPRO_CARD_TEXT);
		String attribute = Util.getStringOrNull(current, Const.YGOPRO_ATTRIBUTE);
		String race = Util.getStringOrNull(current, Const.YGOPRO_RACE);
		String linkval = Util.getStringOrNull(current, Const.YGOPRO_LINK_VALUE);
		String level = Util.getStringOrNull(current, Const.YGOPRO_LEVEL_RANK);
		String scale = Util.getStringOrNull(current, Const.YGOPRO_PENDULUM_SCALE);
		String atk = Util.getStringOrNull(current, Const.YGOPRO_ATTACK);
		String def = Util.getStringOrNull(current, Const.YGOPRO_DEFENSE);
		String archetype = Util.getStringOrNull(current, Const.YGOPRO_ARCHETYPE);

		GamePlayCard gamePlayCard = new GamePlayCard();

		name = Util.checkForTranslatedCardName(name);
		passcode = Util.checkForTranslatedPasscode(passcode);

		gamePlayCard.cardName = name;
		gamePlayCard.cardType = type;
		gamePlayCard.archetype = archetype;
		gamePlayCard.passcode = passcode;

		gamePlayCard.gamePlayCardUUID = db.getGamePlayCardUUIDFromPasscode(passcode);

		if(gamePlayCard.gamePlayCardUUID == null) {
			Pair<String, String> uuidAndName = Util.getGamePlayCardUUIDFromTitleOrGenerateNewWithSkillCheck(name, db);

			gamePlayCard.gamePlayCardUUID = uuidAndName.getKey();
			gamePlayCard.cardName = uuidAndName.getValue();
		}

		gamePlayCard.desc = desc;
		gamePlayCard.attribute = attribute;
		gamePlayCard.race = race;
		gamePlayCard.linkval = linkval;
		gamePlayCard.scale = scale;
		gamePlayCard.level = level;
		gamePlayCard.atk = atk;
		gamePlayCard.def = def;

		db.replaceIntoGamePlayCard(gamePlayCard);

		for(OwnedCard currentOwnedCard : ownedCardsToCheck){
			if(currentOwnedCard.gamePlayCardUUID.equals(gamePlayCard.gamePlayCardUUID)){
				currentOwnedCard.passcode = passcode;
				db.updateOwnedCardByUUID(currentOwnedCard);
			}
		}

		return gamePlayCard;
	}

	public static void insertCardSetsForOneCard(Iterator<JsonNode> setIterator, String name, String gamePlayCardUUID, SQLiteConnection db)
			throws SQLException {

		while(setIterator.hasNext()) {

			JsonNode currentSet = setIterator.next();

			String setCode = null;
			String setName = null;
			String setRarity = null;
			String setPrice = null;

			try {
				setCode = Util.getStringOrNull(currentSet,Const.YGOPRO_SET_CODE);
				setName = Util.getStringOrNull(currentSet,Const.YGOPRO_SET_NAME);
				setRarity = Util.getStringOrNull(currentSet,Const.YGOPRO_SET_RARITY);
				setPrice = Util.getStringOrNull(currentSet,Const.YGOPRO_SET_PRICE);
			} catch (Exception e) {
				System.out.println("issue found on " + name);
				continue;
			}

			name = Util.checkForTranslatedCardName(name);
			setRarity = Util.checkForTranslatedRarity(setRarity);
			setName = Util.checkForTranslatedSetName(setName);
			setCode = Util.checkForTranslatedSetNumber(setCode);

			db.replaceIntoCardSetWithSoftPriceUpdate(setCode, setRarity, setName, gamePlayCardUUID, setPrice, name);

		}
	}

	public static void updateDBWithSetsFromAPI(String setName, SQLiteConnection db) {
		String setAPI = "https://db.ygoprodeck.com/api/v7/cardsets.php";

		boolean specificSet = setName != null && !setName.isBlank();

		try {
			URL url = new URL(setAPI);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.connect();

			// Getting the response code
			int responseCode = conn.getResponseCode();

			if (responseCode != 200) {
				throw new RuntimeException("HttpResponseCode: " + responseCode);
			} else {

				String inline = Util.getApiResponseFromURL(url);

				ObjectMapper objectMapper = new ObjectMapper();
				JsonNode jsonNode = objectMapper.readTree(inline);

				ArrayList<SetMetaData> list = db.getAllSetMetaDataFromSetData();
				ArrayList<String> dbSetNames = new ArrayList<>();

				for(SetMetaData current : list) {
					dbSetNames.add(current.setName);
				}

				for (JsonNode set : jsonNode) {
					String currentSetName = Util.getStringOrNull(set, Const.YGOPRO_SET_NAME);
					String setCode = Util.getStringOrNull(set, Const.YGOPRO_SET_CODE);
					int numOfCards = Util.getIntOrNegativeOne(set, Const.YGOPRO_TOTAL_CARDS_IN_SET);
					String tcgDate = Util.getStringOrNull(set, Const.YGOPRO_TCG_RELEASE_DATE);

					String newSetName = Util.checkForTranslatedSetName(currentSetName);

					if (!dbSetNames.contains(newSetName)) {
						System.out.println("Missing Set: " + newSetName);
					}

					if (!specificSet) {
						db.replaceIntoCardSetMetaData(newSetName, setCode, numOfCards, tcgDate);
					}
					if (specificSet && currentSetName.equalsIgnoreCase(setName)) {
						db.replaceIntoCardSetMetaData(newSetName, setCode, numOfCards, tcgDate);
					}
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
