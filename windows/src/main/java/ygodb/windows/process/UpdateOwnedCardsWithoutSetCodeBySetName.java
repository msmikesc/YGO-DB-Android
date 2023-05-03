package ygodb.windows.process;

import java.sql.SQLException;
import java.util.ArrayList;

import ygodb.commonLibrary.bean.CardSet;
import ygodb.commonLibrary.bean.OwnedCard;
import ygodb.commonLibrary.bean.SetMetaData;
import ygodb.commonLibrary.connection.SQLiteConnection;
import ygodb.commonLibrary.connection.Util;
import ygodb.windows.connection.WindowsUtil;

public class UpdateOwnedCardsWithoutSetCodeBySetName {

	public static void main(String[] args) throws SQLException {
		UpdateOwnedCardsWithoutSetCodeBySetName mainObj = new UpdateOwnedCardsWithoutSetCodeBySetName();

		SQLiteConnection db = WindowsUtil.getDBInstance();

		mainObj.run(db);
		db.closeInstance();
	}

	public void run(SQLiteConnection db) throws SQLException {

		ArrayList<OwnedCard> cards = db.getAllOwnedCardsWithoutSetCode();
		
		int count = 0;

		for (OwnedCard card : cards) {
			
			String newSetName = Util.checkForTranslatedSetName(card.setName);
				
			card.setName = newSetName;

			CardSet setIdentified = db.getFirstCardSetForCardInSet(card.cardName, newSetName);

			if (setIdentified == null) {
				System.out.println("Unknown setCode for card name and set: " + card.cardName + ":" + newSetName);
				continue;
			}

			card.id = setIdentified.id;
			card.setNumber = setIdentified.setNumber;

			ArrayList<SetMetaData> metaData = db.getSetMetaDataFromSetName(newSetName);

			if (metaData.size() != 1) {
				System.out.println("Unknown metaData for set: " + newSetName);
				continue;
			} else {
				card.setCode = metaData.get(0).set_code;
			}

			card.rarityUnsure = 0;

			try{
				db.UpdateOwnedCardByUUID(card);
				count++;
			}
			catch(Exception e) {
				e.printStackTrace();
			}
				

		}
		
		System.out.println("Updated " + count + " rows");
	}

}
