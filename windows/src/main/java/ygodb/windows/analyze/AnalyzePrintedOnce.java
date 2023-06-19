package ygodb.windows.analyze;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.csv.CSVPrinter;

import ygodb.commonlibrary.bean.AnalyzePrintedOnceData;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.connection.CsvConnection;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.windows.utility.WindowsUtil;

public class AnalyzePrintedOnce {

	public static void main(String[] args) throws SQLException, IOException {
		AnalyzePrintedOnce mainObj = new AnalyzePrintedOnce();
		SQLiteConnection db = WindowsUtil.getDBInstance();
		mainObj.run(db);
		db.closeInstance();
	}

	public void run(SQLiteConnection db) throws SQLException, IOException {

		HashMap<String, AnalyzePrintedOnceData> h = db.getCardsOnlyPrintedOnce();

		ArrayList<AnalyzePrintedOnceData> array = new ArrayList<>(h.values());

		printOutput(array);

	}

	public void printOutput(List<AnalyzePrintedOnceData> array) throws IOException {
		Collections.sort(array);

		String filename = "Analyze-PrintedOnce.csv";
		String resourcePath = Const.CSV_ANALYZE_FOLDER + filename;

		CSVPrinter p = CsvConnection.getAnalyzePrintedOnceOutputFile(resourcePath);

		for (AnalyzePrintedOnceData s : array) {
			p.printRecord(s.gamePlayCardUUID, s.cardName, s.cardType, s.getStringOfRarities(), s.getStringOfSetNames(),
					s.getStringOfSetNumbers(), s.releaseDate, s.archetype);

		}
		p.flush();
		p.close();
	}
}
