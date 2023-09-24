package ygodb.windows.importer;

import ygodb.commonlibrary.bean.GamePlayCard;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.utility.Util;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.windows.utility.WindowsUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DownloadCardImagesByCardName {

	public static void main(String[] args) throws SQLException, InterruptedException {
		String cardName = "Last Day Of Witch";

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

		if (codes == null || codes.isEmpty()) {
			YGOLogger.error("No Cards found for name:" + cardName);
			return false;
		}

		Set<Integer> codeSet = new HashSet<>(codes);

		for (Integer passcode : codes) {
			int manualUpdatePasscode = Util.checkForTranslatedYgoProImagePasscode(passcode);
			if(manualUpdatePasscode != passcode){
				codeSet.add(manualUpdatePasscode);
			}
		}

		codes = new ArrayList<>(codeSet);

		List<GamePlayCard> artCardsList = new ArrayList<>();

		for (Integer passcode : codes) {
			GamePlayCard newCard = card.clone();
			newCard.setPasscode(passcode);
			artCardsList.add(newCard);
		}

		return WindowsUtil.downloadAllCardImagesForList(artCardsList);
	}


}
