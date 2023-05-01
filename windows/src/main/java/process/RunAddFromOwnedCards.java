package process;

import java.sql.SQLException;

import connection.SQLiteConnection;
import connection.WindowsUtil;

public class RunAddFromOwnedCards {


	public static void main(String[] args) throws SQLException {
		AddFromOwnedCards mainObj = new AddFromOwnedCards();

		SQLiteConnection db = WindowsUtil.getDBInstance();

		mainObj.run(db);
		db.closeInstance();
		System.out.println("Process Complete");
	}
}
