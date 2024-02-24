package ygodb.windows.process;

import ygodb.commonlibrary.bean.CardSet;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.Util;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.windows.utility.WindowsUtil;

import java.sql.SQLException;
import java.util.List;

public class UpdateOwnedCardsWithoutPriceBought {

	public static void main(String[] args) throws SQLException {
		UpdateOwnedCardsWithoutPriceBought mainObj = new UpdateOwnedCardsWithoutPriceBought();

		SQLiteConnection db = WindowsUtil.getDBInstance();

		mainObj.run(db);
		db.closeInstance();
	}

	public void run(SQLiteConnection db) throws SQLException {

		List<OwnedCard> cards = db.getAllOwnedCardsWithoutPriceBought();

		int count = 0;

		for (OwnedCard card : cards) {

			CardSet setIdentified = db.getRarityOfCardInSetByNumberAndRarity(
					card.getGamePlayCardUUID(), card.getSetNumber(), card.getSetRarity(), card.getColorVariant());

			String priceBought;

			if(card.getEditionPrinting().equals(Const.CARD_PRINTING_FIRST_EDITION)){
				priceBought = setIdentified.getSetPriceFirst();
			}
			else if(card.getEditionPrinting().equals(Const.CARD_PRINTING_LIMITED)){
				priceBought = setIdentified.getSetPriceLimited();
			}
			else{
				priceBought = setIdentified.getSetPrice();
			}

			if(priceBought == null || Util.normalizePrice(priceBought).equals(Const.ZERO_PRICE_STRING)){
				continue;
			}

			card.setPriceBought(priceBought);

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
