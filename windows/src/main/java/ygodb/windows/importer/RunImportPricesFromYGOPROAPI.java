package ygodb.windows.importer;

import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.importer.ImportPricesFromYGOPROAPI;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.windows.utility.WindowsUtil;

import java.io.IOException;
import java.sql.SQLException;

public class RunImportPricesFromYGOPROAPI {
	public static void main(String[] args) throws SQLException, IOException {
		ImportPricesFromYGOPROAPI mainObj = new ImportPricesFromYGOPROAPI();

		SQLiteConnection db = WindowsUtil.getDBInstance();

		boolean successful = mainObj.run(db, "C:\\Users\\Mike\\AndroidStudioProjects\\YGODB\\log\\lastPriceLoadJSON.txt", true);
		if (!successful) {
			YGOLogger.info("Import Failed");
		} else {
			YGOLogger.info("Import Finished");
		}
		db.closeInstance();
	}
}
