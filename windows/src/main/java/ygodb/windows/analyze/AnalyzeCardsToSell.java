package ygodb.windows.analyze;

import org.apache.commons.csv.CSVPrinter;
import ygodb.commonlibrary.bean.CardSet;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.connection.CsvConnection;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.constant.Const;
import ygodb.windows.utility.WindowsUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyzeCardsToSell {

	private final BigDecimal minPrice = new BigDecimal("4.00");

	private final BigDecimal minPricePercentage = new BigDecimal("2.00");


	public static void main(String[] args) throws SQLException, IOException {
		AnalyzeCardsToSell mainObj = new AnalyzeCardsToSell();

		SQLiteConnection db = WindowsUtil.getDBInstance();
		mainObj.run(db);
		db.closeInstance();
	}

	public void run(SQLiteConnection db) throws SQLException, IOException {

		List<OwnedCard> cards = db.getAllOwnedCards();

		HashMap<String, Integer> countMap = new HashMap<>();

		HashMap<String, ArrayList<OwnedCard>> cardMap = new HashMap<>();

		for (OwnedCard card : cards) {

			Integer count = countMap.get(card.getCardName());

			ArrayList<OwnedCard> cardList = cardMap.computeIfAbsent(card.getCardName(), k -> new ArrayList<>());

			cardList.add(card);

			if (count == null) {
				count = 0;
			}

			count += card.getQuantity();
			countMap.put(card.getCardName(), count);

		}


		printOutput(countMap, cardMap, db);

	}

	public void printOutput(Map<String, Integer> countMap, Map<String, ArrayList<OwnedCard>> cardMap, SQLiteConnection db)
			throws IOException, SQLException {

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

			for (OwnedCard card : cardMap.get(cardName)) {

				CardSet set = db.getRarityOfExactCardInSet(card.getGamePlayCardUUID(), card.getSetNumber(), card.getSetRarity(),
														   card.getColorVariant(), card.getSetName());

				String priceFromAPI = set.getBestExistingPrice(card.getEditionPrinting());

				BigDecimal priceApi = new BigDecimal(priceFromAPI);
				card.setAnalyzeResultsCardSets(List.of(set));

				BigDecimal priceBought = new BigDecimal(card.getPriceBought());

				if (priceBought.compareTo(new BigDecimal(0)) == 0) {
					priceBought = new BigDecimal("0.01");
				}


				if (priceApi.compareTo(minPrice) >= 0 || (priceApi.compareTo(minPricePercentage) >= 0 &&
						priceApi.divide(priceBought, 2, RoundingMode.HALF_UP).compareTo(BigDecimal.valueOf(1.5)) > 0)) {
					p.printRecord(card.getQuantity(), card.getCardName(), card.getSetRarity(), card.getSetName(), card.getSetPrefix(),
								  card.getPriceBought(),
								  card.getAnalyzeResultsCardSets().get(0).getBestExistingPrice(card.getEditionPrinting()));
				}
			}

		}

		p.flush();
		p.close();
	}

}
