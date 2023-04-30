package importer;

import java.io.IOException;
import java.sql.SQLException;

import connection.SQLiteConnection;
import connection.WindowsUtil;

public class RunImportPricesFromYGOPROAPI {

	public static void main(String[] args) throws SQLException, IOException {
		ImportPricesFromYGOPROAPI mainObj = new ImportPricesFromYGOPROAPI();

		SQLiteConnection db = WindowsUtil.getDBInstance();

		mainObj.run(db);
		db.closeInstance();
		System.out.println("Import Finished");
	}

}
