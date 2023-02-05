package com.example.ygodb.backend.export;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import org.apache.commons.csv.CSVPrinter;
import com.example.ygodb.backend.bean.OwnedCard;
import com.example.ygodb.backend.connection.CsvConnection;
import com.example.ygodb.backend.connection.SQLiteConnection;

public class ExportUnSyncedForUpload {

	public static void main(String[] args) throws SQLException, IOException {
		ExportUnSyncedForUpload mainObj = new ExportUnSyncedForUpload();
		mainObj.run();
		
	}

	public void run() throws SQLException, IOException {

		boolean isOnlyUnsyncedCards = true;

		// isOnlyUnsyncedCards = false;

		String filename = "C:\\Users\\Mike\\Documents\\GitHub\\YGO-DB\\YGO-DB\\csv\\all-upload.csv";

		ArrayList<OwnedCard> list = SQLiteConnection.getObj().getAllOwnedCards();

		CSVPrinter p = CsvConnection.getExportUploadFile(filename);

		int quantityCount = 0;

		for (OwnedCard current : list) {

			if ((!isOnlyUnsyncedCards) || current.folderName.equals("UnSynced Folder")) {
				CsvConnection.writeUploadCardToCSV(p, current);

				quantityCount += current.quantity;

				if (current.folderName.equals("UnSynced Folder")) {
					
					current.folderName = "Sync Folder";
					
					SQLiteConnection.getObj().UpdateOwnedCardByUUID(current);
				}

			}
		}

		System.out.println("Exported cards: " + quantityCount);

		System.out.println("Total cards: " + SQLiteConnection.getObj().getCountQuantity() + " + "
				+ SQLiteConnection.getObj().getCountQuantityManual() + " Manual");

		p.flush();
		p.close();

	}

}
