package ygodb.windows.export;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import org.apache.commons.csv.CSVPrinter;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.commonlibrary.connection.CsvConnection;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.constant.Const;
import ygodb.windows.utility.WindowsUtil;

public class ExportUnSyncedForUpload {

	public static void main(String[] args) throws SQLException, IOException {
		ExportUnSyncedForUpload mainObj = new ExportUnSyncedForUpload();
		SQLiteConnection db = WindowsUtil.getDBInstance();
		mainObj.run(db);
		db.closeInstance();
	}

	public void run(SQLiteConnection db) throws SQLException, IOException {

		String filename = "all-upload.csv";
		String resourcePath = Const.CSV_EXPORT_FOLDER + filename;

		ArrayList<OwnedCard> list = db.getAllOwnedCards();

		CSVPrinter p = CsvConnection.getExportUploadFile(resourcePath);

		int quantityCount = 0;

		for (OwnedCard current : list) {

			if (current.folderName.equals(Const.FOLDER_UNSYNCED)) {
				CsvConnection.writeUploadCardToCSV(p, current);

				quantityCount += current.quantity;

				if (current.folderName.equals(Const.FOLDER_UNSYNCED)) {
					
					current.folderName = Const.FOLDER_SYNC;
					
					db.updateOwnedCardByUUID(current);
				}

			}
		}

		YGOLogger.info("Exported cards: " + quantityCount);

		YGOLogger.info("Total cards: " + db.getCountQuantity() + " + "
				+ db.getCountQuantityManual() + " Manual");

		p.flush();
		p.close();

	}

}
