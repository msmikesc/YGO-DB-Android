package ygodb.windows.importer;

import java.io.IOException;
import java.sql.SQLException;

import ygodb.commonLibrary.connection.SQLiteConnection;
import ygodb.commonLibrary.importer.ImportGamePlayCardFromCSV;
import ygodb.windows.connection.WindowsUtil;

public class RunImportGamePlayCardFromCSV {

	public static void main(String[] args) throws SQLException, IOException {
		ImportGamePlayCardFromCSV mainObj = new ImportGamePlayCardFromCSV();

        SQLiteConnection db = WindowsUtil.getDBInstance();

		mainObj.run(db);
		db.closeInstance();
		System.out.println("Import Complete");
	}
}
