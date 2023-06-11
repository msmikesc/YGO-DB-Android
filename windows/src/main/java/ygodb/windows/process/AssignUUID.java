package ygodb.windows.process;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ygodb.commonlibrary.bean.GamePlayCard;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.windows.utility.WindowsUtil;

public class AssignUUID {


	public static void main(String[] args) throws SQLException {
		AssignUUID mainObj = new AssignUUID();

		SQLiteConnection db = WindowsUtil.getDBInstance();

		mainObj.run(db);
		db.closeInstance();
		YGOLogger.info("Process Finished");
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
