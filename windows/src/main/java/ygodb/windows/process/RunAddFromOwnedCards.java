package ygodb.windows.process;

import java.sql.SQLException;

import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.windows.utility.WindowsUtil;
import ygodb.commonlibrary.process.AddFromOwnedCards;

public class RunAddFromOwnedCards {


	public static void main(String[] args) throws SQLException {
		AddFromOwnedCards mainObj = new AddFromOwnedCards();

		SQLiteConnection db = WindowsUtil.getDBInstance();

		mainObj.run(db);
		db.closeInstance();
		YGOLogger.info("Process Complete");
	}
}
