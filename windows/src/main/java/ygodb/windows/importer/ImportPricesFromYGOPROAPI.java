package ygodb.windows.importer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ygodb.commonlibrary.bean.CardSet;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.Util;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.windows.utility.WindowsUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ImportPricesFromYGOPROAPI {

	private static final String OPEN = "\"";
	private static final String CLOSE = "\",";
	private static final String SEP = "\",\"";

	private final HashMap<String, List<String>> nameUpdateMap = new HashMap<>();

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
					YGOLogger.debug("Possibly need to handle set name issue count: " + nameUpdateMap.get(setName).size() + " " + setName);

					for (int j = 0; j < nameUpdateMap.get(setName).size(); j++) {
						YGOLogger.debug(nameUpdateMap.get(setName).get(j));
					}

				}

			}
		} catch (Exception e) {
			YGOLogger.logException(e);
		}
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
		String setCode = null;
		String setName = null;
		String setRarity = null;
		String setPrice = null;
		String cardEdition = null;

		try {
			setCode = Util.getStringOrNull(setNode, Const.YGOPRO_SET_CODE);
			setName = Util.getStringOrNull(setNode, Const.YGOPRO_SET_NAME);
			setRarity = Util.getStringOrNull(setNode, Const.YGOPRO_SET_RARITY);
			setPrice = Util.getStringOrNull(setNode, Const.YGOPRO_SET_PRICE);
			cardEdition = Util.getStringOrNull(setNode,Const.YGOPRO_CARD_EDITION);

			//String set_rarity_code = Util.getStringOrNull(setNode,"set_rarity_code");
			//String set_url = Util.getStringOrNull(setNode,"set_url");
		} catch (Exception e) {
			YGOLogger.info("issue found on " + cardName);
			return;
		}
		if(cardEdition == null){
			cardEdition = "";
		}

		boolean isFirstEdition = cardEdition.contains(Const.CARD_PRINTING_CONTAINS_FIRST);

		setRarity = Util.checkForTranslatedRarity(setRarity);
		setName = Util.checkForTranslatedSetName(setName);
		setCode = Util.checkForTranslatedSetNumber(setCode);

		List<String> translatedList = Util.checkForTranslatedQuadKey(cardName, setCode, setRarity, setName);
		cardName = translatedList.get(0);
		setCode = translatedList.get(1);
		setRarity = translatedList.get(2);
		setName = translatedList.get(3);

		setPrice = Util.normalizePrice(setPrice);

		if (setPrice != null && !setPrice.equals(Const.ZERO_PRICE_STRING)) {
			int rowsUpdated = updatePriceUsingMultipleStrategies(cardName, db, setCode, setName, setRarity, setPrice, isFirstEdition);
			if (rowsUpdated != 1) {
				YGOLogger.info(OPEN + setCode + SEP + cardName + SEP + setRarity +
						SEP + setName + CLOSE + setPrice + "," + rowsUpdated + " rows updated");
			}
		}
	}

	private int updatePriceUsingMultipleStrategies(String cardName, SQLiteConnection db, String setCode, String setName,
												   String setRarity, String setPrice, boolean isFirstEdition) throws SQLException {
		int rowsUpdated = db.updateCardSetPriceWithCardAndSetName(setCode, setRarity, setPrice, setName, cardName, isFirstEdition);
		if (rowsUpdated > 0) {
			//All input was correct, no need to log
			return rowsUpdated;
		}

		rowsUpdated = db.updateCardSetPriceWithSetName(setCode, setRarity, setPrice, setName, isFirstEdition);
		if (rowsUpdated > 0) {
			//card name was incorrect
			YGOLogger.debug("Card name mismatch from price API:" + OPEN + setCode + SEP + cardName + SEP + setRarity +
					SEP + setName + CLOSE + setPrice);
			return rowsUpdated;
		}

		rowsUpdated = db.updateCardSetPriceWithCardName(setCode, setRarity, setPrice, cardName, isFirstEdition);
		if (rowsUpdated > 0) {
			//set name was incorrect
			List<String> setNamesList = nameUpdateMap.computeIfAbsent(setName, k -> new ArrayList<>());
			setNamesList.add(cardName + " " + setCode);
			return rowsUpdated;
		}

		rowsUpdated = db.updateCardSetPrice(setCode, setRarity, setPrice, isFirstEdition);
		if (rowsUpdated > 0) {
			//Card set name and card name was inaccurate
			YGOLogger.debug("Card name mismatch from price API:" + OPEN + setCode + SEP + cardName + SEP + setRarity +
					SEP + setName + CLOSE + setPrice);
			List<String> setNamesList = nameUpdateMap.computeIfAbsent(setName, k -> new ArrayList<>());
			setNamesList.add(cardName + " " + setCode);
			return rowsUpdated;
		}

		ArrayList<CardSet> list = db.getAllCardSetsOfCardBySetNumber(setCode);
		if (list.size() == 1) {
			rowsUpdated = db.updateCardSetPrice(setCode, setPrice, isFirstEdition);
			if (rowsUpdated > 0) {
				//card rarity was inaccurate and only one option, so assume it is right
				YGOLogger.debug("Card rarity mismatch from price API:" + OPEN + setCode + SEP + cardName + SEP + setRarity +
						SEP + setName + CLOSE + setPrice);
				return rowsUpdated;
			}
		}
		//multiple options or zero are possible for setCode, so don't update anything
		return 0;
	}
}
