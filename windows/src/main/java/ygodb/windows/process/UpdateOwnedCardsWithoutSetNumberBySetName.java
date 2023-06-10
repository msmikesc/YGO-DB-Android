package ygodb.windows.process;

import java.sql.SQLException;
import java.util.ArrayList;

import ygodb.commonLibrary.bean.CardSet;
import ygodb.commonLibrary.bean.OwnedCard;
import ygodb.commonLibrary.bean.SetMetaData;
import ygodb.commonLibrary.connection.SQLiteConnection;
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
			
			String newSetName = WindowsUtil.checkForTranslatedSetName(card.setName);
				
			card.setName = newSetName;

			CardSet setIdentified = db.getFirstCardSetForCardInSet(card.cardName, newSetName);

			if (setIdentified == null) {
				System.out.println("Unknown setCode for card name and set: " + card.cardName + ":" + newSetName);
				continue;
			}

			card.gamePlayCardUUID = setIdentified.gamePlayCardUUID;
			card.setNumber = setIdentified.setNumber;

			ArrayList<SetMetaData> metaData = db.getSetMetaDataFromSetName(newSetName);

			if (metaData.size() != 1) {
				System.out.println("Unknown metaData for set: " + newSetName);
				continue;
			} else {
				card.setCode = metaData.get(0).setCode;
			}

			card.rarityUnsure = 0;

			try{
				db.updateOwnedCardByUUID(card);
				count++;
			}
			catch(Exception e) {
				e.printStackTrace();
			}
				

		}
		
		System.out.println("Updated " + count + " rows");
	}

}
