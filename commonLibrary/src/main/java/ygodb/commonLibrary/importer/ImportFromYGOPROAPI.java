package ygodb.commonLibrary.importer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.util.Pair;
import ygodb.commonLibrary.bean.GamePlayCard;
import ygodb.commonLibrary.bean.OwnedCard;
import ygodb.commonLibrary.bean.SetMetaData;
import ygodb.commonLibrary.connection.SQLiteConnection;
import ygodb.commonLibrary.connection.Util;

import java.sql.SQLException;

public class ImportFromYGOPROAPI {

	public void run(SQLiteConnection db, String setName) throws SQLException, IOException {

		setName = setName.trim();

		String setAPI = "https://db.ygoprodeck.com/api/v7/cardinfo.php?cardset=";

		String apiURL = setAPI + URLEncoder.encode(setName);

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

				String inline = "";
				InputStream inputStreamFromURL = url.openStream();

				ByteArrayOutputStream result = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				for (int length; (length = inputStreamFromURL.read(buffer)) != -1; ) {
					result.write(buffer, 0, length);
				}
				inline = result.toString("UTF-8");

				ObjectMapper objectMapper = new ObjectMapper();
				JsonNode jsonNode = objectMapper.readTree(inline);

				inline = null;

				JsonNode cards = jsonNode.get("data");

				Iterator<JsonNode> keyset = cards.iterator();

				ArrayList<OwnedCard> ownedCardsToCheck = db.getAllOwnedCardsWithoutPasscode();

				while (keyset.hasNext()) {

					JsonNode current = keyset.next();

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

	public static GamePlayCard insertGameplayCardFromYGOPRO(JsonNode current, ArrayList<OwnedCard> ownedCardsToCheck, SQLiteConnection db) throws SQLException {

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

		GamePlayCard GPC = new GamePlayCard();

		name = Util.checkForTranslatedCardName(name);
		passcode = Util.checkForTranslatedPasscode(passcode);

		GPC.cardName = name;
		GPC.cardType = type;
		GPC.archetype = archetype;
		GPC.passcode = passcode;

		Pair<String, String> UUIDAndName = Util.getGamePlayCardUUIDFromTitleOrGenerateNewWithSkillCheck(name, db);

		GPC.gamePlayCardUUID = UUIDAndName.getKey();
		GPC.cardName = UUIDAndName.getValue();

		GPC.desc = desc;
		GPC.attribute = attribute;
		GPC.race = race;
		GPC.linkval = linkval;
		GPC.scale = scale;
		GPC.level = level;
		GPC.atk = atk;
		GPC.def = def;

		db.replaceIntoGamePlayCard(GPC);

		for(OwnedCard currentOwnedCard : ownedCardsToCheck){
			if(currentOwnedCard.gamePlayCardUUID.equals(GPC.gamePlayCardUUID)){
				currentOwnedCard.passcode = passcode;
				db.UpdateOwnedCardByUUID(currentOwnedCard);
			}
		}

		return GPC;
	}

	public static void insertCardSetsForOneCard(Iterator<JsonNode> setIterator, String name, String gamePlayCardUUID, SQLiteConnection db)
			throws SQLException {

		while(setIterator.hasNext()) {

			JsonNode currentSet = setIterator.next();

			String set_code = null;
			String set_name = null;
			String set_rarity = null;
			String set_price = null;

			try {
				set_code = Util.getStringOrNull(currentSet,"set_code");
				set_name = Util.getStringOrNull(currentSet,"set_name");
				set_rarity = Util.getStringOrNull(currentSet,"set_rarity");
				set_price = Util.getStringOrNull(currentSet,"set_price");
			} catch (Exception e) {
				System.out.println("issue found on " + name);
				continue;
			}

			//set_price = Util.getAdjustedPriceFromRarity(set_rarity, set_price);

			name = Util.checkForTranslatedCardName(name);
			set_rarity = Util.checkForTranslatedRarity(set_rarity);
			set_name = Util.checkForTranslatedSetName(set_name);
			set_code = Util.checkForTranslatedSetNumber(set_code);

			db.replaceIntoCardSetWithSoftPriceUpdate(set_code, set_rarity, set_name, gamePlayCardUUID, set_price, name);

		}
	}

	public static void updateDBWithSetsFromAPI(String setName, SQLiteConnection db) {
		String setAPI = "https://db.ygoprodeck.com/api/v7/cardsets.php";

		boolean specificSet = true;

		if (setName == null || setName.isBlank()) {
			specificSet = false;
		}

		try {
			URL url = new URL(setAPI);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.connect();

			// Getting the response code
			int responsecode = conn.getResponseCode();

			if (responsecode != 200) {
				throw new RuntimeException("HttpResponseCode: " + responsecode);
			} else {

				String inline = "";
				Scanner scanner = new Scanner(url.openStream());

				// Write all the JSON data into a string using a scanner
				while (scanner.hasNext()) {
					inline += scanner.nextLine();
				}

				// Close the scanner
				scanner.close();

				ObjectMapper objectMapper = new ObjectMapper();
				JsonNode jsonNode = objectMapper.readTree(inline);

				inline = null;

				ArrayList<SetMetaData> list = db.getAllSetMetaDataFromSetData();
				ArrayList<String> dbSetNames = new ArrayList<>();

				for(SetMetaData current : list) {
					dbSetNames.add(current.set_name);
				}

				Iterator<JsonNode> keyset = jsonNode.iterator();

				while (keyset.hasNext()) {
					JsonNode set = keyset.next();

					String set_name = Util.getStringOrNull(set, "set_name");
					String set_code = Util.getStringOrNull(set, "set_code");
					int num_of_cards = Util.getIntOrNegativeOne(set, "num_of_cards");
					String tcg_date = Util.getStringOrNull(set, "tcg_date");
					
					String newSetName = Util.checkForTranslatedSetName(set_name);

					if(!dbSetNames.contains(newSetName)) {
						System.out.println("Missing Set: "+ newSetName);
					}

					if (!specificSet) {
						db.replaceIntoCardSetMetaData(newSetName, set_code, num_of_cards, tcg_date);
					}
					if (specificSet && set_name.equalsIgnoreCase(setName)) {
						db.replaceIntoCardSetMetaData(newSetName, set_code, num_of_cards, tcg_date);
					}
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
