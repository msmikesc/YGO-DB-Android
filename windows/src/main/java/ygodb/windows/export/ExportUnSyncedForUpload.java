package ygodb.windows.export;

import org.apache.commons.csv.CSVPrinter;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.connection.CsvConnection;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.Util;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.windows.utility.WindowsUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExportUnSyncedForUpload {

	public static final List<String> DO_NOT_UPLOAD_SET_PREFIX = Arrays.asList("OP23", "OP24", "BLC1");

	public static void main(String[] args) throws SQLException, IOException {
		ExportUnSyncedForUpload mainObj = new ExportUnSyncedForUpload();
		SQLiteConnection db = WindowsUtil.getDBInstance();
		mainObj.run(db);
		db.closeInstance();
	}

	public void run(SQLiteConnection db) throws SQLException, IOException {

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		Date readTime = new Date();

		String filename = "all-upload" + dateFormat.format(readTime) + ".csv";
		String resourcePath = Const.CSV_EXPORT_FOLDER + filename;

		List<OwnedCard> list = db.getAllOwnedCards();

		CsvConnection csvConnection = new CsvConnection();

		CSVPrinter p = csvConnection.getExportUploadFile(resourcePath);

		int quantityCount = 0;

		for (OwnedCard current : list) {

			if (current.getFolderName().equals(Const.FOLDER_UNSYNCED) && !DO_NOT_UPLOAD_SET_PREFIX.contains(current.getSetPrefix())
					&& !Util.normalizePrice(current.getPriceBought()).equals(Const.ZERO_PRICE_STRING)) {
				quantityCount += current.getQuantity();
				current.setFolderName(Const.FOLDER_EXPORT_PREFIX + dateFormat.format(readTime));
				db.updateOwnedCardByUUID(current);

				//temporary changes
				if (current.getSetName().contains("(25th Anniversary Edition)")) {
					current.setSetPrefix(current.getSetPrefix() + "_25");
				}

				csvConnection.writeUploadCardToCSV(p, current);
			}
		}

		YGOLogger.info("Exported cards: " + quantityCount);

		YGOLogger.info("Total cards: " + db.getCountQuantity() + " + " + db.getCountQuantityManual() + " Manual");

		p.flush();
		p.close();

	}

}
