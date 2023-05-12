package ygodb.windows.importer;

import ygodb.commonLibrary.connection.SQLiteConnection;
import ygodb.commonLibrary.importer.ImportCardSetFromCSV;
import ygodb.windows.connection.WindowsUtil;

import java.io.IOException;
import java.sql.SQLException;

public class RunImportCardSetFromCSV {

	public static void main(String[] args) throws SQLException, IOException {
		ImportCardSetFromCSV mainObj = new ImportCardSetFromCSV();

        SQLiteConnection db = WindowsUtil.getDBInstance();

		mainObj.run(db);
		db.closeInstance();
		System.out.println("Import Complete");
	}
}
