package ygodb.windows.analyze;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVPrinter;

import ygodb.commonLibrary.bean.OwnedCard;
import ygodb.windows.connection.CsvConnection;
import ygodb.commonLibrary.connection.SQLiteConnection;
import ygodb.windows.utility.WindowsUtil;

public class AnalyzeCardsToSell {
	
	BigDecimal minPrice = new BigDecimal("2.00");


	public static void main(String[] args) throws SQLException, IOException {
		AnalyzeCardsToSell mainObj = new AnalyzeCardsToSell();

		SQLiteConnection db = WindowsUtil.getDBInstance();
		mainObj.run(db);
		db.closeInstance();
	}

	public void run(SQLiteConnection db) throws SQLException, IOException {

		 ArrayList<OwnedCard> cards = db.getAllOwnedCards();
		 
		 HashMap <String, ArrayList<String>> priceMap = new HashMap<>();
		 
		 HashMap <String, Integer> countMap = new HashMap<>();
		 
		 HashMap <String, ArrayList<OwnedCard>> cardMap = new HashMap<>();
		 
		 for(OwnedCard card: cards) {
			 
			 ArrayList<String> priceList = priceMap.get(card.cardName);
			 
			 Integer count = countMap.get(card.cardName);

			 ArrayList<OwnedCard> cardList = cardMap.computeIfAbsent(card.cardName, k -> new ArrayList<>());

			 cardList.add(card);
			 
			 if(priceMap.get(card.cardName) == null) {
				 priceList = new ArrayList<>();
				 priceMap.put(card.cardName, priceList);
			 }
			 
			 if(count == null) {
				 count = 0;
			 }
			 
			 count += card.quantity;
			 countMap.put(card.cardName, count);
			 
			 if(card.priceBought != null) {
				 priceList.add(card.priceBought);
			 }
		 }
		 

		printOutput(priceMap, countMap, cardMap);

	}

	public void printOutput(Map<String, ArrayList<String>> priceMap, Map<String, Integer> countMap, Map<String, ArrayList<OwnedCard>> cardMap)
			throws IOException {

		String filename = "C:\\Users\\Mike\\Documents\\GitHub\\YGO-DB\\YGO-DB\\csv\\Analyze-" + "Sell.csv";

		CSVPrinter p = CsvConnection.getSellFile(filename);

		for (String cardName : countMap.keySet()) {

			Integer count = countMap.get(cardName);

			if (count <= 3) {
				continue;
			}

			boolean foundHighPrice = false;

			for (String price : priceMap.get(cardName)) {
				BigDecimal priceBD = new BigDecimal(price);

				if (priceBD.compareTo(minPrice) >= 0) {
					foundHighPrice = true;
					break;
				}
			}

			if (foundHighPrice) {
				System.out.println(cardName + ":" + count);
				
				for(OwnedCard card: cardMap.get(cardName)) {
					
					//p.printRecord(Const.quantityCSV, Const.cardNameCSV, Const.cardTypeCSV, Const.rarityCSV, Const.setNameCSV, Const.setCodeCSV, Const.priceBoughtCSV);
					
					p.printRecord(card.quantity, card.cardName, card.setRarity, card.setName, card.setCode, card.priceBought);
				}
				
			}

		}
		
		p.flush();
		p.close();
	}

}
