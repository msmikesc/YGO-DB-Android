package ygodb.windows.export;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import org.apache.commons.csv.CSVPrinter;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.windows.connection.CsvConnection;
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
		
		String filename = "C:\\Users\\Mike\\Documents\\GitHub\\YGO-DB\\YGO-DB\\csv\\rarity-unsure-export.csv";
		
		ArrayList<OwnedCard> list = db.getRarityUnsureOwnedCards();
		
		CSVPrinter p = CsvConnection.getExportOutputFile(filename);

		int quantityCount = 0;
		
		for(OwnedCard current : list) {
			
			quantityCount += current.quantity;

			CsvConnection.writeOwnedCardToCSV(p,current);

		}
		
		YGOLogger.info("Exported cards: "+ quantityCount);
		
		YGOLogger.info("Total cards: "+db.getCountQuantity() + " + " + db.getCountQuantityManual() + " Manual");
		
		p.flush();
		p.close();
		
	}

}
