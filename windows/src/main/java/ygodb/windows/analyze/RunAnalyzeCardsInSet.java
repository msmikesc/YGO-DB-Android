package ygodb.windows.analyze;

import java.io.IOException;
import java.sql.SQLException;

import ygodb.commonLibrary.analyze.AnalyzeCardsInSet;
import ygodb.commonLibrary.connection.SQLiteConnection;
import ygodb.windows.connection.WindowsUtil;

public class RunAnalyzeCardsInSet {


	public static void main(String[] args) throws SQLException, IOException {
		AnalyzeCardsInSet mainObj = new AnalyzeCardsInSet();

		SQLiteConnection db = WindowsUtil.getDBInstance();

		mainObj.run(db);
		db.closeInstance();
	}

}
