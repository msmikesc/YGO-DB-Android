package ygodb.windows.process;

import java.sql.SQLException;
import java.util.ArrayList;

import ygodb.commonlibrary.bean.SetMetaData;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.utility.Util;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.windows.utility.WindowsUtil;

public class UpdateSetNames {

	public static void main(String[] args) throws SQLException {
		UpdateSetNames mainObj = new UpdateSetNames();

		SQLiteConnection db = WindowsUtil.getDBInstance();

		mainObj.run(db);
		db.closeInstance();
		YGOLogger.info("Process complete");
	}

	public void run(SQLiteConnection db) throws SQLException {

		ArrayList<String> setsList = db.getDistinctSetNames();
		
		ArrayList<SetMetaData> metaData = db.getAllSetMetaDataFromSetData();
		
		for(SetMetaData meta: metaData) {
			if(!setsList.contains(meta.getSetName())) {
				setsList.add(meta.getSetName());
			}
		}

		for (String setName : setsList) {

			if (setName.contains("Tip Card") || setName.contains("(POR)")) {
				continue;
			}
			
			String newSetName = Util.checkForTranslatedSetName(setName);
			
			if(!newSetName.equals(setName)) {
				YGOLogger.info("Updating " + setName + " to " + newSetName);
				db.updateSetName(setName, newSetName);
			}
		}
	}

}
