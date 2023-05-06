package ygodb.commonLibrary.importer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Iterator;

import org.apache.commons.csv.CSVRecord;
import ygodb.commonLibrary.connection.CsvConnection;
import ygodb.commonLibrary.connection.SQLiteConnection;

public class ImportCardSetFromCSV {

	public void run(SQLiteConnection db) throws SQLException, IOException {

		String csvFileName = "cardsets";

		String fileNameString = "C:\\Users\\Mike\\Documents\\GitHub\\YGO-DB\\YGO-DB\\csv\\" + csvFileName + ".csv";

		Iterator<CSVRecord> it = CsvConnection.getIterator(fileNameString, StandardCharsets.UTF_16LE);

		while (it.hasNext()) {

			CSVRecord current = it.next();

			CsvConnection.insertCardSetFromCSV(current, csvFileName, db);

		}

	}

}
