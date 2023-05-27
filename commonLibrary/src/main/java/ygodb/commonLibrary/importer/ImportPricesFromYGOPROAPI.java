package ygodb.commonLibrary.importer;

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

import ygodb.commonLibrary.bean.CardSet;
import ygodb.commonLibrary.connection.SQLiteConnection;
import ygodb.commonLibrary.constant.Const;
import ygodb.commonLibrary.utility.Util;

public class ImportPricesFromYGOPROAPI {
	
	HashMap<String, List<String>> NameUpdateMap = new HashMap<>();

	public void run(SQLiteConnection db) throws SQLException, IOException {

		String setAPI = "https://db.ygoprodeck.com/api/v7/cardinfo.php?tcgplayer_data=true";

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

				String inline = Util.getApiResponseFromURL(url);

				ObjectMapper objectMapper = new ObjectMapper();
				JsonNode jsonNode = objectMapper.readTree(inline);

				System.out.println("Finished reading from API");

				JsonNode cards = jsonNode.get(Const.YGOPRO_TOP_LEVEL_DATA);

				for (JsonNode current : cards) {

					String name = Util.getStringOrNull(current, Const.YGOPRO_CARD_NAME);

					JsonNode sets = null;
					Iterator<JsonNode> setIteraor = null;

					sets = current.get(Const.YGOPRO_CARD_SETS);


					if (sets != null) {
						setIteraor = sets.iterator();
						insertCardSetsForOneCard(setIteraor, name, db);
					}

				}
				
				List<String> namesList = new ArrayList<>(NameUpdateMap.keySet());

				for (String setName : namesList) {
					System.out.println("Possibly need to handle set name issue count: " + NameUpdateMap.get(setName).size() + " " + setName);

					for (int j = 0; j < NameUpdateMap.get(setName).size(); j++) {
						System.out.println(NameUpdateMap.get(setName).get(j));
					}

				}

			}
		} catch (Exception e) {
			e.printStackTrace();
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
				System.out.println("issue found on " + name);
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

						List<String> setNamesList = NameUpdateMap.computeIfAbsent(setName, k -> new ArrayList<>());

						setNamesList.add(name +" "+ setCode);
					}
				}
				
				if(updated != 1) {
					System.out.println("\"" +setCode +"\",\""+name + "\",\"" + setRarity + "\",\"" + setName + "\"," + setPrice +","+ updated + " rows updated");
				}
			}
		}
	}
}
