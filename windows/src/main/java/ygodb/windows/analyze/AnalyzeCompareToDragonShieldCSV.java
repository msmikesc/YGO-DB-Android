package ygodb.windows.analyze;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import ygodb.commonlibrary.bean.CardSet;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.commonlibrary.connection.CsvConnection;
import ygodb.commonlibrary.connection.DatabaseHashMap;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.Util;
import ygodb.windows.utility.WindowsUtil;

public class AnalyzeCompareToDragonShieldCSV {

	private static final Map<String, String> longRareMap = new HashMap<>();

	static {
		longRareMap.put("SR","Super Rare");
		longRareMap.put("R","Rare");
		longRareMap.put("C","Common");
		longRareMap.put("SP","Common");
		longRareMap.put("UR","Ultra Rare");
		longRareMap.put("PGR","Premium Gold Rare");
		longRareMap.put("PScR","Prismatic Secret Rare");
		longRareMap.put("ScR","Secret Rare");
		longRareMap.put("DUPR","Duel Terminal Ultra Parallel Rare");
		longRareMap.put("DRPR","Duel Terminal Normal Parallel Rare");
		longRareMap.put("UtR","Ultimate Rare");
		longRareMap.put("GUR","Gold Rare");
		longRareMap.put("GScR","Gold Secret Rare");
		longRareMap.put("SSP","Common");
		longRareMap.put("CR","Collector's Rare");
		longRareMap.put("SHR","Shatterfoil Rare");
		longRareMap.put("DNPR","Duel Terminal Normal Parallel Rare");
		longRareMap.put("SFR","Starfoil Rare");
		longRareMap.put("QCScR","Quarter Century Secret Rare");
	}


	public static void main(String[] args) throws SQLException, IOException {
		AnalyzeCompareToDragonShieldCSV mainObj = new AnalyzeCompareToDragonShieldCSV();
		SQLiteConnection db = WindowsUtil.getDBInstance();
		mainObj.run(db);
		db.closeInstance();
		YGOLogger.info("Analyze Complete");
	}

	public void run(SQLiteConnection db) throws SQLException, IOException {

		String filename = "all-folders.csv";
		String resourcePath = Const.CSV_IMPORT_FOLDER + filename;

		CsvConnection csvConnection = new CsvConnection();

		CSVParser parser = csvConnection.getParserSkipFirstLine(resourcePath, StandardCharsets.UTF_16LE);

		Iterator<CSVRecord> it = parser.iterator();

		Map<String, List<OwnedCard>> ownedCardsMap = DatabaseHashMap.getOwnedInstance(db);

		while (it.hasNext()) {

			CSVRecord current = it.next();

			String folder = csvConnection.getStringOrNull(current, Const.FOLDER_NAME_CSV);
			String name = csvConnection.getStringOrNull(current, Const.CARD_NAME_CSV);
			String quantity = csvConnection.getStringOrNull(current, Const.QUANTITY_CSV);
			String setCode = csvConnection.getStringOrNull(current, Const.SET_CODE_CSV);
			String setNumber = csvConnection.getStringOrNull(current, Const.CARD_NUMBER_CSV);
			String condition = csvConnection.getStringOrNull(current, Const.CONDITION_CSV);
			String printing = csvConnection.getStringOrNull(current, Const.PRINTING_CSV);
			String priceBought = Util.normalizePrice(csvConnection.getStringOrNull(current, Const.PRICE_BOUGHT_CSV));
			String dateBought = csvConnection.getStringOrNull(current, Const.DATE_BOUGHT_CSV);
			String setName = csvConnection.getStringOrNull(current, Const.SET_NAME_CSV);

			String rarity = csvConnection.getStringOrNull(current, "Rarity");

			rarity = convertRarityToLongForm(rarity);

			String colorCode = Const.DEFAULT_COLOR_VARIANT;

			if(printing == null || printing.equals("")){
				printing = Const.CARD_PRINTING_UNLIMITED;
			}

			if (printing.equals(Const.CARD_PRINTING_FOIL)) {
				printing = Const.CARD_PRINTING_FIRST_EDITION;
			}

			CardSet setIdentified = new CardSet("", setNumber, name, rarity, setName, colorCode, setCode);
			OwnedCard csvOwnedcard = new OwnedCard(folder, name, quantity, condition, printing, priceBought, dateBought, setIdentified, 0);

			String key = DatabaseHashMap.getOwnedCardHashMapKey(csvOwnedcard);

			List<OwnedCard> existingOwnedCardsList = ownedCardsMap.get(key);

			if (existingOwnedCardsList != null) {

				handleForCSVKey(ownedCardsMap, csvOwnedcard, existingOwnedCardsList);
			}

			if (existingOwnedCardsList == null) {
				// try removing color code
				String newColorCode = setNumber.substring(setNumber.length() - 1);
				String newSetNumber = setNumber.substring(0, setNumber.length() - 1);

				setIdentified = new CardSet("", newSetNumber, name, rarity, setName, newColorCode, setCode);
				csvOwnedcard = new OwnedCard(folder, name, quantity, condition, printing, priceBought, dateBought, setIdentified, 0);

				String newKey = DatabaseHashMap.getOwnedCardHashMapKey(csvOwnedcard);

				existingOwnedCardsList = ownedCardsMap.get(newKey);
				if(existingOwnedCardsList != null){
					handleForCSVKey(ownedCardsMap, csvOwnedcard, existingOwnedCardsList);
				}
			}

			if (existingOwnedCardsList == null) {
				// try adding EN to set number
				String[] brokenSetNumber = setNumber.split("-");

				if(brokenSetNumber.length == 2){
					String newSetNumber = brokenSetNumber[0] + "-EN" + brokenSetNumber[1];

					setIdentified = new CardSet("", newSetNumber, name, rarity, setName, colorCode, setCode);
					csvOwnedcard = new OwnedCard(folder, name, quantity, condition, printing, priceBought, dateBought, setIdentified, 0);

					String newKey = DatabaseHashMap.getOwnedCardHashMapKey(csvOwnedcard);
					existingOwnedCardsList = ownedCardsMap.get(newKey);
					if(existingOwnedCardsList != null){
						handleForCSVKey(ownedCardsMap, csvOwnedcard, existingOwnedCardsList);
					}
				}
			}

			if (existingOwnedCardsList == null) {
				YGOLogger.info("no match in DB found for key : " + key);
			}

		}

		parser.close();

		for (List<OwnedCard> rarityList : ownedCardsMap.values()) {
			for (OwnedCard card : rarityList) {

				if (!card.getFolderName().equals(Const.FOLDER_MANUAL)) {
					YGOLogger.info(
							"Card in DB but not in CSV: " + card.getCardName() + " " + card.getSetNumber() + " " + card.getSetRarity()
									+ " " + card.getColorVariant() + " " + card.getPriceBought() + " " + card.getDateBought());
				}
			}
		}

	}

	public String convertRarityToLongForm(String input){
		String output = longRareMap.get(input);
		if(output == null){
			return input;
		}
		return output;
	}


	private static void handleForCSVKey(Map<String, List<OwnedCard>> ownedCardsMap, OwnedCard csvOwnedcard,
										List<OwnedCard> existingOwnedCardsList) {

		String key = DatabaseHashMap.getOwnedCardHashMapKey(csvOwnedcard);;

		if (existingOwnedCardsList.size() == 1) {
			// exact 1 match

			OwnedCard existingCard = existingOwnedCardsList.get(0);

			if (!csvOwnedcard.getColorVariant().equalsIgnoreCase(existingOwnedCardsList.get(0).getColorVariant())
					&& !Const.setColorVariantUnsupportedDragonShield.contains(existingCard.getSetName())) {
				YGOLogger.info(
						"Color Code Mismatch on: " + existingCard.getCardName() + " " + existingCard.getSetNumber() + " " + existingCard.getSetRarity()
								+ " " + existingCard.getColorVariant() + " " + existingCard.getPriceBought() + " " + existingCard.getDateBought());
			}

			if (existingCard.getQuantity() != csvOwnedcard.getQuantity()) {
				YGOLogger.info(
						"Quantity Mismatch on: " + existingCard.getCardName() + " " + existingCard.getSetNumber() + " " + existingCard.getSetRarity() + " "
								+ existingCard.getColorVariant() + " " + existingCard.getPriceBought() + " " + existingCard.getDateBought());
			}

			if (!existingCard.getSetRarity().equals(csvOwnedcard.getSetRarity())) {
				YGOLogger.info(
						"Rarity Mismatch on: " + existingCard.getCardName() + " " + existingCard.getSetNumber() + " " + existingCard.getSetRarity() + " "
								+ existingCard.getColorVariant() + " " + existingCard.getPriceBought() + " " + existingCard.getDateBought());
			}

			ownedCardsMap.remove(key);
		} else {

			boolean foundMatch = false;

			for (int i = 0; i < existingOwnedCardsList.size(); i++) {

				OwnedCard existingCard = existingOwnedCardsList.get(i);

				csvOwnedcard.setGamePlayCardUUID(existingCard.getGamePlayCardUUID());
				csvOwnedcard.setPasscode(existingCard.getPasscode());

				if (existingCard.equals(csvOwnedcard)) {
					foundMatch = true;
					existingOwnedCardsList.remove(i);

					if (existingCard.getQuantity() != csvOwnedcard.getQuantity()) {
						YGOLogger.info("Quantity Mismatch on: " + existingCard.getCardName() + " " + existingCard.getSetNumber() + " "
								+ existingCard.getSetRarity() + " " + existingCard.getColorVariant() + " " + existingCard.getPriceBought() + " "
								+ existingCard.getDateBought());
					}

					if (existingOwnedCardsList.isEmpty()) {
						ownedCardsMap.remove(key);
					}
					break;

				}
			}

			if (!foundMatch) {
				YGOLogger.info("Unable to find exact match for key: " + key);
			}

		}
	}

}
