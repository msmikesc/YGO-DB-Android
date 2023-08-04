package ygodb.windows.utility;

import ygodb.windows.connection.SQLiteConnectionWindows;

public class WindowsUtil {

	private WindowsUtil() {
	}

	private static SQLiteConnectionWindows dbInstance = null;

	public static SQLiteConnectionWindows getDBInstance() {
		if (dbInstance == null) {
			dbInstance = new SQLiteConnectionWindows();
		}

		return dbInstance;
	}


}
