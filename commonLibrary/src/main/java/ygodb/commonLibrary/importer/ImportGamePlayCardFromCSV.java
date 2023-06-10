package ygodb.commonLibrary.importer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import ygodb.commonLibrary.connection.CsvConnection;
import ygodb.commonLibrary.connection.SQLiteConnection;

import java.sql.SQLException;

public class ImportGamePlayCardFromCSV {

	public void run(SQLiteConnection db) throws SQLException, IOException {
		
		String csvFileName = "gamePlayCards";

		String fileNameString = "C:\\Users\\Mike\\Documents\\GitHub\\YGO-DB\\YGO-DB\\csv\\" + csvFileName + ".csv";

		CSVParser parser = CsvConnection.getParser(fileNameString, StandardCharsets.UTF_16LE);

		Iterator<CSVRecord> it = parser.iterator();

		while (it.hasNext()) {

			CSVRecord current = it.next();

			CsvConnection.insertGamePlayCardFromCSV(current, db);
		}

		parser.close();

	}
}
