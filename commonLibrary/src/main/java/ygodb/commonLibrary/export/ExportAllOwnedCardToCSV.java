package ygodb.commonLibrary.export;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import org.apache.commons.csv.CSVPrinter;
import ygodb.commonLibrary.bean.OwnedCard;
import ygodb.commonLibrary.connection.CsvConnection;
import ygodb.commonLibrary.connection.SQLiteConnection;

public class ExportAllOwnedCardToCSV {


	/*
	public static void main(String[] args) throws SQLException, IOException {
		ExportAllOwnedCardToCSV mainObj = new ExportAllOwnedCardToCSV();
		mainObj.run();
		SQLiteConnection.closeInstance();
	}*/

	public void run(SQLiteConnection db) throws SQLException, IOException {
		
		String filename = "C:\\Users\\Mike\\Documents\\GitHub\\YGO-DB\\YGO-DB\\csv\\all-export.csv";
		
		ArrayList<OwnedCard> list = db.getAllOwnedCards();
		
		CSVPrinter p = CsvConnection.getExportOutputFile(filename);
		
		int quantityCount = 0;
		
		for(OwnedCard current : list) {
			
			quantityCount += current.quantity;

			CsvConnection.writeOwnedCardToCSV(p,current);

		}
		
		System.out.println("Exported cards: "+ quantityCount);
		
		System.out.println("Total cards: "+db.getCountQuantity() + " + " + db.getCountQuantityManual() + " Manual");
		
		p.flush();
		p.close();
		
	}

}
