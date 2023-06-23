package ygodb.windows.analyze;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.csv.CSVPrinter;
import ygodb.commonlibrary.analyze.AnalyzeCardsInSet;
import ygodb.commonlibrary.bean.AnalyzeData;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.windows.utility.WindowsUtil;
import ygodb.commonlibrary.connection.CsvConnection;

public class RunAnalyzeCardsInSet {


	public static void main(String[] args) throws SQLException, IOException {
		AnalyzeCardsInSet mainObj = new AnalyzeCardsInSet();

		SQLiteConnection db = WindowsUtil.getDBInstance();
		run(mainObj, db);
		db.closeInstance();
	}

	public static void run(AnalyzeCardsInSet mainObj, SQLiteConnection db) throws SQLException, IOException {

		YGOLogger.info("Set Name or Code: ");

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		String setName = reader.readLine();
		String finalFileName = setName;

		if (setName == null || setName.isBlank()) {
			setName = "HAC1;BLVO;SDFC;MAMA;SGX2;SDCB;MP22;TAMA;POTE;"
					+ "LDS3;LED9;DIFO;GFP2;SDAZ;SGX1;BACH;GRCR;BROL;"
					+ "MGED;BODE;LED8;SDCS;MP21;DAMA;KICO;EGO1;EGS1;"
					+ "LIOV;ANGU;GEIM;SBCB;SDCH;PHHY;DABL;AMDE;PHHY;MAZE;CYAC;WISU";
			finalFileName = "Combined";
		}

		HashMap<String, AnalyzeData> h = new HashMap<>();

		String[] sets = setName.split(";");

		for (String individualSet : sets) {
			mainObj.addAnalyzeDataForSet(h, individualSet, db);
		}

		ArrayList<AnalyzeData> array = new ArrayList<>(h.values());

		printOutput(array, finalFileName);

	}

	public static void printOutput(List<AnalyzeData> array, String setName) throws IOException {
		Collections.sort(array);

		String filename = "Analyze-" + setName.replaceAll("[\\s\\\\/:*?\"<>|]", "") + ".csv";
		String resourcePath = Const.CSV_ANALYZE_FOLDER + filename;

		CSVPrinter p = CsvConnection.getAnalyzeOutputFile(resourcePath);

		boolean printedSeparator = false;

		for (AnalyzeData s : array) {

			if (!printedSeparator && s.getQuantity() >= 3) {
				printedSeparator = true;
				YGOLogger.info("");
				YGOLogger.info("----");
				YGOLogger.info("");
			}

			YGOLogger.info(s.getQuantity() + ":" + s.getCardName() + " " + s.getStringOfRarities());

			String massbuy = "";

			if (s.getQuantity() < 3) {
				if (Const.CARD_TYPE_SKILL.equals(s.getCardType())) {
					if (s.getQuantity() < 1) {
						massbuy = (1) + " " + s.getCardName();
					} else {
						massbuy = "";
					}
				} else {

					massbuy = (3 - s.getQuantity()) + " " + s.getCardName();
				}
			}

			String massbuy1 = "";

			if (s.getQuantity() < 1) {
				massbuy1 = (1 - s.getQuantity()) + " " + s.getCardName();
			}

			p.printRecord(s.getQuantity(), s.getCardName(), s.getCardType(), s.getStringOfRarities(), s.getStringOfSetNames(),
					s.getStringOfSetNumbers(), massbuy, massbuy1);

		}
		p.flush();
		p.close();
	}

}
