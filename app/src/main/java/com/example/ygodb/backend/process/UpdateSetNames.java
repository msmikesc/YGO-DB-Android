package com.example.ygodb.backend.process;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import com.example.ygodb.backend.bean.SetMetaData;
import com.example.ygodb.backend.connection.SQLiteConnection;
import com.example.ygodb.backend.connection.Util;

public class UpdateSetNames {

	public static void main(String[] args) throws SQLException, IOException {
		UpdateSetNames mainObj = new UpdateSetNames();
		mainObj.run();
		
	}

	public void run() throws SQLException, IOException {

		ArrayList<String> setsList = SQLiteConnection.getObj().getDistinctSetNames();
		
		ArrayList<SetMetaData> metaData = SQLiteConnection.getObj().getAllSetMetaDataFromSetData();
		
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
				SQLiteConnection.getObj().updateSetName(setName, newSetName);
			}

			

		}
		

	}

}
