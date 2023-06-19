package ygodb.windows.importer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.windows.connection.CsvConnection;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.windows.utility.WindowsUtil;

import java.sql.SQLException;

public class ImportGamePlayCardFromCSV {

	public static void main(String[] args) throws SQLException, IOException {
		ImportGamePlayCardFromCSV mainObj = new ImportGamePlayCardFromCSV();

		SQLiteConnection db = WindowsUtil.getDBInstance();

		mainObj.run(db);
		db.closeInstance();
		YGOLogger.info("Import Complete");
	}

	public void run(SQLiteConnection db) throws SQLException, IOException {

		String filename = "gamePlayCards.csv";
		String resourcePath = Const.CSV_IMPORT_FOLDER + filename;

		CSVParser parser = CsvConnection.getParser(resourcePath, StandardCharsets.UTF_16LE);

		for (CSVRecord current : parser) {
			CsvConnection.insertGamePlayCardFromCSV(current, db);
		}

		parser.close();

	}
}
