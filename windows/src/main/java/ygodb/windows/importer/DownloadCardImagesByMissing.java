package ygodb.windows.importer;

import ygodb.commonlibrary.bean.GamePlayCard;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.windows.utility.WindowsUtil;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadCardImagesByMissing {

	public static void main(String[] args) throws SQLException, InterruptedException {

		DownloadCardImagesByMissing mainObj = new DownloadCardImagesByMissing();

		SQLiteConnection db = WindowsUtil.getDBInstance();

		boolean successful = mainObj.run(db);
		if (!successful) {
			YGOLogger.info("Import Failed");
		} else {
			YGOLogger.info("Import Finished");
		}
		db.closeInstance();
	}

	public boolean run(SQLiteConnection db) throws SQLException, InterruptedException {
		//get alt art ids UNION passcode
		Set<Integer> dbPasscodes = new HashSet<>(db.getAllArtPasscodes());

		Set<Integer> fileSystemPicCodes = getIntegersFromFilenames(
				"C:\\Users\\Mike\\AndroidStudioProjects\\YGODB\\app\\src\\main\\assets\\pics");

		dbPasscodes.removeAll(fileSystemPicCodes);

		List<GamePlayCard> artCardsList = new ArrayList<>();

		for (Integer passcode : dbPasscodes) {
			GamePlayCard newCard = new GamePlayCard();
			newCard.setCardName(String.valueOf(passcode));
			newCard.setPasscode(passcode);
			artCardsList.add(newCard);
		}

		return WindowsUtil.downloadAllCardImagesForList(artCardsList);
	}

	public static Set<Integer> getIntegersFromFilenames(String directoryPath) {
		HashSet<Integer> integers = new HashSet<>();
		File directory = new File(directoryPath);

		if (!directory.isDirectory()) {
			YGOLogger.info("The provided path is not a directory.");
			return integers;
		}

		// Regex pattern to match filenames like "<positiveinteger>.jpg"
		Pattern pattern = Pattern.compile("^(\\d+)\\.jpg$");

		File[] files = directory.listFiles();
		if (files != null) {
			for (File file : files) {
				Matcher matcher = pattern.matcher(file.getName());
				if (matcher.matches()) {
					integers.add(Integer.parseInt(matcher.group(1)));
				}
			}
		}

		return integers;
	}


}
