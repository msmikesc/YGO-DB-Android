package com.example.ygodb.backend.importer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.csv.CSVRecord;

import com.example.ygodb.backend.bean.OwnedCard;
import com.example.ygodb.backend.connection.CsvConnection;
import com.example.ygodb.backend.connection.DatabaseHashMap;
import com.example.ygodb.backend.connection.SQLiteConnection;
import com.example.ygodb.backend.connection.Util;

public class ImportOwnedCardFromExportedCSV {

	public static void main(String[] args) throws SQLException, IOException {
		ImportOwnedCardFromExportedCSV mainObj = new ImportOwnedCardFromExportedCSV();
		mainObj.run();
		
		System.out.println("Import Complete");
	}

	public void run() throws SQLException, IOException {

		Iterator<CSVRecord> it = CsvConnection.getIterator(
				"C:\\Users\\Mike\\Documents\\GitHub\\YGO-DB\\YGO-DB\\csv\\rarity-unsure-export.csv",
				StandardCharsets.UTF_16LE);
		
		int count = 0;

		while (it.hasNext()) {

			CSVRecord current = it.next();

			OwnedCard card = CsvConnection.getOwnedCardFromExportedCSV(current);
			
			ArrayList<OwnedCard> ownedRarities = DatabaseHashMap.getExistingOwnedRaritesForCardFromHashMap(card.setNumber, card.priceBought,
					card.dateBought, card.folderName, card.condition, card.editionPrinting);

			for (OwnedCard existingCard : ownedRarities) {
				if (Util.doesCardExactlyMatchWithColor(card.folderName, card.cardName, card.setCode, card.setNumber,
						card.condition, card.editionPrinting, card.priceBought, card.dateBought, card.colorVariant,
						existingCard)) {
					// exact match found
					if (existingCard.quantity == card.quantity && existingCard.rarityUnsure == card.rarityUnsure
							&& existingCard.setRarity.equals(card.setRarity)
							&& existingCard.priceLow.equals(card.priceLow)
							&& existingCard.priceMid.equals(card.priceMid)
							&& existingCard.priceMarket.equals(card.priceMarket)) {
						// nothing to update
						card = null;
						break;
					} else {
						// something to update
						card.UUID = existingCard.UUID;
						break;
					}
				}
			}

			if (card != null) {
				count += card.quantity;
				SQLiteConnection.getObj().upsertOwnedCardBatch(card);
			}
		}
		
		
		
		System.out.println("Imported " + count + " cards");

	}

}
