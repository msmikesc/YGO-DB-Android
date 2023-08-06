package ygodb.windows.analyze;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVPrinter;

import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.commonlibrary.connection.CsvConnection;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.windows.utility.WindowsUtil;

public class AnalyzeCardsToSell {

	private final BigDecimal minPrice = new BigDecimal("2.00");


	public static void main(String[] args) throws SQLException, IOException {
		AnalyzeCardsToSell mainObj = new AnalyzeCardsToSell();

		SQLiteConnection db = WindowsUtil.getDBInstance();
		mainObj.run(db);
		db.closeInstance();
	}

	public void run(SQLiteConnection db) throws SQLException, IOException {

		//TODO update this to use api prices

		List<OwnedCard> cards = db.getAllOwnedCards();

		HashMap<String, ArrayList<String>> priceMap = new HashMap<>();

		HashMap<String, Integer> countMap = new HashMap<>();

		HashMap<String, ArrayList<OwnedCard>> cardMap = new HashMap<>();

		for (OwnedCard card : cards) {

			ArrayList<String> priceList = priceMap.get(card.getCardName());

			Integer count = countMap.get(card.getCardName());

			ArrayList<OwnedCard> cardList = cardMap.computeIfAbsent(card.getCardName(), k -> new ArrayList<>());

			cardList.add(card);

			if (priceMap.get(card.getCardName()) == null) {
				priceList = new ArrayList<>();
				priceMap.put(card.getCardName(), priceList);
			}

			if (count == null) {
				count = 0;
			}

			count += card.getQuantity();
			countMap.put(card.getCardName(), count);

			if (card.getPriceBought() != null) {
				priceList.add(card.getPriceBought());
			}
		}


		printOutput(priceMap, countMap, cardMap);

	}

	public void printOutput(Map<String, ArrayList<String>> priceMap, Map<String, Integer> countMap,
			Map<String, ArrayList<OwnedCard>> cardMap) throws IOException {

		String filename = "Analyze-Sell.csv";
		String resourcePath = Const.CSV_ANALYZE_FOLDER + filename;

		CsvConnection csvConnection = new CsvConnection();

		CSVPrinter p = csvConnection.getSellFile(resourcePath);
		for (Map.Entry<String, Integer> entry : countMap.entrySet()) {

			String cardName = entry.getKey();
			Integer count = entry.getValue();

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
				YGOLogger.info(cardName + ":" + count);

				for (OwnedCard card : cardMap.get(cardName)) {

					p.printRecord(card.getQuantity(), card.getCardName(), card.getSetRarity(), card.getSetName(), card.getSetPrefix(),
								  card.getPriceBought());
				}

			}

		}

		p.flush();
		p.close();
	}

}
