package com.example.ygodb.backend.process;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import com.example.ygodb.backend.bean.CardSet;
import com.example.ygodb.backend.bean.OwnedCard;
import com.example.ygodb.backend.bean.SetMetaData;
import com.example.ygodb.backend.connection.SQLiteConnection;
import com.example.ygodb.backend.connection.Util;

public class UpdateOwnedCardsBySetName {

	public static void main(String[] args) throws SQLException, IOException {
		UpdateOwnedCardsBySetName mainObj = new UpdateOwnedCardsBySetName();
		mainObj.run();
		
	}

	public void run() throws SQLException, IOException {

		ArrayList<OwnedCard> cards = SQLiteConnection.getObj().getAllOwnedCardsWithoutSetCode();
		
		int count = 0;

		for (OwnedCard card : cards) {
			
			String newSetName = Util.checkForTranslatedSetName(card.setName);
			
			if(!newSetName.equals(card.setName)) {
				
				card.setName = newSetName;
				
				CardSet setIdentified = SQLiteConnection.getObj().getFirstCardSetForCardInSet(card.cardName, newSetName);

				if (setIdentified == null) {
					System.out.println("Unknown setCode for card name and set: " + card.cardName + ":" + newSetName);
					continue;
				}
				
				card.id = setIdentified.id;
				card.setNumber = setIdentified.setNumber;

				ArrayList<SetMetaData> metaData = SQLiteConnection.getObj().getSetMetaDataFromSetName(newSetName);

				if (metaData.size() != 1) {
					System.out.println("Unknown metaData for set: " + newSetName);
					continue;
				} else {
					card.setCode = metaData.get(0).set_code;
				}
				
				card.rarityUnsure = 0;
				
				
				
				try{
					SQLiteConnection.getObj().UpdateOwnedCardByUUID(card);
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
