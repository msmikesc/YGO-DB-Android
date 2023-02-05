package com.example.ygodb.backend.analyze;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import org.apache.commons.csv.CSVPrinter;

import com.example.ygodb.backend.bean.AnalyzePrintedOnceData;
import com.example.ygodb.backend.connection.CsvConnection;
import com.example.ygodb.backend.connection.SQLiteConnection;

public class AnalyzePrintedOnce {

	public static void main(String[] args) throws SQLException, IOException {
		AnalyzePrintedOnce mainObj = new AnalyzePrintedOnce();
		mainObj.run();
		
	}

	public void run() throws SQLException, IOException {

		HashMap<String, AnalyzePrintedOnceData> h = SQLiteConnection.getObj().getCardsOnlyPrintedOnce();

		ArrayList<AnalyzePrintedOnceData> array = new ArrayList<AnalyzePrintedOnceData>(h.values());

		printOutput(array);

	}

	public void printOutput(ArrayList<AnalyzePrintedOnceData> array) throws IOException {
		Collections.sort(array);

		String filename = "C:\\Users\\Mike\\Documents\\GitHub\\YGO-DB\\YGO-DB\\csv\\Analyze-PrintedOnce.csv";

		CSVPrinter p = CsvConnection.getAnalyzePrintedOnceOutputFile(filename);

		for (AnalyzePrintedOnceData s : array) {
			p.printRecord(s.wikiID, s.cardName, s.cardType, s.getStringOfRarities(), s.getStringOfSetNames(),
					s.getStringOfSetNumbers(), s.releaseDate, s.archetype);

		}
		p.flush();
		p.close();
	}
}
