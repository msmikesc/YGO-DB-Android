package ygodb.windows.importer;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.bean.ReadCSVRecord;
import ygodb.commonlibrary.connection.DatabaseHashMap;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.Util;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.windows.connection.CsvConnection;
import ygodb.windows.utility.WindowsUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ImportFromTCGPlayer {

	public static void main(String[] args) throws SQLException, IOException {
		ImportFromTCGPlayer mainObj = new ImportFromTCGPlayer();

		SQLiteConnection db = WindowsUtil.getDBInstance();

		if(mainObj.run(db)) {
			YGOLogger.info("Import Complete");
		}
		else{
			YGOLogger.info("Import Failed");
		}
		db.closeInstance();
	}

	public boolean run(SQLiteConnection db) throws SQLException, IOException {

		String filename = "TCGPlayer.csv";

		String resourcePath = "import/" + filename;

		String tempResourcePath = "import/temp" + filename;

		File inputFile = new File(resourcePath);

		boolean fileIsNotLocked = inputFile.renameTo(inputFile);

		if(!fileIsNotLocked){
			YGOLogger.error("Unable to acquire exclusive access to input file");
			return false;
		}

		InputStream fileStream = new FileInputStream(inputFile);

		CSVParser parser = CsvConnection.getParser(fileStream, StandardCharsets.UTF_16LE);
		
		Iterator<CSVRecord> it = parser.iterator();

		HashMap<String, OwnedCard> map = new HashMap<>();

		int count = 0;

		List<ReadCSVRecord> readCSVRecords = new ArrayList<>();

		while (it.hasNext()) {

			CSVRecord current = it.next();

			String importTime = CsvConnection.getStringOrNull(current, Const.TCGPLAYER_IMPORT_TIME);

			if(importTime != null){
				//card is already imported
				ReadCSVRecord currentRead;
				try {
					currentRead = new ReadCSVRecord(current, importTime);
				} catch (ParseException e) {
					YGOLogger.error("ParseException reading imported time:" + importTime +":" + e.getLocalizedMessage());
					currentRead = new ReadCSVRecord(current, new Date());
				}
				readCSVRecords.add(currentRead);

				continue;
			}
			else{
				readCSVRecords.add(new ReadCSVRecord(current, new Date()));
			}

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
			else{
				YGOLogger.error("Failed to import card" + current.toString());
			}
		}

		parser.close();

		File tempFile = new File(tempResourcePath);

		CSVPrinter outfile = CsvConnection.getTCGPlayerOutputFile(tempFile.getPath());

		for(ReadCSVRecord current : readCSVRecords){
			CsvConnection.writeTCGPlayerRecordToCSV(outfile, current);
		}

		outfile.close();

		try {

			Files.delete(inputFile.toPath());

			boolean succeeded = tempFile.renameTo(inputFile);

			if (!succeeded) {
				YGOLogger.error("Failed to rename temp file");
			}


		} catch (Exception e) {
			YGOLogger.error("Failed to replace TCGPlayer.csv with temp file:" + e.getLocalizedMessage());
		}

		for (OwnedCard card : map.values()) {
			db.upsertOwnedCardBatch(card);
		}

		db.closeInstance();

		YGOLogger.info("Imported " + count + " cards");
		YGOLogger.info("Total cards: "+db.getCountQuantity() + " + " + db.getCountQuantityManual() + " Manual");

		return true;
	}

}
