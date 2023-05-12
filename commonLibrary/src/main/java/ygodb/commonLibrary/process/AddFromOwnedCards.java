package ygodb.commonLibrary.process;

import java.sql.SQLException;
import java.util.ArrayList;

import ygodb.commonLibrary.bean.CardSet;
import ygodb.commonLibrary.bean.GamePlayCard;
import ygodb.commonLibrary.bean.OwnedCard;
import ygodb.commonLibrary.connection.SQLiteConnection;

public class AddFromOwnedCards {

	public void run(SQLiteConnection db) throws SQLException {
		ArrayList<OwnedCard> cards = db.getAllOwnedCards();

		for (OwnedCard card : cards) {

			card.cardName = card.cardName.trim();
			card.setName = card.setName.trim();

			GamePlayCard gamePlayCard = db.getGamePlayCardByUUID(card.gamePlayCardUUID);

			if (gamePlayCard == null) {
				// check for skill card
				String newCardName = card.cardName + " (Skill Card)";

				gamePlayCard = db.getGamePlayCardByUUID(card.gamePlayCardUUID);

				if (gamePlayCard != null) {
					card.cardName = newCardName;
				} else {
					// add it
					System.out.println("No gamePlayCard found for " + card.cardName + ":" + card.gamePlayCardUUID);

					GamePlayCard gamePlayCard1 = new GamePlayCard();

					gamePlayCard1.cardName = card.cardName;
					gamePlayCard1.cardType = "unknown";
					gamePlayCard1.gamePlayCardUUID = card.gamePlayCardUUID;

					db.replaceIntoGamePlayCard(gamePlayCard1);

				}
			}

			ArrayList<CardSet> sets = db.getRaritiesOfCardInSetByGamePlayCardUUID(card.gamePlayCardUUID, card.setName
			);

			if (sets.isEmpty()) {
				// add it
				System.out.println("No rarity entries found for " + card.cardName + ":" + card.gamePlayCardUUID + ":" + card.setName);
				db.replaceIntoCardSetWithSoftPriceUpdate(card.setNumber, card.setRarity, card.setName, card.gamePlayCardUUID, null,
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
					System.out.println("No matching rarity entries found for " + card.cardName + ":" + card.gamePlayCardUUID + ":"
							+ card.setName);
					db.replaceIntoCardSetWithSoftPriceUpdate(card.setNumber, card.setRarity, card.setName, card.gamePlayCardUUID, null,
							card.cardName);
				}
			}

		}

	}

}
