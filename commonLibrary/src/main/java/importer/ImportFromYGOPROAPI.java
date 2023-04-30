package importer;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Scanner;

import org.json.*;

import connection.SQLiteConnection;
import connection.Util;

import java.sql.SQLException;

public class ImportFromYGOPROAPI {

	/*
	
	public static void main(String[] args) throws SQLException, IOException {
		ImportFromYGOPROAPI mainObj = new ImportFromYGOPROAPI();
		mainObj.run();
		
		System.out.println("Import Finished");
	}*/
	
	

	public void run(SQLiteConnection db) throws SQLException, IOException {

		String setName = "Amazing Defenders";

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
				Scanner scanner = new Scanner(url.openStream());

				// Write all the JSON data into a string using a scanner
				while (scanner.hasNext()) {
					inline += scanner.nextLine();
				}

				// Close the scanner
				scanner.close();

				JSONObject jo = new JSONObject(inline);

				inline = null;

				JSONArray cards = (JSONArray) jo.get("data");

				for(int iterator = 0; iterator < cards.length(); iterator++){

					JSONObject current = cards.getJSONObject(iterator);

					insertGameplayCardFromYGOPRO(current, db);

					int cardID = current.getInt("id");
					String name = current.getString("name");

					JSONArray sets = null;
					Iterator<Object> setIteraor = null;
					boolean isSets = false;

					try {
						sets = current.getJSONArray("card_sets");
						isSets = true;
					} catch (JSONException e) {

					}

					if (isSets) {
						insertCardSetsForOneCard(sets, name, cardID, db);
					}

				}

				Util.checkForIssuesWithSet(setName, db);
				Util.checkSetCounts(db);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void insertGameplayCardFromYGOPRO(JSONObject current, SQLiteConnection db) throws SQLException {

		Integer wikiID = Util.getIntOrNull(current, "id");
		String name = Util.getStringOrNull(current, "name");
		String type = Util.getStringOrNull(current, "type");
		Integer passcode = Util.getIntOrNull(current, "id");// passcode
		String desc = Util.getStringOrNull(current, "desc");
		String attribute = Util.getStringOrNull(current, "attribute");
		String race = Util.getStringOrNull(current, "race");
		Integer linkval = Util.getIntOrNull(current, "linkval");
		Integer level = Util.getIntOrNull(current, "level");
		Integer scale = Util.getIntOrNull(current, "scale");
		Integer atk = Util.getIntOrNull(current, "atk");
		Integer def = Util.getIntOrNull(current, "def");
		String archetype = Util.getStringOrNull(current, "archetype");

		db.replaceIntoGamePlayCard(wikiID, name, type, passcode, desc, attribute, race, linkval, level,
				scale, atk, def, archetype);
	}

	public static void insertCardSetsForOneCard(JSONArray sets, String name, int wikiID, SQLiteConnection db)
			throws SQLException, JSONException {

		for (int i = 0; i < sets.length(); i++) {

			JSONObject currentSet = sets.getJSONObject(i);

			String set_code = null;
			String set_name = null;
			String set_rarity = null;
			String set_price = null;

			try {
				set_code = currentSet.getString("set_code");
				set_name = currentSet.getString("set_name");
				set_rarity = currentSet.getString("set_rarity");
				set_price = currentSet.getString("set_price");
			} catch (Exception e) {
				System.out.println("issue found on " + name);
				continue;
			}

			set_price = Util.getAdjustedPriceFromRarity(set_rarity, set_price);
			
			set_name = Util.checkForTranslatedSetName(set_name);

			db.replaceIntoCardSet(set_code, set_rarity, set_name, wikiID, set_price, name);

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

				JSONArray array = new JSONArray(inline);

				inline = null;

				for(int iterator = 0; iterator < array.length(); iterator++){
					JSONObject set = array.getJSONObject(iterator);

					String set_name = Util.getStringOrNull(set, "set_name");
					String set_code = Util.getStringOrNull(set, "set_code");
					int num_of_cards = Util.getIntOrNull(set, "num_of_cards");
					String tcg_date = Util.getStringOrNull(set, "tcg_date");
					
					String newSetName = Util.checkForTranslatedSetName(set_name);

					if (!specificSet) {
						db.replaceIntoCardSetMetaData(newSetName, set_code, num_of_cards, tcg_date);
					}
					if (specificSet && set_name.equalsIgnoreCase(setName)) {
						db.replaceIntoCardSetMetaData(newSetName, set_code, num_of_cards, tcg_date);
						return;
					}
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
