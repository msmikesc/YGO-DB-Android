package ygodb.windows.importer;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import ygodb.commonlibrary.bean.SetMetaData;
import ygodb.commonlibrary.connection.CsvConnection;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.Util;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.windows.utility.WindowsUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ImportCardSetFromCSV {
	public static void main(String[] args) throws SQLException, IOException {
		ImportCardSetFromCSV mainObj = new ImportCardSetFromCSV();

		SQLiteConnection db = WindowsUtil.getDBInstance();

		String filename = "cardsets.csv";
		String resourcePath = Const.CSV_IMPORT_FOLDER + filename;

		mainObj.run(db, resourcePath);
		db.closeInstance();
		YGOLogger.info("Import Complete");
	}

	public void run(SQLiteConnection db, String resourcePath) throws SQLException, IOException {

		CsvConnection csvConnection = new CsvConnection();

		CSVParser parser = csvConnection.getParser(resourcePath, StandardCharsets.UTF_16LE);

		Map<String, Set<String>> setNameToCardNumbers = new HashMap<>();

		for (CSVRecord current : parser) {
			csvConnection.insertCardSetFromCSV(current, db);

			String setName = csvConnection.getSetNameFromCSVRecord(current);
			String cardNumber = csvConnection.getCardNumberFromCSVRecord(current);

			setNameToCardNumbers.computeIfAbsent(setName, k -> new HashSet<>());
			setNameToCardNumbers.get(setName).add(cardNumber);
		}

		handleCreatingNewSetMetaData(db, setNameToCardNumbers);

		parser.close();

	}

	private static void handleCreatingNewSetMetaData(SQLiteConnection db, Map<String, Set<String>> setNameToCardNumbers) throws SQLException {
		List<SetMetaData> list = db.getAllSetMetaDataFromSetData();
		HashMap<String, SetMetaData> setMetaDataHashMap = new HashMap<>();
		for (SetMetaData s : list) {
			setMetaDataHashMap.put(s.getSetName(), s);
		}

		for(Map.Entry<String, Set<String>> setEntry: setNameToCardNumbers.entrySet()){
			if(setMetaDataHashMap.get(setEntry.getKey()) == null){
				//Entry does not yet exist, create it
				Set<String> cardNumbers = setNameToCardNumbers.get(setEntry.getKey());
				int setCount = cardNumbers.size();
				String setPrefix = Util.getPrefixFromSetNumber(cardNumbers.iterator().next());

				db.replaceIntoCardSetMetaData(setEntry.getKey(), setPrefix, setCount, null);
			}
		}
	}

}
