package ygodb.commonlibrary.process;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ygodb.commonlibrary.bean.CardSet;
import ygodb.commonlibrary.bean.GamePlayCard;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.YGOLogger;

public class AddFromOwnedCards {

	public void run(SQLiteConnection db) throws SQLException {
		List<OwnedCard> cards = db.getAllOwnedCards();

		for (OwnedCard card : cards) {

			card.setCardName(card.getCardName().trim());
			card.setSetName(card.getSetName().trim());

			GamePlayCard gamePlayCard = db.getGamePlayCardByUUID(card.getGamePlayCardUUID());

			if (gamePlayCard == null) {
				// check for skill card
				String newCardName = card.getCardName() + Const.SKILL_CARD_NAME_APPEND;

				gamePlayCard = db.getGamePlayCardByUUID(card.getGamePlayCardUUID());

				if (gamePlayCard != null) {
					card.setCardName(newCardName);
				} else {
					// add it
					YGOLogger.info("No gamePlayCard found for " + card.getCardName() + ":" + card.getGamePlayCardUUID());

					GamePlayCard gamePlayCard1 = new GamePlayCard();

					gamePlayCard1.setCardName(card.getCardName());
					gamePlayCard1.setGamePlayCardUUID(card.getGamePlayCardUUID());
					gamePlayCard1.setArchetype(Const.ARCHETYPE_AUTOGENERATE);
					gamePlayCard1.setPasscode(card.getPasscode());

					db.replaceIntoGamePlayCard(gamePlayCard1);

				}
			}

			List<CardSet> sets = db.getRaritiesOfCardInSetByGamePlayCardUUID(card.getGamePlayCardUUID(),
					card.getSetName());

			if (sets.isEmpty()) {
				// add it
				YGOLogger.info("No rarity entries found for " + card.getCardName() + ":" + card.getGamePlayCardUUID() + ":" + card.getSetName());
				db.insertOrIgnoreIntoCardSet(card.getSetNumber(), card.getSetRarity(), card.getSetName(), card.getGamePlayCardUUID(),
						card.getCardName(), card.getColorVariant(), null);
			} else {
				boolean match = false;

				for (CardSet set : sets) {
					if (set.getSetRarity().equalsIgnoreCase(card.getSetRarity())
							&& set.getSetNumber().equalsIgnoreCase(card.getSetNumber())) {
						match = true;
						break;
					}
				}

				if (!match) {
					// add it
					YGOLogger.info("No matching rarity entries found for " + card.getCardName() + ":" + card.getGamePlayCardUUID() + ":"
							+ card.getSetName());
					db.insertOrIgnoreIntoCardSet(card.getSetNumber(), card.getSetRarity(), card.getSetName(), card.getGamePlayCardUUID(),
							card.getCardName(), card.getColorVariant(), null);
				}
			}

		}

	}

}
