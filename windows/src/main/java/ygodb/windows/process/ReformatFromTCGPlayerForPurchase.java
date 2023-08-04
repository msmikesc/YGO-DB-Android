package ygodb.windows.process;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.commonlibrary.connection.CsvConnection;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.windows.utility.WindowsUtil;

public class ReformatFromTCGPlayerForPurchase {


	public static void main(String[] args) throws SQLException, IOException {
		ReformatFromTCGPlayerForPurchase mainObj = new ReformatFromTCGPlayerForPurchase();

		SQLiteConnection db = WindowsUtil.getDBInstance();

		mainObj.run(db);
		db.closeInstance();
	}

	public void run(SQLiteConnection db) throws SQLException, IOException {

		CsvConnection csvConnection = new CsvConnection();

		CSVParser parser = csvConnection.getParser("C:\\Users\\Mike\\Documents\\GitHub\\YGO-DB\\YGO-DB\\csv\\TCGPlayer.csv",
												   StandardCharsets.UTF_16LE);

		Iterator<CSVRecord> it = parser.iterator();

		HashMap<String, OwnedCard> map = new HashMap<>();

		while (it.hasNext()) {

			CSVRecord current = it.next();

			OwnedCard card = csvConnection.getOwnedCardFromTCGPlayerCSV(current, db);

			if (card != null) {

				String key = card.getCardName();

				if (map.containsKey(key)) {
					map.get(key).setQuantity(map.get(key).getQuantity() + card.getQuantity());
				} else {

					map.put(key, card);
				}

			}
		}

		parser.close();

		for (OwnedCard card : map.values()) {
			YGOLogger.info(card.getQuantity() + " " + card.getCardName());
		}

	}


}
