package ygodb.windows.importer;

import ygodb.commonlibrary.bean.GamePlayCard;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.ApiUtil;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.windows.utility.WindowsUtil;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;

public class DownloadCardImagesFromYGOPRO {

	public static void main(String[] args) throws SQLException, InterruptedException {
		String setName = "Photon Hypernova";

		DownloadCardImagesFromYGOPRO mainObj = new DownloadCardImagesFromYGOPRO();

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

		if(cardsList == null || cardsList.isEmpty()){
			YGOLogger.error("No Cards found in set:" + setName);
			return false;
		}

		boolean anyFailed = false;

		for(GamePlayCard card: cardsList){
			Path filePath = Paths.get(Const.PROJECT_CARD_IMAGES_DIRECTORY, card.getPasscode() + ".jpg");
			boolean successful = ApiUtil.downloadCardImageFromYGOPRO(card, filePath);

			if(!successful){
				anyFailed = true;
			}

			//Don't flood the api
			Thread.sleep(500);
		}

		return !anyFailed;
	}



}
