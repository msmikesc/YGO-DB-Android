package ygodb.windows.process;

import java.sql.SQLException;
import java.util.ArrayList;

import ygodb.commonLibrary.bean.CardSet;
import ygodb.commonLibrary.bean.GamePlayCard;
import ygodb.commonLibrary.connection.SQLiteConnection;
import ygodb.commonLibrary.constant.Const;
import ygodb.windows.connection.WindowsUtil;

public class FixMissingGamePlayCardFromCardSets {

	public static void main(String[] args) throws SQLException {
		FixMissingGamePlayCardFromCardSets mainObj = new FixMissingGamePlayCardFromCardSets();

		SQLiteConnection db = WindowsUtil.getDBInstance();

		mainObj.run(db);
		db.closeInstance();
		System.out.println("Analyze complete");
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
					System.out.println("0 gameplaycard found for ID " + gamePlayCardUUID + " " + cardSets.get(0).cardName);
					
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
