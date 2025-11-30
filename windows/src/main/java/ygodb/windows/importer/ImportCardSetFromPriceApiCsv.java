package ygodb.windows.importer;

import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.windows.utility.WindowsUtil;

import java.io.IOException;
import java.sql.SQLException;

public class ImportCardSetFromPriceApiCsv {
	public static void main(String[] args) throws SQLException, IOException {
		ImportCardSetFromCSV mainObj = new ImportCardSetFromCSV();

		SQLiteConnection db = WindowsUtil.getDBInstance();

		String filename = "importPricesCSV.csv";
		String resourcePath = Const.CSV_EXPORT_FOLDER + filename;

		mainObj.run(db, resourcePath);
		db.closeInstance();
		YGOLogger.info("Import Complete");
	}

}
