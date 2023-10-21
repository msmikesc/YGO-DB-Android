package ygodb.windows.process;

import ygodb.commonlibrary.bean.CardSet;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.utility.Util;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.windows.utility.WindowsUtil;

import java.sql.SQLException;
import java.util.List;

public class UpdateOwnedCardsWithoutPasscode {

	public static void main(String[] args) throws SQLException {
		UpdateOwnedCardsWithoutPasscode mainObj = new UpdateOwnedCardsWithoutPasscode();

		SQLiteConnection db = WindowsUtil.getDBInstance();

		mainObj.run(db);
		db.closeInstance();
	}

	public void run(SQLiteConnection db) throws SQLException {

		List<OwnedCard> cards = db.getAllOwnedCardsWithoutPasscode();

		int count = 0;

		for (OwnedCard card : cards) {

			String newSetName = Util.checkForTranslatedSetName(card.getSetName());

			card.setSetName(newSetName);
			card.setCardName(Util.checkForTranslatedCardName(card.getCardName()));

			CardSet setIdentified = db.getFirstCardSetForCardInSet(card.getCardName(), newSetName);

			if (setIdentified == null) {
				YGOLogger.info("Unknown set number for card name and set: " + card.getCardName() + ":" + newSetName);
				continue;
			}

			Integer passcode = db.getPasscodeFromGamePlayCardUUID(setIdentified.getGamePlayCardUUID());

			if(passcode == null || passcode == 0){
				YGOLogger.info("Unknown passcode for card name and set: " + card.getCardName() + ":" + newSetName);
				passcode = 0;
			}

			if(card.getPasscode() == passcode){
				//skip, nothing to update
				continue;
			}

			card.setGamePlayCardUUID(setIdentified.getGamePlayCardUUID());
			card.setSetNumber(setIdentified.getSetNumber());
			card.setSetPrefix(setIdentified.getSetPrefix());
			card.setRarityUnsure(0);
			card.setPasscode(passcode);

			try {
				db.updateOwnedCardByUUID(card);
				count++;
			} catch (Exception e) {
				YGOLogger.logException(e);
			}


		}

		YGOLogger.info("Updated " + count + " rows");
	}

}
