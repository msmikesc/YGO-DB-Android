package ygodb.windows.process;

import java.sql.SQLException;
import java.util.ArrayList;

import ygodb.commonlibrary.bean.CardSet;
import ygodb.commonlibrary.bean.GamePlayCard;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.windows.utility.WindowsUtil;

public class FixMissingGamePlayCardFromCardSets {

	public static void main(String[] args) throws SQLException {
		FixMissingGamePlayCardFromCardSets mainObj = new FixMissingGamePlayCardFromCardSets();

		SQLiteConnection db = WindowsUtil.getDBInstance();

		mainObj.run(db);
		db.closeInstance();
		YGOLogger.info("Analyze complete");
	}

	public void run(SQLiteConnection db) throws SQLException {

		ArrayList<String> setsList = db.getDistinctSetNames();

		for (String setName : setsList) {

			if (setName.contains("Tip Card") || setName.contains("(POR)")) {
				continue;
			}

			ArrayList<String> list = db.getDistinctGamePlayCardUUIDsInSetByName(setName);
			for (String gamePlayCardUUID : list) {
				ArrayList<String> titles = db.getMultipleCardNamesFromGamePlayCardUUID(gamePlayCardUUID);
				
				ArrayList<CardSet> cardSets = db.getAllCardSetsOfCardByGamePlayCardUUIDAndSet(gamePlayCardUUID, setName);
				
				if(titles == null || titles.isEmpty()) {
					YGOLogger.info("0 gameplaycard found for ID " + gamePlayCardUUID + " " + cardSets.get(0).cardName);
					
					GamePlayCard current = new GamePlayCard();
					
					current.cardName = cardSets.get(0).cardName;
					current.archetype = Const.ARCHETYPE_AUTOGENERATE;
					current.gamePlayCardUUID = gamePlayCardUUID;
					
					db.replaceIntoGamePlayCard(current);
				}
				
			}

		}

	}

}
