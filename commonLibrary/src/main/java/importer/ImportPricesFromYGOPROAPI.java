package importer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import bean.CardSet;
import connection.SQLiteConnection;
import connection.Util;

public class ImportPricesFromYGOPROAPI {
	
	HashMap<String, List<String>> NameUpdateMap = new HashMap<String, List<String>>();

	public void run(SQLiteConnection db) throws SQLException, IOException {

		//String setName = "OTS Tournament Pack 21";

		//setName = setName.trim();

		String setAPI = "https://db.ygoprodeck.com/api/v7/cardinfo.php?tcgplayer_data=true";

		//String apiURL = setAPI + URLEncoder.encode(setName);

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

				while (keyset.hasNext()) {

					JsonNode current = keyset.next();

					int cardID = current.get("id").asInt();
					String name = current.get("name").asText();

					JsonNode sets = null;
					Iterator<JsonNode> setIteraor = null;

					sets = current.get("card_sets");


					if (sets != null) {
						setIteraor = sets.iterator();
						insertCardSetsForOneCard(setIteraor, name, cardID, db);
					}

				}
				
				List<String> namesList = new ArrayList<String>(NameUpdateMap.keySet());
				
				for(int i = 0; i < namesList.size(); i++) {
					String setName = namesList.get(i);
					System.out.println("Possibly need to handle set name issue count: " + NameUpdateMap.get(setName).size() + " " + setName );
					/*
					for(int j = 0; j < NameUpdateMap.get(setName).size(); j++) {
						System.out.println(NameUpdateMap.get(setName).get(j));
					}
					*/
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void insertCardSetsForOneCard(Iterator<JsonNode> setIteraor, String name, int wikiID, SQLiteConnection db)
			throws SQLException {
		
		while (setIteraor.hasNext()) {

			JsonNode currentSet = setIteraor.next();

			String set_code = null;
			String set_name = null;
			String set_rarity = null;
			String set_price = null;

			try {
				set_code = currentSet.get("set_code").asText();
				set_name = currentSet.get("set_name").asText();
				set_rarity = currentSet.get("set_rarity").asText();
				set_price = currentSet.get("set_price").asText();
				//set_rarity_code = currentSet.get("set_rarity_code").asText();
				//set_edition = currentSet.get("set_edition").asText();
				//set_url = currentSet.get("set_url").asText();
			} catch (Exception e) {
				System.out.println("issue found on " + name);
				continue;
			}

			set_rarity = Util.checkForTranslatedRarity(set_rarity);
			
			set_name = Util.checkForTranslatedSetName(set_name);
			
			set_price = Util.normalizePrice(set_price);
			
			if(!set_price.equals("0.00")){
				int updated = db.updateCardSetPriceWithSetName(set_code, set_rarity, set_price, set_name);
				
				if(updated == 0) {
				
					updated = db.updateCardSetPrice(set_code, set_rarity, set_price);
					
					if(updated == 0) {
						ArrayList<CardSet> list = db.getAllCardSetsOfCardBySetNumber(set_code);
						
						if(list.size() == 1) {
							updated = db.updateCardSetPrice(set_code, set_price);
						}
						
					}
					else {
						
						List<String> setNamesList = NameUpdateMap.get(set_name);
						
						if(setNamesList == null) {
							setNamesList = new ArrayList<String>();
							NameUpdateMap.put(set_name, setNamesList);
						}
						setNamesList.add(name);
					}
				}
				
				if(updated != 1) {
					System.out.println(updated + " rows updated for: "+name+":"+set_code + ":" + set_rarity + ":" + set_price+":"+set_name);
				}
			}
		}
	}
}
