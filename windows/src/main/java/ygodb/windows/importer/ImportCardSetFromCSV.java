package ygodb.windows.importer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.commonlibrary.connection.CsvConnection;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.windows.utility.WindowsUtil;

public class ImportCardSetFromCSV {

	public static void main(String[] args) throws SQLException, IOException {
		ImportCardSetFromCSV mainObj = new ImportCardSetFromCSV();

		SQLiteConnection db = WindowsUtil.getDBInstance();

		mainObj.run(db);
		db.closeInstance();
		YGOLogger.info("Import Complete");
	}

	public void run(SQLiteConnection db) throws SQLException, IOException {

		String filename = "cardsets.csv";
		String resourcePath = Const.CSV_IMPORT_FOLDER + filename;

		CsvConnection csvConnection = new CsvConnection();

		CSVParser parser = csvConnection.getParser(resourcePath, StandardCharsets.UTF_16LE);

		for (CSVRecord current : parser) {
			csvConnection.insertCardSetFromCSV(current, filename, db);
		}

		parser.close();

	}

}
