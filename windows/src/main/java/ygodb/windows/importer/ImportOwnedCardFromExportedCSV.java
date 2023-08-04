package ygodb.windows.importer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.commonlibrary.connection.CsvConnection;
import ygodb.commonlibrary.connection.DatabaseHashMap;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.windows.utility.WindowsUtil;

public class ImportOwnedCardFromExportedCSV {

	public static void main(String[] args) throws SQLException, IOException {
		ImportOwnedCardFromExportedCSV mainObj = new ImportOwnedCardFromExportedCSV();
		SQLiteConnection db = WindowsUtil.getDBInstance();

		mainObj.run(db);
		db.closeInstance();
		YGOLogger.info("Import Complete");
	}

	public void run(SQLiteConnection db) throws SQLException, IOException {

		String filename = "all-export.csv";
		String resourcePath = Const.CSV_EXPORT_FOLDER + filename;

		CsvConnection csvConnection = new CsvConnection();

		CSVParser parser = csvConnection.getParser(resourcePath, StandardCharsets.UTF_16LE);

		Iterator<CSVRecord> it = parser.iterator();

		int count = 0;

		while (it.hasNext()) {

			CSVRecord current = it.next();

			OwnedCard csvOwnedCard = csvConnection.getOwnedCardFromExportedCSV(current, db);

			List<OwnedCard> ownedRarities = DatabaseHashMap.getExistingOwnedRaritiesForCardFromHashMap(csvOwnedCard, db);

			for (OwnedCard existingCard : ownedRarities) {
				if (existingCard.equals(csvOwnedCard)) {
					// exact match found
					if (existingCard.getQuantity() == csvOwnedCard.getQuantity() && existingCard.getRarityUnsure() == csvOwnedCard.getRarityUnsure()) {
						// nothing to update
						csvOwnedCard = null;
					} else {
						// something to update
						csvOwnedCard.setUuid(existingCard.getUuid());
					}
					break;
				}
			}

			if (csvOwnedCard != null) {
				count += csvOwnedCard.getQuantity();
				db.insertOrUpdateOwnedCardByUUID(csvOwnedCard);
			}
		}

		parser.close();

		db.closeInstance();

		YGOLogger.info("Imported " + count + " cards");

	}

}
