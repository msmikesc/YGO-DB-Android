package com.example.ygodb.backend.importer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import org.apache.commons.csv.CSVRecord;
import com.example.ygodb.backend.connection.CsvConnection;
import com.example.ygodb.backend.connection.SQLiteConnection;
import java.sql.SQLException;

public class ImportGamePlayCardFromCSV {

	public static void main(String[] args) throws SQLException, IOException {
		ImportGamePlayCardFromCSV mainObj = new ImportGamePlayCardFromCSV();
		mainObj.run();
		
		System.out.println("Import Complete");
	}

	public void run() throws SQLException, IOException {
		
		String csvFileName = "gamePlayCards";

		String fileNameString = "C:\\Users\\Mike\\Documents\\GitHub\\YGO-DB\\YGO-DB\\csv\\" + csvFileName + ".csv";

		Iterator<CSVRecord> it = CsvConnection.getIterator(fileNameString, StandardCharsets.UTF_16LE);

		while (it.hasNext()) {

			CSVRecord current = it.next();

			CsvConnection.insertGamePlayCardFromCSV(current, csvFileName);
		}

	}
}
