package process;

import java.sql.SQLException;
import java.util.ArrayList;

import bean.CardSet;
import bean.GamePlayCard;
import bean.OwnedCard;
import connection.SQLiteConnection;

public class AddFromOwnedCards {

	public void run(SQLiteConnection db) throws SQLException {
		ArrayList<OwnedCard> cards = db.getAllOwnedCards();

		for (OwnedCard card : cards) {

			card.cardName = card.cardName.trim();
			card.setName = card.setName.trim();

			GamePlayCard gamePlayCard = db.getGamePlayCardByNameAndID(card.id, card.cardName);

			if (gamePlayCard == null) {
				// check for skill card
				String newCardName = card.cardName + " (Skill Card)";

				gamePlayCard = db.getGamePlayCardByNameAndID(card.id, newCardName);

				if (gamePlayCard != null) {
					card.cardName = newCardName;
				} else {
					// add it
					System.out.println("No gamePlayCard found for " + card.cardName + ":" + card.id);

					GamePlayCard GPC = new GamePlayCard();

					GPC.cardName = card.cardName;
					GPC.cardType = "unknown";
					GPC.passcode = card.id;
					GPC.wikiID = card.id;

					db.replaceIntoGamePlayCard(GPC);

				}
			}

			ArrayList<CardSet> sets = db.getRaritiesOfCardInSetByIDAndName(card.id, card.setName,
					card.cardName);

			if (sets.size() == 0) {
				// add it
				System.out.println("No rarity entries found for " + card.cardName + ":" + card.id + ":" + card.setName);
				db.replaceIntoCardSet(card.setNumber, card.setRarity, card.setName, card.id, null,
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
					db.replaceIntoCardSet(card.setNumber, card.setRarity, card.setName, card.id, null,
							card.cardName);
				}
			}

		}

	}

}
