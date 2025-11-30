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

		String[] setNames = mainObj.run(db, "C:\\Users\\Mike\\AndroidStudioProjects\\YGODB\\log\\lastPriceLoadJSON",
										 true,"C:\\Users\\Mike\\AndroidStudioProjects\\YGODB\\log\\lastPriceLoadMissingCardSets.csv");
		if (setNames.length > 0) {
			new CreateCSVFromYugipedia().run(db, setNames, "importPricesCSV");
		}
		YGOLogger.info("Import Finished");
		db.closeInstance();
	}
}
