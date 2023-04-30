package process;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import connection.SQLiteConnection;
import connection.Util;

public class CheckDBforIssues {

	/*
	public static void main(String[] args) throws SQLException, IOException {
		CheckDBforIssues mainObj = new CheckDBforIssues();
		mainObj.run();
		
	}*/

	public void run(SQLiteConnection db) throws SQLException, IOException {

		ArrayList<String> setsList = db.getDistinctSetNames();

		for (String setName : setsList) {

			if (setName.contains("Tip Card") || setName.contains("(POR)")) {
				continue;
			}

			Util.checkForIssuesWithSet(setName, db);

		}
		Util.checkSetCounts(db);

	}

}
