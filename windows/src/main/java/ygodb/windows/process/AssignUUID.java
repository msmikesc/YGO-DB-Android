package ygodb.windows.process;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ygodb.commonLibrary.bean.GamePlayCard;
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
			if(card.uuid == null) {
				UUID id = UUID.randomUUID();
				card.uuid = id.toString();
				db.upsertOwnedCardBatch(card);
				
			}
		}

		List<GamePlayCard> gamePlayCards = db.getAllGamePlayCard();

		for(GamePlayCard card: gamePlayCards) {
			if(card.gamePlayCardUUID == null) {
				UUID id = UUID.randomUUID();
				card.gamePlayCardUUID = id.toString();
				db.replaceIntoGamePlayCard(card);

			}
		}
		

	}

}
