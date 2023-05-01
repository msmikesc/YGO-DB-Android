package Analyze;

import java.io.IOException;
import java.sql.SQLException;

import analyze.AnalyzeCardsInSet;
import connection.SQLiteConnection;
import connection.WindowsUtil;

public class RunAnalyzeCardsInset {


	public static void main(String[] args) throws SQLException, IOException {
		AnalyzeCardsInSet mainObj = new AnalyzeCardsInSet();

		SQLiteConnection db = WindowsUtil.getDBInstance();

		mainObj.run(db);
		db.closeInstance();
	}

}
