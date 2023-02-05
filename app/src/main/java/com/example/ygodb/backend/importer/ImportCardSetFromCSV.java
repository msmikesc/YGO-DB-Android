package com.example.ygodb.backend.importer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Iterator;

import org.apache.commons.csv.CSVRecord;
import com.example.ygodb.backend.connection.CsvConnection;
import com.example.ygodb.backend.connection.SQLiteConnection;

public class ImportCardSetFromCSV {

	public static void main(String[] args) throws SQLException, IOException {
		ImportCardSetFromCSV mainObj = new ImportCardSetFromCSV();
		mainObj.run();
		
		System.out.println("Import Complete");
	}

	public void run() throws SQLException, IOException {

		String csvFileName = "cardsets";

		String fileNameString = "C:\\Users\\Mike\\Documents\\GitHub\\YGO-DB\\YGO-DB\\csv\\" + csvFileName + ".csv";

		Iterator<CSVRecord> it = CsvConnection.getIterator(fileNameString, StandardCharsets.UTF_16LE);

		while (it.hasNext()) {

			CSVRecord current = it.next();

			CsvConnection.insertCardSetFromCSV(current, csvFileName);

		}

	}

}
