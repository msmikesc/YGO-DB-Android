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
import ygodb.commonlibrary.connection.CsvConnection;
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
import java.util.List;

public class ImportFromTCGPlayer {

	public static void main(String[] args) throws SQLException, IOException {
		ImportFromTCGPlayer mainObj = new ImportFromTCGPlayer();

		SQLiteConnection db = WindowsUtil.getDBInstance();

		if (mainObj.run(db)) {
			YGOLogger.info("Import Complete");
		} else {
			YGOLogger.info("Import Failed");
		}
		db.closeInstance();
	}

	public boolean run(SQLiteConnection db) throws SQLException, IOException {

		String filename = "TCGPlayer.csv";
		String resourcePath = Const.CSV_IMPORT_FOLDER + filename;
		String tempResourcePath = Const.CSV_IMPORT_FOLDER + "temp" + filename;
		File inputFile = new File(resourcePath);
		HashMap<String, OwnedCard> map = new HashMap<>();
		List<ReadCSVRecord> readCSVRecords = new ArrayList<>();
		int importedCardQuantity = 0;

		CSVParser parser = getCSVFileWithWriteAccess(inputFile);
		if (parser == null) {
			return false;
		}

		for (CSVRecord current : parser) {

			String importTime = CsvConnection.getStringOrNull(current, Const.TCGPLAYER_IMPORT_TIME);

			if (importTime != null) {
				//card is already imported
				ReadCSVRecord currentRead;
				try {
					currentRead = new ReadCSVRecord(current, importTime);
				} catch (ParseException e) {
					YGOLogger.error("ParseException reading imported time:" + importTime + ":" + e.getLocalizedMessage());
					currentRead = new ReadCSVRecord(current, new Date());
				}
				readCSVRecords.add(currentRead);
			} else {
				readCSVRecords.add(new ReadCSVRecord(current, new Date()));
				importedCardQuantity += addCSVRecordToImportMap(db, map, current);
			}

		}

		parser.close();

		overwriteInputFileWithUpdates(tempResourcePath, inputFile, readCSVRecords);

		for (OwnedCard card : map.values()) {
			db.upsertOwnedCardBatch(card);
		}

		db.closeInstance();

		YGOLogger.info("Imported " + importedCardQuantity + " cards");
		YGOLogger.info("Total cards: " + db.getCountQuantity() + " + " + db.getCountQuantityManual() + " Manual");

		return true;
	}

	private static CSVParser getCSVFileWithWriteAccess(File inputFile) throws IOException {
		boolean fileIsNotLocked = inputFile.renameTo(inputFile);

		if (!fileIsNotLocked) {
			YGOLogger.error("Unable to acquire exclusive access to input file");
			return null;
		}

		InputStream fileStream = new FileInputStream(inputFile);

		return CsvConnection.getParser(fileStream, StandardCharsets.UTF_16LE);
	}

	private static void overwriteInputFileWithUpdates(String tempResourcePath, File inputFile, List<ReadCSVRecord> readCSVRecords) throws IOException {
		File tempFile = new File(tempResourcePath);

		CSVPrinter outfile = CsvConnection.getTCGPlayerOutputFile(tempFile.getPath());

		for (ReadCSVRecord current : readCSVRecords) {
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
	}

	private static int addCSVRecordToImportMap(SQLiteConnection db, HashMap<String, OwnedCard> map, CSVRecord current) throws SQLException {

		OwnedCard card = CsvConnection.getOwnedCardFromTCGPlayerCSV(current, db);

		if (card != null) {

			String key = card.getSetNumber() + Util.normalizePrice(card.getPriceBought()) + card.getDateBought() + card.getFolderName()
					+ card.getCondition() + card.getEditionPrinting();

			int currentRowQuantity = card.getQuantity();

			if (map.containsKey(key)) {
				map.get(key).setQuantity(map.get(key).getQuantity() + card.getQuantity());
			} else {

				List<OwnedCard> ownedRarities = DatabaseHashMap.getExistingOwnedRaritiesForCardFromHashMap(
						card.getSetNumber(), card.getPriceBought(), card.getDateBought(), card.getFolderName(), card.getCondition(),
						card.getEditionPrinting(), db);

				for (OwnedCard existingCard : ownedRarities) {
					if (Util.doesCardExactlyMatchWithColor(card.getFolderName(), card.getCardName(), card.getSetCode(),
							card.getSetNumber(), card.getCondition(), card.getEditionPrinting(), card.getPriceBought(), card.getDateBought(),
							card.getColorVariant(), existingCard)) {
						card.setQuantity(card.getQuantity() + existingCard.getQuantity());
						card.setUuid(existingCard.getUuid());
						card.setGamePlayCardUUID(existingCard.getGamePlayCardUUID());
						break;
					}
				}

				map.put(key, card);
			}
			return currentRowQuantity;
		} else {
			YGOLogger.error("Failed to import card" + current.toString());
			return 0;
		}
	}

}
