package process;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import bean.CardSet;
import bean.OwnedCard;
import bean.SetMetaData;
import connection.SQLiteConnection;
import connection.Util;

public class UpdateOwnedCardsBySetName {

	/*
	public static void main(String[] args) throws SQLException, IOException {
		UpdateOwnedCardsBySetName mainObj = new UpdateOwnedCardsBySetName();
		mainObj.run();
		SQLiteConnection.closeInstance();
	}*/

	public void run(SQLiteConnection db) throws SQLException, IOException {

		ArrayList<OwnedCard> cards = db.getAllOwnedCardsWithoutSetCode();
		
		int count = 0;

		for (OwnedCard card : cards) {
			
			String newSetName = Util.checkForTranslatedSetName(card.setName);
			
			if(!newSetName.equals(card.setName)) {
				
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
		}
		
		System.out.println("Updated " + count + " rows");
	}

}
