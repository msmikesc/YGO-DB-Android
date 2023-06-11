package ygodb.windows.importer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
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

public class ImportFromTCGPlayer {

	public static void main(String[] args) throws SQLException, IOException {
		ImportFromTCGPlayer mainObj = new ImportFromTCGPlayer();

		SQLiteConnection db = WindowsUtil.getDBInstance();

		mainObj.run(db);
		db.closeInstance();
		YGOLogger.info("Import Complete");
	}

	public void run(SQLiteConnection db) throws SQLException, IOException {

		CSVParser parser = CsvConnection.getParser(
				"C:\\Users\\Mike\\Documents\\GitHub\\YGO-DB\\YGO-DB\\csv\\TCGPlayer.csv", StandardCharsets.UTF_16LE);
		
		Iterator<CSVRecord> it = parser.iterator();

		HashMap<String, OwnedCard> map = new HashMap<>();

		int count = 0;

		while (it.hasNext()) {

			CSVRecord current = it.next();

			OwnedCard card = CsvConnection.getOwnedCardFromTCGPlayerCSV(current, db);

			if (card != null) {

				count += card.quantity;

				String key = card.setNumber + Util.normalizePrice(card.priceBought) + card.dateBought + card.folderName
						+ card.condition + card.editionPrinting;

				if (map.containsKey(key)) {
					map.get(key).quantity += card.quantity;
				} else {

					List<OwnedCard> ownedRarities = DatabaseHashMap.getExistingOwnedRaritesForCardFromHashMap(
							card.setNumber, card.priceBought, card.dateBought, card.folderName, card.condition,
							card.editionPrinting, db);

					for (OwnedCard existingCard : ownedRarities) {
						if (Util.doesCardExactlyMatchWithColor(card.folderName, card.cardName, card.setCode,
								card.setNumber, card.condition, card.editionPrinting, card.priceBought, card.dateBought,
								card.colorVariant, existingCard)) {
							card.quantity += existingCard.quantity;
							card.uuid = existingCard.uuid;
							card.gamePlayCardUUID = existingCard.gamePlayCardUUID;
							break;
						}
					}

					map.put(key, card);
				}

			}
		}

		for (OwnedCard card : map.values()) {
			db.upsertOwnedCardBatch(card);
		}

		parser.close();

		db.closeInstance();

		YGOLogger.info("Imported " + count + " cards");
		YGOLogger.info("Total cards: "+db.getCountQuantity() + " + " + db.getCountQuantityManual() + " Manual");

	}

}
