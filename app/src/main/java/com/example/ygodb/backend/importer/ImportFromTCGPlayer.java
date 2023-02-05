package com.example.ygodb.backend.importer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.csv.CSVRecord;

import com.example.ygodb.backend.bean.OwnedCard;
import com.example.ygodb.backend.connection.CsvConnection;
import com.example.ygodb.backend.connection.DatabaseHashMap;
import com.example.ygodb.backend.connection.SQLiteConnection;
import com.example.ygodb.backend.connection.Util;

public class ImportFromTCGPlayer {

	public static void main(String[] args) throws SQLException, IOException {
		ImportFromTCGPlayer mainObj = new ImportFromTCGPlayer();
		mainObj.run();
		
		System.out.println("Import Complete");
	}

	public void run() throws SQLException, IOException {

		Iterator<CSVRecord> it = CsvConnection.getIterator(
				"C:\\Users\\Mike\\Documents\\GitHub\\YGO-DB\\YGO-DB\\csv\\TCGPlayer.csv", StandardCharsets.UTF_16LE);

		HashMap<String, OwnedCard> map = new HashMap<String, OwnedCard>();

		int count = 0;

		while (it.hasNext()) {

			CSVRecord current = it.next();

			OwnedCard card = CsvConnection.getOwnedCardFromTCGPlayerCSV(current);

			if (card != null) {

				count += card.quantity;

				String key = card.setNumber + Util.normalizePrice(card.priceBought) + card.dateBought + card.folderName
						+ card.condition + card.editionPrinting;

				if (map.containsKey(key)) {
					map.get(key).quantity += card.quantity;
				} else {

					ArrayList<OwnedCard> ownedRarities = DatabaseHashMap.getExistingOwnedRaritesForCardFromHashMap(
							card.setNumber, card.priceBought, card.dateBought, card.folderName, card.condition,
							card.editionPrinting);

					for (OwnedCard existingCard : ownedRarities) {
						if (Util.doesCardExactlyMatchWithColor(card.folderName, card.cardName, card.setCode,
								card.setNumber, card.condition, card.editionPrinting, card.priceBought, card.dateBought,
								card.colorVariant, existingCard)) {
							card.quantity += existingCard.quantity;
							card.UUID = existingCard.UUID;
							break;
						}
					}

					map.put(key, card);
				}

			}
		}

		for (OwnedCard card : map.values()) {
			SQLiteConnection.getObj().upsertOwnedCardBatch(card);
		}
		
		

		System.out.println("Imported " + count + " cards");
		System.out.println("Total cards: "+SQLiteConnection.getObj().getCountQuantity() + " + " + SQLiteConnection.getObj().getCountQuantityManual() + " Manual");

	}

}
