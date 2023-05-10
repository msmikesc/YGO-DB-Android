package ygodb.windows.process;

import java.sql.SQLException;
import java.util.ArrayList;

import ygodb.commonLibrary.bean.SetMetaData;
import ygodb.commonLibrary.connection.SQLiteConnection;
import ygodb.commonLibrary.utility.Util;
import ygodb.windows.connection.WindowsUtil;

public class UpdateSetNames {

	public static void main(String[] args) throws SQLException {
		UpdateSetNames mainObj = new UpdateSetNames();

		SQLiteConnection db = WindowsUtil.getDBInstance();

		mainObj.run(db);
		db.closeInstance();
		System.out.println("Process complete");
	}

	public void run(SQLiteConnection db) throws SQLException {

		ArrayList<String> setsList = db.getDistinctSetNames();
		
		ArrayList<SetMetaData> metaData = db.getAllSetMetaDataFromSetData();
		
		for(SetMetaData meta: metaData) {
			if(!setsList.contains(meta.set_name)) {
				setsList.add(meta.set_name);
			}
		}

		for (String setName : setsList) {

			if (setName.contains("Tip Card") || setName.contains("(POR)")) {
				continue;
			}
			
			String newSetName = Util.checkForTranslatedSetName(setName);
			
			if(!newSetName.equals(setName)) {
				System.out.println("Updating " + setName + " to " + newSetName);
				db.updateSetName(setName, newSetName);
			}
		}
	}

}
