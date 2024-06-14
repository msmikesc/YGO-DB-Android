package ygodb.windows.utility;

import ygodb.commonlibrary.bean.GamePlayCard;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.ApiUtil;
import ygodb.commonlibrary.utility.Util;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.windows.connection.SQLiteConnectionWindows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class WindowsUtil {

	private WindowsUtil() {
	}

	private static SQLiteConnectionWindows dbInstance = null;

	public static SQLiteConnectionWindows getDBInstance() {
		if (dbInstance == null) {
			dbInstance = new SQLiteConnectionWindows();
		}

		return dbInstance;
	}


	public static boolean downloadAllCardImagesForList(List<GamePlayCard> cardsList) throws InterruptedException {
		boolean anyFailed = false;
		int downloadCount = 0;

		for (GamePlayCard card : cardsList) {

			if(card.getPasscode() < 1){
				YGOLogger.info("Skipping image for:" + card.getCardName());
				continue;
			}

			int destinationPasscode = Util.checkForTranslatedYgoProImagePasscode(card.getPasscode());

			Path filePath = Paths.get(Const.PROJECT_CARD_IMAGES_DIRECTORY, destinationPasscode + ".jpg");
			boolean successful = ApiUtil.downloadCardImageFromYGOPRO(card, filePath);

			if (!successful) {
				anyFailed = true;
			}
			else{
				downloadCount++;
			}

			//Don't flood the api
			Thread.sleep(500);
		}
		YGOLogger.info("Downloaded this many images:" + downloadCount);

		return !anyFailed;
	}
}
