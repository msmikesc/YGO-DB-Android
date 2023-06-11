package ygodb.windows.process;

import java.sql.SQLException;
import java.util.ArrayList;

import ygodb.commonLibrary.bean.SetMetaData;
import ygodb.commonLibrary.connection.SQLiteConnection;
import ygodb.commonLibrary.utility.YGOLogger;
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
			if(!setsList.contains(meta.setName)) {
				setsList.add(meta.setName);
			}
		}

		for (String setName : setsList) {

			if (setName.contains("Tip Card") || setName.contains("(POR)")) {
				continue;
			}
			
			String newSetName = WindowsUtil.checkForTranslatedSetName(setName);
			
			if(!newSetName.equals(setName)) {
				YGOLogger.info("Updating " + setName + " to " + newSetName);
				db.updateSetName(setName, newSetName);
			}
		}
	}

}
