package com.example.ygodb.backend.process;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.csv.CSVRecord;

import com.example.ygodb.backend.bean.OwnedCard;
import com.example.ygodb.backend.connection.CsvConnection;
import com.example.ygodb.backend.connection.SQLiteConnection;

public class ReformatFromTCGPlayerForPurchase {

	public static void main(String[] args) throws SQLException, IOException {
		ReformatFromTCGPlayerForPurchase mainObj = new ReformatFromTCGPlayerForPurchase();
		mainObj.run();
		
	}

	public void run() throws SQLException, IOException {

		Iterator<CSVRecord> it = CsvConnection.getIterator(
				"C:\\Users\\Mike\\Documents\\GitHub\\YGO-DB\\YGO-DB\\csv\\TCGPlayer.csv", StandardCharsets.UTF_16LE);
		
		HashMap<String, OwnedCard> map = new HashMap<String, OwnedCard>();

		while (it.hasNext()) {

			CSVRecord current = it.next();

			OwnedCard card = CsvConnection.getOwnedCardFromTCGPlayerCSV(current);

			if (card != null) {

				String key = card.cardName;

				if (map.containsKey(key)) {
					map.get(key).quantity += card.quantity;
				} else {

					map.put(key, card);
				}

			}
		}

		for (OwnedCard card : map.values()) {
			System.out.println(card.quantity + " " + card.cardName);
		}

	}
	


}
