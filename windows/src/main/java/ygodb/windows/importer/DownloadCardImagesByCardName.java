package ygodb.windows.importer;

import ygodb.commonlibrary.bean.GamePlayCard;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.windows.utility.WindowsUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DownloadCardImagesByCardName {

	public static void main(String[] args) throws SQLException, InterruptedException {
		String cardName = "Gilford the Lightning";

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

	public boolean run(SQLiteConnection db, String cardName) throws SQLException, InterruptedException {
		//get alt art ids UNION passcode
		List<Integer> codes = db.getAllArtPasscodesByName(cardName);

		String gpcUUID = db.getGamePlayCardUUIDFromTitle(cardName);

		GamePlayCard card = db.getGamePlayCardByUUID(gpcUUID);

		if(codes == null || codes.isEmpty()){
			YGOLogger.error("No Cards found for name:" + cardName);
			return false;
		}

		List<GamePlayCard> artCardsList = new ArrayList<>();

		for(Integer passcode: codes){
			GamePlayCard newCard = card.clone();
			newCard.setPasscode(passcode);
			artCardsList.add(newCard);
		}

		return WindowsUtil.downloadAllCardImagesForList(artCardsList);
	}


}
