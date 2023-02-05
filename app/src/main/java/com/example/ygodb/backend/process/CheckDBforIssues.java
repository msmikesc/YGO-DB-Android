package com.example.ygodb.backend.process;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import com.example.ygodb.backend.connection.SQLiteConnection;
import com.example.ygodb.backend.connection.Util;

public class CheckDBforIssues {

	public static void main(String[] args) throws SQLException, IOException {
		CheckDBforIssues mainObj = new CheckDBforIssues();
		mainObj.run();
		
	}

	public void run() throws SQLException, IOException {

		ArrayList<String> setsList = SQLiteConnection.getObj().getDistinctSetNames();

		for (String setName : setsList) {

			if (setName.contains("Tip Card") || setName.contains("(POR)")) {
				continue;
			}

			Util.checkForIssuesWithSet(setName);

		}
		Util.checkSetCounts();

	}

}
