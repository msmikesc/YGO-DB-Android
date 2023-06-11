package ygodb.windows.export;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import org.apache.commons.csv.CSVPrinter;
import ygodb.commonLibrary.bean.OwnedCard;
import ygodb.commonLibrary.utility.YGOLogger;
import ygodb.windows.connection.CsvConnection;
import ygodb.commonLibrary.connection.SQLiteConnection;
import ygodb.commonLibrary.constant.Const;
import ygodb.windows.utility.WindowsUtil;

public class ExportUnSyncedForUpload {

	public static void main(String[] args) throws SQLException, IOException {
		ExportUnSyncedForUpload mainObj = new ExportUnSyncedForUpload();
		SQLiteConnection db = WindowsUtil.getDBInstance();
		mainObj.run(db);
		db.closeInstance();
	}

	public void run(SQLiteConnection db) throws SQLException, IOException {

		boolean isOnlyUnsyncedCards = true;

		// isOnlyUnsyncedCards = false;

		String filename = "C:\\Users\\Mike\\Documents\\GitHub\\YGO-DB\\YGO-DB\\csv\\all-upload.csv";

		ArrayList<OwnedCard> list = db.getAllOwnedCards();

		CSVPrinter p = CsvConnection.getExportUploadFile(filename);

		int quantityCount = 0;

		for (OwnedCard current : list) {

			if ((!isOnlyUnsyncedCards) || current.folderName.equals(Const.FOLDER_UNSYNCED)) {
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
