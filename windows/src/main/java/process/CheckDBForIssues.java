package process;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import connection.SQLiteConnection;
import connection.Util;
import connection.WindowsUtil;

public class CheckDBForIssues {

	public static void main(String[] args) throws SQLException, IOException {
		CheckDBForIssues mainObj = new CheckDBForIssues();

		SQLiteConnection db = WindowsUtil.getDBInstance();

		mainObj.run(db);
		db.closeInstance();
		System.out.println("Analyze complete");
	}

	public void run(SQLiteConnection db) throws SQLException, IOException {

		ArrayList<String> setsList = db.getDistinctSetNames();

		for (String setName : setsList) {

			if (setName.contains("Tip Card") || setName.contains("(POR)")) {
				continue;
			}

			Util.checkForIssuesWithSet(setName, db);
			Util.checkForIssuesWithCardNamesInSet(setName, db);

		}
		Util.checkSetCounts(db);

	}

}
