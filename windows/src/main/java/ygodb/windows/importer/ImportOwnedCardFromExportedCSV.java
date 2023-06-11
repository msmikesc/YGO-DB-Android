package ygodb.windows.importer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import ygodb.commonLibrary.bean.OwnedCard;
import ygodb.commonLibrary.utility.YGOLogger;
import ygodb.windows.connection.CsvConnection;
import ygodb.commonLibrary.connection.DatabaseHashMap;
import ygodb.commonLibrary.connection.SQLiteConnection;
import ygodb.commonLibrary.utility.Util;
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

		CSVParser parser = CsvConnection.getParser(
				"C:\\Users\\Mike\\Documents\\GitHub\\YGO-DB\\YGO-DB\\csv\\rarity-unsure-export.csv",
				StandardCharsets.UTF_16LE);

		Iterator<CSVRecord> it = parser.iterator();
		
		int count = 0;

		while (it.hasNext()) {

			CSVRecord current = it.next();

			OwnedCard card = CsvConnection.getOwnedCardFromExportedCSV(current, db);
			
			List<OwnedCard> ownedRarities = DatabaseHashMap.getExistingOwnedRaritesForCardFromHashMap(card.setNumber, card.priceBought,
					card.dateBought, card.folderName, card.condition, card.editionPrinting, db);

			for (OwnedCard existingCard : ownedRarities) {
				if (Util.doesCardExactlyMatchWithColor(card.folderName, card.cardName, card.setCode, card.setNumber,
						card.condition, card.editionPrinting, card.priceBought, card.dateBought, card.colorVariant,
						existingCard)) {
					// exact match found
					if (existingCard.quantity == card.quantity && existingCard.rarityUnsure == card.rarityUnsure
							&& existingCard.setRarity.equals(card.setRarity)) {
						// nothing to update
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

	}

}
