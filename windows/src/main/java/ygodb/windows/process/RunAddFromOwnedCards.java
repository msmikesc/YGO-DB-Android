package ygodb.windows.process;

import java.sql.SQLException;

import ygodb.commonLibrary.connection.SQLiteConnection;
import ygodb.windows.utility.WindowsUtil;
import ygodb.commonLibrary.process.AddFromOwnedCards;

public class RunAddFromOwnedCards {


	public static void main(String[] args) throws SQLException {
		AddFromOwnedCards mainObj = new AddFromOwnedCards();

		SQLiteConnection db = WindowsUtil.getDBInstance();

		mainObj.run(db);
		db.closeInstance();
		System.out.println("Process Complete");
	}
}
