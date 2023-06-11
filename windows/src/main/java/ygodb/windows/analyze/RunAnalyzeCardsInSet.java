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
import ygodb.commonLibrary.analyze.AnalyzeCardsInSet;
import ygodb.commonLibrary.bean.AnalyzeData;
import ygodb.commonLibrary.connection.SQLiteConnection;
import ygodb.commonLibrary.constant.Const;
import ygodb.commonLibrary.utility.YGOLogger;
import ygodb.windows.utility.WindowsUtil;
import ygodb.windows.connection.CsvConnection;

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
					+ "LIOV;ANGU;GEIM;SBCB;SDCH;PHHY;DABL;AMDE;PHHY;MAZE";
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

		String filename = "C:\\Users\\Mike\\Documents\\GitHub\\YGO-DB\\YGO-DB\\csv\\Analyze-"
				+ setName.replaceAll("[\\s\\\\/:*?\"<>|]", "") + ".csv";

		CSVPrinter p = CsvConnection.getAnalyzeOutputFile(filename);

		if(p == null){
			return;
		}

		boolean printedSeparator = false;

		for (AnalyzeData s : array) {

			if (!printedSeparator && s.quantity >= 3) {
				printedSeparator = true;
				YGOLogger.info("");
				YGOLogger.info("----");
				YGOLogger.info("");
			}

			YGOLogger.info(s.quantity + ":" + s.cardName + " " + s.getStringOfRarities());

			String massbuy = "";

			if (s.quantity < 3) {
				if (s.cardType.equals(Const.CARD_TYPE_SKILL)) {
					if (s.quantity < 1) {
						massbuy = (1) + " " + s.cardName;
					} else {
						massbuy = "";
					}
				} else {

					massbuy = (3 - s.quantity) + " " + s.cardName;
				}
			}

			String massbuy1 = "";

			if (s.quantity < 1) {
				massbuy1 = (1 - s.quantity) + " " + s.cardName;
			}

			p.printRecord(s.quantity, s.cardName, s.cardType, s.getStringOfRarities(), s.getStringOfSetNames(),
					s.getStringOfSetNumbers(), massbuy, massbuy1);

		}
		p.flush();
		p.close();
	}

}