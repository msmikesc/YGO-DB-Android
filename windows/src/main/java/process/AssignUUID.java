package process;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

import bean.OwnedCard;
import connection.SQLiteConnection;
import connection.WindowsUtil;

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
