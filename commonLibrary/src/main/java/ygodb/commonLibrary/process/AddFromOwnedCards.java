package ygodb.commonLibrary.process;

import java.sql.SQLException;
import java.util.ArrayList;

import ygodb.commonLibrary.bean.CardSet;
import ygodb.commonLibrary.bean.GamePlayCard;
import ygodb.commonLibrary.bean.OwnedCard;
import ygodb.commonLibrary.connection.SQLiteConnection;
import ygodb.commonLibrary.constant.Const;
import ygodb.commonLibrary.utility.YGOLogger;

public class AddFromOwnedCards {

	public void run(SQLiteConnection db) throws SQLException {
		ArrayList<OwnedCard> cards = db.getAllOwnedCards();

		for (OwnedCard card : cards) {

			card.cardName = card.cardName.trim();
			card.setName = card.setName.trim();

			GamePlayCard gamePlayCard = db.getGamePlayCardByUUID(card.gamePlayCardUUID);

			if (gamePlayCard == null) {
				// check for skill card
				String newCardName = card.cardName + Const.SKILL_CARD_NAME_APPEND;

				gamePlayCard = db.getGamePlayCardByUUID(card.gamePlayCardUUID);

				if (gamePlayCard != null) {
					card.cardName = newCardName;
				} else {
					// add it
					YGOLogger.info("No gamePlayCard found for " + card.cardName + ":" + card.gamePlayCardUUID);

					GamePlayCard gamePlayCard1 = new GamePlayCard();

					gamePlayCard1.cardName = card.cardName;
					gamePlayCard1.gamePlayCardUUID = card.gamePlayCardUUID;
					gamePlayCard1.archetype = Const.ARCHETYPE_AUTOGENERATE;
					gamePlayCard1.passcode = card.passcode;

					db.replaceIntoGamePlayCard(gamePlayCard1);

				}
			}

			ArrayList<CardSet> sets = db.getRaritiesOfCardInSetByGamePlayCardUUID(card.gamePlayCardUUID, card.setName
			);

			if (sets.isEmpty()) {
				// add it
				YGOLogger.info("No rarity entries found for " + card.cardName + ":" + card.gamePlayCardUUID + ":" + card.setName);
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
					YGOLogger.info("No matching rarity entries found for " + card.cardName + ":" + card.gamePlayCardUUID + ":"
							+ card.setName);
					db.replaceIntoCardSetWithSoftPriceUpdate(card.setNumber, card.setRarity, card.setName, card.gamePlayCardUUID, null,
							card.cardName);
				}
			}

		}

	}

}
