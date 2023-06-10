package ygodb.windows.importer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Iterator;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import ygodb.windows.connection.CsvConnection;
import ygodb.commonLibrary.connection.SQLiteConnection;
import ygodb.windows.utility.WindowsUtil;

public class ImportCardSetFromCSV {

	public static void main(String[] args) throws SQLException, IOException {
		ImportCardSetFromCSV mainObj = new ImportCardSetFromCSV();

		SQLiteConnection db = WindowsUtil.getDBInstance();

		mainObj.run(db);
		db.closeInstance();
		System.out.println("Import Complete");
	}

	public void run(SQLiteConnection db) throws SQLException, IOException {

		String csvFileName = "cardsets";

		String fileNameString = "C:\\Users\\Mike\\Documents\\GitHub\\YGO-DB\\YGO-DB\\csv\\" + csvFileName + ".csv";

		CSVParser parser = CsvConnection.getParser(fileNameString, StandardCharsets.UTF_16LE);

		for (CSVRecord current : parser) {
			CsvConnection.insertCardSetFromCSV(current, csvFileName, db);
		}

		parser.close();

	}

}
