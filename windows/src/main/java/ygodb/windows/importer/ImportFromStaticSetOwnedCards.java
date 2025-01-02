package ygodb.windows.importer;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.connection.CsvConnection;
import ygodb.commonlibrary.connection.DatabaseHashMap;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.windows.utility.WindowsUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class ImportFromStaticSetOwnedCards {

	//TODO export from db sets to csv
	//TODO add link from card details view to card name search details
	//TODO add way to view all alt arts
	//TODO add error checking on the column values for this class
	//TODO handle rows like legendary dragon decks with multiple of the same card at different rarites?
	//select distinct altArtPasscode from cardSets where gamePlayCardUUID = "edb75569-b1ac-47a1-9e5b-359a7f7c9cc7" and altArtPasscode is not null

	public static void main(String[] args) throws SQLException, IOException {
		ImportFromStaticSetOwnedCards mainObj = new ImportFromStaticSetOwnedCards();

		SQLiteConnection db = WindowsUtil.getDBInstance();

		if (mainObj.run(db)) {
			YGOLogger.info("Import Complete");
		} else {
			YGOLogger.info("Import Failed");
		}
		db.closeInstance();
	}

	public boolean run(SQLiteConnection db) throws SQLException, IOException {

		YGOLogger.info("Number of sets purchased: ");

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		String setQuantity = reader.readLine();

		int setQuantityInt = 0;

		try {
			setQuantityInt = Integer.parseInt(setQuantity);

			if (setQuantityInt < 1) {
				YGOLogger.error("Invalid Number of sets purchased input");
				return false;
			}
		} catch (Exception e) {
			YGOLogger.error("Invalid Number of sets purchased input");
			return false;
		}

		String filename = "static-set-ownedcards.csv";
		String resourcePath = Const.CSV_IMPORT_FOLDER + filename;
		File inputFile = new File(resourcePath);

		InputStream fileStream = new FileInputStream(inputFile);

		CsvConnection csvConnection = new CsvConnection();

		CSVParser parser = csvConnection.getParser(fileStream, StandardCharsets.UTF_16LE);
		if (parser == null) {
			return false;
		}

		HashMap<String, OwnedCard> map = new HashMap<>();
		int importedCardQuantity = 0;

		for (CSVRecord current : parser) {
			importedCardQuantity += addCSVRecordToImportMap(csvConnection, db, map, current, setQuantityInt);
		}

		parser.close();

		for (OwnedCard card : map.values()) {
			db.insertOrUpdateOwnedCardByUUID(card);
		}

		db.closeInstance();

		YGOLogger.info("Imported " + importedCardQuantity + " cards");
		YGOLogger.info("Total cards: " + db.getCountQuantity() + " + " + db.getCountQuantityManual() + " Manual");

		return true;
	}

	private static int addCSVRecordToImportMap(CsvConnection csvConnection, SQLiteConnection db, HashMap<String, OwnedCard> map,
			CSVRecord current, int setQuantityInt) throws SQLException {

		OwnedCard csvOwnedCard = csvConnection.getStaticSetOwnedCardFromCSV(current, db, setQuantityInt);

		if (csvOwnedCard != null) {

			String key = DatabaseHashMap.getOwnedCardHashMapKey(csvOwnedCard);

			int currentRowQuantity = csvOwnedCard.getQuantity();

			if (map.containsKey(key)) {
				map.get(key).setQuantity(map.get(key).getQuantity() + csvOwnedCard.getQuantity());
			} else {

				List<OwnedCard> ownedRarities = DatabaseHashMap.getExistingOwnedRaritiesForCardFromHashMap(csvOwnedCard, db);

				for (OwnedCard existingCard : ownedRarities) {
					if (existingCard.equals(csvOwnedCard)) {
						csvOwnedCard.setQuantity(csvOwnedCard.getQuantity() + existingCard.getQuantity());
						csvOwnedCard.setUuid(existingCard.getUuid());
						break;
					}
				}

				map.put(key, csvOwnedCard);
			}
			return currentRowQuantity;
		} else {
			YGOLogger.error("Failed to import card" + current.toString());
			return 0;
		}
	}

}
