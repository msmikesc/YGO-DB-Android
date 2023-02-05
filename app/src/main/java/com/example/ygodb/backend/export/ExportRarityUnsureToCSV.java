package com.example.ygodb.backend.export;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import org.apache.commons.csv.CSVPrinter;
import com.example.ygodb.backend.bean.OwnedCard;
import com.example.ygodb.backend.connection.CsvConnection;
import com.example.ygodb.backend.connection.SQLiteConnection;

public class ExportRarityUnsureToCSV {

	public static void main(String[] args) throws SQLException, IOException {
		ExportRarityUnsureToCSV mainObj = new ExportRarityUnsureToCSV();
		mainObj.run();
		
	}

	public void run() throws SQLException, IOException {
		
		String filename = "C:\\Users\\Mike\\Documents\\GitHub\\YGO-DB\\YGO-DB\\csv\\rarity-unsure-export.csv";
		
		ArrayList<OwnedCard> list = SQLiteConnection.getObj().getRarityUnsureOwnedCards();
		
		CSVPrinter p = CsvConnection.getExportOutputFile(filename);

		int quantityCount = 0;
		
		for(OwnedCard current : list) {
			
			quantityCount += current.quantity;

			CsvConnection.writeOwnedCardToCSV(p,current);

		}
		
		System.out.println("Exported cards: "+ quantityCount);
		
		System.out.println("Total cards: "+SQLiteConnection.getObj().getCountQuantity() + " + " + SQLiteConnection.getObj().getCountQuantityManual() + " Manual");
		
		p.flush();
		p.close();
		
	}

}
