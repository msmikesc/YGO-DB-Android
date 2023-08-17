package ygodb.windows.process;

import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.process.AddFromOwnedCards;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.windows.utility.WindowsUtil;

import java.sql.SQLException;

public class RunAddFromOwnedCards {


	public static void main(String[] args) throws SQLException {
		AddFromOwnedCards mainObj = new AddFromOwnedCards();

		SQLiteConnection db = WindowsUtil.getDBInstance();

		mainObj.run(db);
		db.closeInstance();
		YGOLogger.info("Process Complete");
	}
}
