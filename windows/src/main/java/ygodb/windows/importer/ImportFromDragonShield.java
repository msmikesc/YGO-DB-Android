package ygodb.windows.importer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.windows.connection.CsvConnection;
import ygodb.commonlibrary.connection.DatabaseHashMap;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.utility.Util;
import ygodb.windows.utility.WindowsUtil;

public class ImportFromDragonShield {

	public static void main(String[] args) throws SQLException, IOException {
		ImportFromDragonShield mainObj = new ImportFromDragonShield();
		SQLiteConnection db = WindowsUtil.getDBInstance();
		mainObj.run(db);
		db.closeInstance();
		YGOLogger.info("Import Complete");
	}

	public void run(SQLiteConnection db) throws SQLException, IOException {

		CSVParser parser = CsvConnection.getParserSkipFirstLine(
				"C:\\Users\\Mike\\Documents\\GitHub\\YGO-DB\\YGO-DB\\csv\\all-folders.csv", StandardCharsets.UTF_16LE);

		Iterator<CSVRecord> it = parser.iterator();

		int count = 0;

		while (it.hasNext()) {

			CSVRecord current = it.next();

			OwnedCard card = CsvConnection.getOwnedCardFromDragonShieldCSV(current, db);

			List<OwnedCard> ownedRarities = DatabaseHashMap.getExistingOwnedRaritesForCardFromHashMap(
					card.setNumber, card.priceBought, card.dateBought, card.folderName, card.condition,
					card.editionPrinting, db);

			for (OwnedCard existingCard : ownedRarities) {
				if (Util.doesCardExactlyMatch(card.folderName, card.cardName, card.setCode, card.setNumber,
						card.condition, card.editionPrinting, card.priceBought, card.dateBought, existingCard)) {
					if (card.quantity == existingCard.quantity) {
						// no changes, no need to update
						card = null;
					} else {
						// something to update
						card.uuid = existingCard.uuid;
					}
					break;
				}
			}

			if (card != null) {
				count += card.quantity;
				db.upsertOwnedCardBatch(card);
			}
		}

		parser.close();

		db.closeInstance();

		YGOLogger.info("Imported " + count + " cards");
		YGOLogger.info("Total cards: " + db.getCountQuantity() + " + "
				+ db.getCountQuantityManual() + " Manual");

	}

}
