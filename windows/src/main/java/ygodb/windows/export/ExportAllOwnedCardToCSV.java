package ygodb.windows.export;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import org.apache.commons.csv.CSVPrinter;
import ygodb.commonLibrary.bean.OwnedCard;
import ygodb.commonLibrary.utility.YGOLogger;
import ygodb.windows.connection.CsvConnection;

import ygodb.commonLibrary.connection.SQLiteConnection;
import ygodb.windows.utility.WindowsUtil;

public class ExportAllOwnedCardToCSV {

	public static void main(String[] args) throws SQLException, IOException {
		ExportAllOwnedCardToCSV mainObj = new ExportAllOwnedCardToCSV();
		SQLiteConnection db = WindowsUtil.getDBInstance();
		mainObj.run(db);
		db.closeInstance();
	}

	public void run(SQLiteConnection db) throws SQLException, IOException {
		
		String filename = "C:\\Users\\Mike\\Documents\\GitHub\\YGO-DB\\YGO-DB\\csv\\all-export.csv";
		
		ArrayList<OwnedCard> list = db.getAllOwnedCards();
		
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
