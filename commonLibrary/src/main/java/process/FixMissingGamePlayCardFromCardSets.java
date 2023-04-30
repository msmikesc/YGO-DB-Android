package process;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import bean.CardSet;
import bean.GamePlayCard;
import connection.SQLiteConnection;
import connection.Util;

public class FixMissingGamePlayCardFromCardSets {

	/*
	public static void main(String[] args) throws SQLException, IOException {
		FixMissingGamePlayCardFromCardSets mainObj = new FixMissingGamePlayCardFromCardSets();
		mainObj.run();
		db.closeInstance();
		System.out.println("Analyze complete");
	}*/

	public void run(SQLiteConnection db) throws SQLException, IOException {

		ArrayList<String> setsList = db.getDistinctSetNames();

		for (String setName : setsList) {

			if (setName.contains("Tip Card") || setName.contains("(POR)")) {
				continue;
			}

			ArrayList<Integer> list = db.getDistinctCardIDsInSetByName(setName);
			for (int i : list) {
				ArrayList<String> titles = db.getMultiCardTitlesFromID(i);
				
				ArrayList<CardSet> cardSets = db.getAllCardSetsOfCardByIDAndSet(i, setName);
				
				if(titles == null || titles.size() == 0) {
					System.out.println("0 gameplaycard found for ID " + i + " " + cardSets.get(0).cardName);
					
					GamePlayCard current = new GamePlayCard();
					
					current.cardName = cardSets.get(0).cardName;
					current.archetype = "autogenerated";
					current.passcode = cardSets.get(0).id;
					current.wikiID = cardSets.get(0).id;
					
					db.replaceIntoGamePlayCard(current);
				}
				
			}

		}

	}

}