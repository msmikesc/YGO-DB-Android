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

				JsonNode cards = jsonNode.get("data");

				Iterator<JsonNode> keySet = cards.iterator();

				ArrayList<OwnedCard> ownedCardsToCheck = db.getAllOwnedCardsWithoutPasscode();

				while (keySet.hasNext()) {

					JsonNode current = keySet.next();

					GamePlayCard inserted = insertGameplayCardFromYGOPRO(current, ownedCardsToCheck, db);

					JsonNode sets = null;
					Iterator<JsonNode> setIterator = null;
					sets = current.get("card_sets");


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

		String name = Util.getStringOrNull(current, "name");
		String type = Util.getStringOrNull(current, "type");
		Integer passcode = Util.getIntOrNegativeOne(current, "id");// passcode
		String desc = Util.getStringOrNull(current, "desc");
		String attribute = Util.getStringOrNull(current, "attribute");
		String race = Util.getStringOrNull(current, "race");
		String linkval = Util.getStringOrNull(current, "linkval");
		String level = Util.getStringOrNull(current, "level");
		String scale = Util.getStringOrNull(current, "scale");
		String atk = Util.getStringOrNull(current, "atk");
		String def = Util.getStringOrNull(current, "def");
		String archetype = Util.getStringOrNull(current, "archetype");

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
				setCode = Util.getStringOrNull(currentSet,"set_code");
				setName = Util.getStringOrNull(currentSet,"set_name");
				setRarity = Util.getStringOrNull(currentSet,"set_rarity");
				setPrice = Util.getStringOrNull(currentSet,"set_price");
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
					String currentSetName = Util.getStringOrNull(set, "set_name");
					String setCode = Util.getStringOrNull(set, "set_code");
					int numOfCards = Util.getIntOrNegativeOne(set, "num_of_cards");
					String tcgDate = Util.getStringOrNull(set, "tcg_date");

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
