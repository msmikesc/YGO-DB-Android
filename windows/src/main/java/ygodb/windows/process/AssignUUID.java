package ygodb.windows.process;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

import ygodb.commonLibrary.bean.OwnedCard;
import ygodb.commonLibrary.connection.SQLiteConnection;
import ygodb.windows.connection.WindowsUtil;

public class AssignUUID {


	public static void main(String[] args) throws SQLException {
		AssignUUID mainObj = new AssignUUID();

		SQLiteConnection db = WindowsUtil.getDBInstance();

		mainObj.run(db);
		db.closeInstance();
		System.out.println("Process Finished");
	}

	public void run(SQLiteConnection db) throws SQLException {

		ArrayList<OwnedCard> cards = db.getAllOwnedCards();
		
		for(OwnedCard card: cards) {
			if(card.UUID == null) {
				UUID id = UUID.randomUUID();
				card.UUID = id.toString();
				db.upsertOwnedCardBatch(card);
				
			}
		}
		
		
		

	}

}
