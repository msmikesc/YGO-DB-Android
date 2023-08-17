package ygodb.windows.export;

import org.apache.commons.csv.CSVPrinter;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.connection.CsvConnection;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.windows.utility.WindowsUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ExportRarityUnsureToCSV {

	public static void main(String[] args) throws SQLException, IOException {
		ExportRarityUnsureToCSV mainObj = new ExportRarityUnsureToCSV();
		SQLiteConnection db = WindowsUtil.getDBInstance();
		mainObj.run(db);
		db.closeInstance();
	}

	public void run(SQLiteConnection db) throws SQLException, IOException {

		String filename = "rarity-unsure-export.csv";
		String resourcePath = Const.CSV_EXPORT_FOLDER + filename;

		List<OwnedCard> list = db.getRarityUnsureOwnedCards();

		CsvConnection csvConnection = new CsvConnection();

		CSVPrinter p = csvConnection.getExportOutputFile(resourcePath);

		int quantityCount = 0;

		for (OwnedCard current : list) {

			quantityCount += current.getQuantity();

			csvConnection.writeOwnedCardToCSV(p, current);

		}

		YGOLogger.info("Exported cards: " + quantityCount);

		YGOLogger.info("Total cards: " + db.getCountQuantity() + " + " + db.getCountQuantityManual() + " Manual");

		p.flush();
		p.close();

	}

}
