package ygodb.windows.analyze;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.commonlibrary.connection.CsvConnection;
import ygodb.commonlibrary.connection.DatabaseHashMap;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.Util;
import ygodb.windows.utility.WindowsUtil;

public class AnalyzeCompareToDragonShieldCSV {

	private static Map<String, String> longRareMap = new HashMap<>();

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

		Map<String, ArrayList<OwnedCard>> databaseList = DatabaseHashMap.getOwnedInstance(db);

		while (it.hasNext()) {

			CSVRecord current = it.next();

			String folder = current.get(Const.FOLDER_NAME_CSV).trim();
			String name = current.get(Const.CARD_NAME_CSV).trim();
			String quantity = current.get(Const.QUANTITY_CSV).trim();
			String setCode = current.get(Const.SET_CODE_CSV).trim();
			String setNumber = current.get(Const.CARD_NUMBER_CSV).trim();
			String condition = current.get(Const.CONDITION_CSV).trim();
			String printing = current.get(Const.PRINTING_CSV).trim();
			String priceBought = Util.normalizePrice(current.get(Const.PRICE_BOUGHT_CSV));
			String dateBought = current.get(Const.DATE_BOUGHT_CSV).trim();
			String setName = current.get(Const.SET_NAME_CSV).trim();

			String rarity = current.get("Rarity").trim();

			rarity = convertRarityToLongForm(rarity);

			String colorCode = Const.DEFAULT_COLOR_VARIANT;

			if(printing == null || printing.equals("")){
				printing = Const.CARD_PRINTING_UNLIMITED;
			}

			if (printing.equals(Const.CARD_PRINTING_FOIL)) {
				printing = Const.CARD_PRINTING_FIRST_EDITION;
			}

			String key = setNumber +":"+ Util.normalizePrice(priceBought) +":"+ dateBought +":"+ folder +":"+ condition +":"+ printing;

			ArrayList<OwnedCard> list = databaseList.get(key);

			if(list != null){
				handleForCSVKey(databaseList, folder, name, quantity, setCode, setNumber, condition, printing, priceBought, dateBought, setName, colorCode, key, list, rarity);
			}

			if (list == null) {
				// try removing color code
				String newColorCode = setNumber.substring(setNumber.length() - 1);
				String newSetNumber = setNumber.substring(0, setNumber.length() - 1);

				String newKey = newSetNumber +":"+ Util.normalizePrice(priceBought) +":"+ dateBought +":"+ folder +":"+ condition +":"+ printing;

				list = databaseList.get(newKey);
				if(list != null){
					handleForCSVKey(databaseList, folder, name, quantity, setCode, newSetNumber, condition, printing, priceBought, dateBought, setName, newColorCode, newKey, list, rarity);
				}
			}

			if (list == null) {
				// try adding EN to set number
				String[] brokenSetNumber = setNumber.split("-");

				if(brokenSetNumber.length == 2){
					String newSetNumber = brokenSetNumber[0] + "-EN" + brokenSetNumber[1];
					String newKey = setNumber +":"+ Util.normalizePrice(priceBought) +":"+ dateBought +":"+ folder +":"+ condition +":"+ printing;
					list = databaseList.get(newKey);
					if(list != null){
						handleForCSVKey(databaseList, folder, name, quantity, setCode, newSetNumber, condition, printing, priceBought, dateBought, setName, colorCode, newKey, list, rarity);
					}
				}
			}

			if (list == null) {
				YGOLogger.info("no match in DB found for key : " + key);
			}

		}

		parser.close();

		for (ArrayList<OwnedCard> rarityList : databaseList.values()) {
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



	private static void handleForCSVKey(Map<String, ArrayList<OwnedCard>> databaseList, String folder, String name, String quantity, String setCode, String setNumber, String condition, String printing, String priceBought, String dateBought, String setName, String colorCode, String key, ArrayList<OwnedCard> list, String rarity) {

		if (list.size() == 1) {
			// exact 1 match

			OwnedCard card = list.get(0);

			if (!colorCode.equalsIgnoreCase(list.get(0).getColorVariant())
					&& !Const.setColorVariantUnsupportedDragonShield.contains(card.getSetName())) {
				YGOLogger.info(
						"Color Code Mismatch on: " + card.getCardName() + " " + card.getSetNumber() + " " + card.getSetRarity()
								+ " " + card.getColorVariant() + " " + card.getPriceBought() + " " + card.getDateBought());
			}

			if (card.getQuantity() != Integer.parseInt(quantity)) {
				YGOLogger.info(
						"Quantity Mismatch on: " + card.getCardName() + " " + card.getSetNumber() + " " + card.getSetRarity() + " "
								+ card.getColorVariant() + " " + card.getPriceBought() + " " + card.getDateBought());
			}

			if (!card.getSetRarity().equals(rarity)) {
				YGOLogger.info(
						"Rarity Mismatch on: " + card.getCardName() + " " + card.getSetNumber() + " " + card.getSetRarity() + " "
								+ card.getColorVariant() + " " + card.getPriceBought() + " " + card.getDateBought());
			}

			databaseList.remove(key);
		} else {

			boolean foundMatch = false;

			for (int i = 0; i < list.size(); i++) {

				OwnedCard card = list.get(i);

				if (Util.doesCardExactlyMatchWithColor(folder, name, setCode, setNumber, condition, printing,
						priceBought, dateBought, colorCode, card.getSetRarity(), setName, card.getPasscode(),
						card.getGamePlayCardUUID(), card)) {
					foundMatch = true;
					list.remove(i);

					if (card.getQuantity() != Integer.parseInt(quantity)) {
						YGOLogger.info("Quantity Mismatch on: " + card.getCardName() + " " + card.getSetNumber() + " "
								+ card.getSetRarity() + " " + card.getColorVariant() + " " + card.getPriceBought() + " "
								+ card.getDateBought());
					}

					if (!card.getSetRarity().equals(rarity)) {
						YGOLogger.info(
								"Rarity Mismatch on: " + card.getCardName() + " " + card.getSetNumber() + " " + card.getSetRarity() + " "
										+ card.getColorVariant() + " " + card.getPriceBought() + " " + card.getDateBought());
					}

					if (list.isEmpty()) {
						databaseList.remove(key);
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
