package ygodb.windows.importer;

import ygodb.commonlibrary.bean.GamePlayCard;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.windows.utility.WindowsUtil;

import java.sql.SQLException;
import java.util.List;

public class DownloadCardImagesByCardName {

	public static void main(String[] args) throws SQLException, InterruptedException {
		String cardName = "Monster Reborn";

		DownloadCardImagesByCardName mainObj = new DownloadCardImagesByCardName();

		SQLiteConnection db = WindowsUtil.getDBInstance();

		boolean successful = mainObj.run(db, cardName);
		if (!successful) {
			YGOLogger.info("Import Failed");
		} else {
			YGOLogger.info("Import Finished");
		}
		db.closeInstance();
	}

	public boolean run(SQLiteConnection db, String setName) throws SQLException, InterruptedException {
		//get alt art ids UNION passcode
		//
		// List<GamePlayCard> cardsList = db.getDistinctGamePlayCardsInSetByName(setName);

		if(cardsList == null || cardsList.isEmpty()){
			YGOLogger.error("No Cards found in set:" + setName);
			return false;
		}

		return WindowsUtil.downloadAllCardImagesForList(cardsList);
	}


}
