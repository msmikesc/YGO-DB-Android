package ygodb.windows.importer;

import ygodb.commonlibrary.bean.GamePlayCard;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.windows.utility.WindowsUtil;

import java.sql.SQLException;
import java.util.List;

public class DownloadCardImagesBySet {

	public static void main(String[] args) throws SQLException, InterruptedException {
		String setName = "Valiant Smashers";

		DownloadCardImagesBySet mainObj = new DownloadCardImagesBySet();

		SQLiteConnection db = WindowsUtil.getDBInstance();

		boolean successful = mainObj.run(db, setName);
		if (!successful) {
			YGOLogger.info("Import Failed");
		} else {
			YGOLogger.info("Import Finished");
		}
		db.closeInstance();
	}

	public boolean run(SQLiteConnection db, String setName) throws SQLException, InterruptedException {
		List<GamePlayCard> cardsList = db.getDistinctGamePlayCardsInSetByName(setName);

		if (cardsList == null || cardsList.isEmpty()) {
			YGOLogger.error("No Cards found in set:" + setName);
			return false;
		}
		else{
			YGOLogger.info("Downloading " + cardsList.size() + " card images for set:" + setName);
		}

		return WindowsUtil.downloadAllCardImagesForList(cardsList);
	}


}
