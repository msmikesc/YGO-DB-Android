package ygodb.windows.analyze;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
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

		CSVParser parser = CsvConnection.getParserSkipFirstLine(resourcePath, StandardCharsets.UTF_16LE);

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

			String colorCode = Const.DEFAULT_COLOR_VARIANT;

			if (printing.equals(Const.CARD_PRINTING_FOIL)) {
				printing = Const.CARD_PRINTING_FIRST_EDITION;
			}

			String key = setNumber + Util.normalizePrice(priceBought) + dateBought + folder + condition + printing;

			ArrayList<OwnedCard> list = databaseList.get(key);

			if (list == null) {
				// try removing color code
				colorCode = setNumber.substring(setNumber.length() - 1);
				setNumber = setNumber.substring(0, setNumber.length() - 1);

				key = setNumber + Util.normalizePrice(priceBought) + dateBought + folder + condition + printing;

				list = databaseList.get(key);
			}

			if (list == null) {
				YGOLogger.info("no match in DB found forkey : " + key);
			} else if (list.size() == 1) {
				// exact 1 match

				OwnedCard card = list.get(0);

				if (!colorCode.equalsIgnoreCase(list.get(0).colorVariant)
						&& !Const.setColorVariantUnsupportedDragonShield.contains(card.setName)) {
					YGOLogger.info(
							"Color Code Mismatch on: " + card.cardName + " " + card.setNumber + " " + card.setRarity
									+ " " + card.colorVariant + " " + card.priceBought + " " + card.dateBought);
				}

				if (card.quantity != Integer.parseInt(quantity)) {
					YGOLogger.info(
							"Quantity Mismatch on: " + card.cardName + " " + card.setNumber + " " + card.setRarity + " "
									+ card.colorVariant + " " + card.priceBought + " " + card.dateBought);
				}

				databaseList.remove(key);
			} else {

				boolean foundMatch = false;

				for (int i = 0; i < list.size(); i++) {

					OwnedCard card = list.get(i);

					if (Util.doesCardExactlyMatchWithColor(folder, name, setCode, setNumber, condition, printing,
							priceBought, dateBought, colorCode, card)) {
						foundMatch = true;
						list.remove(i);

						if (card.quantity != Integer.parseInt(quantity)) {
							YGOLogger.info("Quantity Mismatch on: " + card.cardName + " " + card.setNumber + " "
									+ card.setRarity + " " + card.colorVariant + " " + card.priceBought + " "
									+ card.dateBought);
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

		parser.close();

		for (ArrayList<OwnedCard> rarityList : databaseList.values()) {
			for (OwnedCard card : rarityList) {

				if (!card.folderName.equals(Const.FOLDER_MANUAL)) {
					YGOLogger.info(
							"Card in DB but not in CSV: " + card.cardName + " " + card.setNumber + " " + card.setRarity
									+ " " + card.colorVariant + " " + card.priceBought + " " + card.dateBought);
				}
			}
		}

	}

}
