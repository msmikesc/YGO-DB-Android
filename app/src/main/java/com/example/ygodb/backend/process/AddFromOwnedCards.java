package com.example.ygodb.backend.process;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import com.example.ygodb.backend.bean.CardSet;
import com.example.ygodb.backend.bean.GamePlayCard;
import com.example.ygodb.backend.bean.OwnedCard;
import com.example.ygodb.backend.connection.SQLiteConnection;

public class AddFromOwnedCards {

	public static void main(String[] args) throws SQLException, IOException {
		AddFromOwnedCards mainObj = new AddFromOwnedCards();
		mainObj.run();
		
		System.out.println("Process Complete");
	}

	private void run() throws SQLException {
		ArrayList<OwnedCard> cards = SQLiteConnection.getObj().getAllOwnedCards();

		for (OwnedCard card : cards) {

			card.cardName = card.cardName.trim();
			card.setName = card.setName.trim();

			GamePlayCard gamePlayCard = SQLiteConnection.getObj().getGamePlayCardByNameAndID(card.id, card.cardName);

			if (gamePlayCard == null) {
				// check for skill card
				String newCardName = card.cardName + " (Skill Card)";

				gamePlayCard = SQLiteConnection.getObj().getGamePlayCardByNameAndID(card.id, newCardName);

				if (gamePlayCard != null) {
					card.cardName = newCardName;
				} else {
					// add it
					System.out.println("No gamePlayCard found for " + card.cardName + ":" + card.id);

					SQLiteConnection.getObj().replaceIntoGamePlayCard(card.id, card.cardName, "unknown", card.id, null, null,
							null, null, null, null, null, null, null);

				}
			}

			ArrayList<CardSet> sets = SQLiteConnection.getObj().getRaritiesOfCardInSetByIDAndName(card.id, card.setName,
					card.cardName);

			if (sets.size() == 0) {
				// add it
				System.out.println("No rarity entries found for " + card.cardName + ":" + card.id + ":" + card.setName);
				SQLiteConnection.getObj().replaceIntoCardSet(card.setNumber, card.setRarity, card.setName, card.id, "0",
						card.cardName);
			} else {
				boolean match = false;

				for (CardSet set : sets) {
					if (set.setRarity.equalsIgnoreCase(card.setRarity)
							&& set.setNumber.equalsIgnoreCase(card.setNumber)) {
						match = true;
						break;
					}
				}

				if (!match) {
					// add it
					System.out.println("No matching rarity entries found for " + card.cardName + ":" + card.id + ":"
							+ card.setName);
					SQLiteConnection.getObj().replaceIntoCardSet(card.setNumber, card.setRarity, card.setName, card.id, "0",
							card.cardName);
				}
			}

		}

	}

}
