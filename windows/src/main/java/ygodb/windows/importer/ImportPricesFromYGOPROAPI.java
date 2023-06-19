package ygodb.windows.importer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ygodb.commonlibrary.bean.CardSet;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.Util;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.windows.utility.WindowsUtil;

public class ImportPricesFromYGOPROAPI {
	
	HashMap<String, List<String>> nameUpdateMap = new HashMap<>();

	public static void main(String[] args) throws SQLException, IOException {
		ImportPricesFromYGOPROAPI mainObj = new ImportPricesFromYGOPROAPI();

		SQLiteConnection db = WindowsUtil.getDBInstance();

		mainObj.run(db);
		db.closeInstance();
		YGOLogger.info("Import Finished");
	}

	public void run(SQLiteConnection db) throws SQLException, IOException {

		String setAPI = "https://db.ygoprodeck.com/api/v7/cardinfo.php?tcgplayer_data=true";

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

				YGOLogger.info("Finished reading from API");

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
					YGOLogger.info("Possibly need to handle set name issue count: " + nameUpdateMap.get(setName).size() + " " + setName);

					for (int j = 0; j < nameUpdateMap.get(setName).size(); j++) {
						YGOLogger.debug(nameUpdateMap.get(setName).get(j));
					}

				}

			}
		} catch (Exception e) {
			YGOLogger.logException(e);
		}
	}

	public void insertCardSetsForOneCard(Iterator<JsonNode> setIterator, String name, SQLiteConnection db)
			throws SQLException {
		
		while (setIterator.hasNext()) {

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
				//set_rarity_code = Util.getStringOrNull(currentSet,"set_rarity_code");
				//set_edition = Util.getStringOrNull(currentSet,"set_edition");
				//set_url = Util.getStringOrNull(currentSet,"set_url");
			} catch (Exception e) {
				YGOLogger.info("issue found on " + name);
				continue;
			}

			name = WindowsUtil.checkForTranslatedCardName(name);
			setRarity = WindowsUtil.checkForTranslatedRarity(setRarity);
			setName = WindowsUtil.checkForTranslatedSetName(setName);
			setCode = WindowsUtil.checkForTranslatedSetNumber(setCode);

			List<String> translatedList = WindowsUtil.checkForTranslatedQuadKey(name, setCode, setRarity, setName);
			name = translatedList.get(0);
			setCode = translatedList.get(1);
			setRarity = translatedList.get(2);
			setName = translatedList.get(3);
			
			setPrice = Util.normalizePrice(setPrice);
			
			if(setPrice != null && !setPrice.equals(Const.ZERO_PRICE_STRING)){
				int updated = db.updateCardSetPriceWithSetName(setCode, setRarity, setPrice, setName);
				
				if(updated == 0) {
				
					updated = db.updateCardSetPrice(setCode, setRarity, setPrice);
					
					if(updated == 0) {
						ArrayList<CardSet> list = db.getAllCardSetsOfCardBySetNumber(setCode);
						
						if(list.size() == 1) {
							updated = db.updateCardSetPrice(setCode, setPrice);
						}
						
					}
					else {

						List<String> setNamesList = nameUpdateMap.computeIfAbsent(setName, k -> new ArrayList<>());

						setNamesList.add(name +" "+ setCode);
					}
				}
				
				if(updated != 1) {
					YGOLogger.info("\"" +setCode +"\",\""+name + "\",\"" + setRarity + "\",\"" + setName + "\"," + setPrice +","+ updated + " rows updated");
				}
			}
		}
	}
}
