package analyze;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.csv.CSVPrinter;

import bean.OwnedCard;
import connection.CsvConnection;
import connection.SQLiteConnection;

public class AnalyzeCardsToSell {
	
	BigDecimal minPrice = new BigDecimal("2.00");

	/*
	public static void main(String[] args) throws SQLException, IOException {
		AnalyzeCardsToSell mainObj = new AnalyzeCardsToSell();
		mainObj.run();
		
	}*/

	public void run(SQLiteConnection db) throws SQLException, IOException {

		 ArrayList<OwnedCard> cards = db.getAllOwnedCards();
		 
		 HashMap <String, ArrayList<String>> priceMap = new HashMap<String, ArrayList<String>>();
		 
		 HashMap <String, Integer> countMap = new HashMap<String, Integer>();
		 
		 HashMap <String, ArrayList<OwnedCard>> cardMap = new HashMap<String, ArrayList<OwnedCard>>();
		 
		 for(OwnedCard card: cards) {
			 
			 ArrayList<String> priceList = priceMap.get(card.cardName);
			 
			 Integer count = countMap.get(card.cardName);
			 
			 ArrayList<OwnedCard> cardList = cardMap.get(card.cardName);
			 
			 if(cardList == null) {
				 cardList = new ArrayList<OwnedCard>();
				 cardMap.put(card.cardName, cardList);
			 }
			 
			 cardList.add(card);
			 
			 if(priceMap.get(card.cardName) == null) {
				 priceList = new ArrayList<String>();
				 priceMap.put(card.cardName, priceList);
			 }
			 
			 if(count == null) {
				 count = Integer.valueOf(0);
			 }
			 
			 count += card.quantity;
			 countMap.put(card.cardName, count);
			 
			 if(card.priceBought != null) {
				 priceList.add(card.priceBought);
			 }
			 if(card.priceLow != null) {
				 priceList.add(card.priceLow);
			 }
			 if(card.priceMid != null) {
				 priceList.add(card.priceMid);
			 }
			 if(card.priceMarket != null) {
				 priceList.add(card.priceMarket);
			 }
		 }
		 

		printOutput(priceMap, countMap, cardMap);

	}

	public void printOutput(HashMap<String, ArrayList<String>> priceMap, HashMap<String, Integer> countMap, HashMap<String, ArrayList<OwnedCard>> cardMap)
			throws IOException {

		String filename = "C:\\Users\\Mike\\Documents\\GitHub\\YGO-DB\\YGO-DB\\csv\\Analyze-" + "Sell.csv";

		CSVPrinter p = CsvConnection.getSellFile(filename);

		countMap.keySet();

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
					
					//p.printRecord("Quantity", "Card Name", "Card Type", "Rarity", "Set Name", "Set Code", "Price Bought", "LOW", "MID", "MARKET");
					
					p.printRecord(card.quantity, card.cardName, card.setRarity, card.setName, card.setCode, card.priceBought, card.priceLow, card.priceMid, card.priceMarket);
				}
				
			}

		}
		
		p.flush();
		p.close();
	}

}
