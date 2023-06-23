package ygodb.windows.export;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import org.apache.commons.csv.CSVPrinter;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.commonlibrary.connection.CsvConnection;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.windows.utility.WindowsUtil;

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
		
		ArrayList<OwnedCard> list = db.getRarityUnsureOwnedCards();
		
		CSVPrinter p = CsvConnection.getExportOutputFile(resourcePath);

		int quantityCount = 0;
		
		for(OwnedCard current : list) {
			
			quantityCount += current.getQuantity();

			CsvConnection.writeOwnedCardToCSV(p,current);

		}
		
		YGOLogger.info("Exported cards: "+ quantityCount);
		
		YGOLogger.info("Total cards: "+db.getCountQuantity() + " + " + db.getCountQuantityManual() + " Manual");
		
		p.flush();
		p.close();
		
	}

}
