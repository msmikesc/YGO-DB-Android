package ygodb.windows.process;

import java.sql.SQLException;
import java.util.ArrayList;

import ygodb.commonlibrary.bean.CardSet;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.bean.SetMetaData;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.utility.Util;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.windows.utility.WindowsUtil;

public class UpdateOwnedCardsWithoutSetNumberBySetName {

	public static void main(String[] args) throws SQLException {
		UpdateOwnedCardsWithoutSetNumberBySetName mainObj = new UpdateOwnedCardsWithoutSetNumberBySetName();

		SQLiteConnection db = WindowsUtil.getDBInstance();

		mainObj.run(db);
		db.closeInstance();
	}

	public void run(SQLiteConnection db) throws SQLException {

		ArrayList<OwnedCard> cards = db.getAllOwnedCardsWithoutSetNumber();
		
		int count = 0;

		for (OwnedCard card : cards) {
			
			String newSetName = Util.checkForTranslatedSetName(card.getSetName());
				
			card.setSetName(newSetName);

			CardSet setIdentified = db.getFirstCardSetForCardInSet(card.getCardName(), newSetName);

			if (setIdentified == null) {
				YGOLogger.info("Unknown setCode for card name and set: " + card.getCardName() + ":" + newSetName);
				continue;
			}

			card.setGamePlayCardUUID(setIdentified.getGamePlayCardUUID());
			card.setSetNumber(setIdentified.getSetNumber());

			ArrayList<SetMetaData> metaData = db.getSetMetaDataFromSetName(newSetName);

			if (metaData.size() != 1) {
				YGOLogger.info("Unknown metaData for set: " + newSetName);
				continue;
			} else {
				card.setSetCode(metaData.get(0).getSetCode());
			}

			card.setRarityUnsure(0);

			try{
				db.updateOwnedCardByUUID(card);
				count++;
			}
			catch(Exception e) {
				YGOLogger.logException(e);
			}
				

		}
		
		YGOLogger.info("Updated " + count + " rows");
	}

}
