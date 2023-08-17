package ygodb.windows.process;

import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.utility.Util;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.windows.utility.WindowsUtil;

import java.sql.SQLException;
import java.util.List;

public class CheckDBForIssues {

	public static void main(String[] args) throws SQLException {
		CheckDBForIssues mainObj = new CheckDBForIssues();

		SQLiteConnection db = WindowsUtil.getDBInstance();

		mainObj.run(db);
		db.closeInstance();
		YGOLogger.info("Analyze complete");
	}

	public void run(SQLiteConnection db) throws SQLException {

		List<String> setsList = db.getDistinctSetNames();

		for (String setName : setsList) {

			if (setName.contains("Tip Card") || setName.contains("(POR)") || setName.equals("Redemption Replacement")) {
				continue;
			}

			Util.checkForIssuesWithSet(setName, db);
			Util.checkForIssuesWithCardNamesInSet(setName, db);

		}
		Util.checkSetCounts(db);

	}

}
