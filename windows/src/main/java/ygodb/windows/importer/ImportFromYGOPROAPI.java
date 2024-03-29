package ygodb.windows.importer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ygodb.commonlibrary.bean.GamePlayCard;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.bean.SetMetaData;
import ygodb.commonlibrary.connection.DatabaseHashMap;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.ApiUtil;
import ygodb.commonlibrary.utility.Util;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.windows.utility.WindowsUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ImportFromYGOPROAPI {

	public static void main(String[] args) throws SQLException, IOException, InterruptedException {

		String setName = "25th Anniversary Ultimate Kaiba Set";
		boolean importCardImages = false;

		ImportFromYGOPROAPI mainObj = new ImportFromYGOPROAPI();
		SQLiteConnection db = WindowsUtil.getDBInstance();

		boolean successful = mainObj.run(db, setName);
		if (!successful) {
			YGOLogger.info("Import Failed");
		} else {
			YGOLogger.info("Set Data Import Finished");

			if (importCardImages) {
				DownloadCardImagesBySet downloadCardImagesBySet = new DownloadCardImagesBySet();

				successful = downloadCardImagesBySet.run(db, setName);
				if (!successful) {
					YGOLogger.info("Images Import Failed");
				} else {
					YGOLogger.info("Images Import Finished");
				}
			}
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

				String inline = ApiUtil.getApiResponseFromURL(url);

				ObjectMapper objectMapper = new ObjectMapper();
				JsonNode jsonNode = objectMapper.readTree(inline);

				YGOLogger.info("Finished reading from API");

				JsonNode cards = jsonNode.get(Const.YGOPRO_TOP_LEVEL_DATA);

				List<OwnedCard> ownedCardsToCheck = db.getAllOwnedCardsWithoutPasscode();

				for (JsonNode currentGamePlayCard : cards) {

					GamePlayCard inserted = ApiUtil.replaceIntoGameplayCardFromYGOPRO(currentGamePlayCard, ownedCardsToCheck, db);
					JsonNode setListNode = currentGamePlayCard.get(Const.YGOPRO_CARD_SETS);

					if (setListNode != null) {
						ApiUtil.insertOrIgnoreCardSetsForOneCard(setListNode, inserted.getCardName(), inserted.getGamePlayCardUUID(), db);
					}

				}

				DatabaseHashMap.closeRaritiesInstance();

				Util.checkForIssuesWithSet(setName, db);
				Util.checkSetCounts(db);

			}
		} catch (Exception e) {
			YGOLogger.logException(e);
		}
		return true;
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
				String inline = ApiUtil.getApiResponseFromURL(url);

				ObjectMapper objectMapper = new ObjectMapper();
				JsonNode jsonNode = objectMapper.readTree(inline);

				List<SetMetaData> list = db.getAllSetMetaDataFromSetData();
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

	private static void handleSingleSetNode(String inputSetName, SQLiteConnection db, List<String> dbSetNames, JsonNode setNode)
			throws SQLException {

		boolean isSpecificSet = inputSetName != null && !inputSetName.isBlank();
		String currentSetName = ApiUtil.getStringOrNull(setNode, Const.YGOPRO_SET_NAME);
		String setPrefix = ApiUtil.getStringOrNull(setNode, Const.YGOPRO_SET_CODE);
		int numOfCards = ApiUtil.getIntOrNegativeOne(setNode, Const.YGOPRO_TOTAL_CARDS_IN_SET);
		String tcgDate = ApiUtil.getStringOrNull(setNode, Const.YGOPRO_TCG_RELEASE_DATE);

		String newSetName = Util.checkForTranslatedSetName(currentSetName);

		if (!dbSetNames.contains(newSetName) && (newSetName == null || !newSetName.equalsIgnoreCase(inputSetName)) &&
				(!Const.IGNORED_MISSING_SETS.contains(newSetName))) {
			YGOLogger.info("Missing Set: " + newSetName);
		}

		if (!isSpecificSet) {
			List<SetMetaData> setMetaDataList = db.getSetMetaDataFromSetName(newSetName);

			if (setMetaDataList.isEmpty()) {
				db.replaceIntoCardSetMetaData(newSetName, setPrefix, numOfCards, tcgDate);
			}
		}
		if (isSpecificSet && inputSetName.equalsIgnoreCase(newSetName)) {
			List<SetMetaData> setMetaDataList = db.getSetMetaDataFromSetName(newSetName);

			if (setMetaDataList.isEmpty()) {
				db.replaceIntoCardSetMetaData(newSetName, setPrefix, numOfCards, tcgDate);
			}
		}
	}
}
