package ygodb.commonLibrary.analyze;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.csv.CSVPrinter;

import ygodb.commonLibrary.bean.AnalyzeData;
import ygodb.commonLibrary.bean.CardSet;
import ygodb.commonLibrary.bean.GamePlayCard;
import ygodb.commonLibrary.bean.OwnedCard;
import ygodb.commonLibrary.bean.SetMetaData;
import ygodb.commonLibrary.connection.CsvConnection;
import ygodb.commonLibrary.connection.SQLiteConnection;
import ygodb.commonLibrary.constant.Const;

public class AnalyzeCardsInSet {



	public List<AnalyzeData> runFor(String setName, SQLiteConnection db) throws SQLException {
		HashMap<String, AnalyzeData> h = new HashMap<>();

		String[] sets = setName.split(";");

		for (String individualSet : sets) {
			addAnalyzeDataForSet(h, individualSet, db);
		}

		return new ArrayList<>(h.values());
	}

	public void run(SQLiteConnection db) throws SQLException, IOException {

		System.out.print("Set Name or Code: ");

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		String setName = reader.readLine();
		String finalFileName = setName;

		if (setName == null || setName.isBlank()) {
			setName = "HAC1;BLVO;SDFC;MAMA;SGX2;SDCB;MP22;TAMA;POTE;"
					+ "LDS3;LED9;DIFO;GFP2;SDAZ;SGX1;BACH;GRCR;BROL;"
					+ "MGED;BODE;LED8;SDCS;MP21;DAMA;KICO;EGO1;EGS1;"
					+ "LIOV;ANGU;GEIM;SBCB;SDCH;PHHY;DABL;AMDE;PHHY;MAZE";
			finalFileName = "Combined";
		}

		HashMap<String, AnalyzeData> h = new HashMap<>();

		String[] sets = setName.split(";");

		for (String individualSet : sets) {
			addAnalyzeDataForSet(h, individualSet, db);
		}

		ArrayList<AnalyzeData> array = new ArrayList<>(h.values());

		printOutput(array, finalFileName);

	}

	public void printOutput(List<AnalyzeData> array, String setName) throws IOException {
		Collections.sort(array);

		String filename = "C:\\Users\\Mike\\Documents\\GitHub\\YGO-DB\\YGO-DB\\csv\\Analyze-"
				+ setName.replaceAll("[\\s\\\\/:*?\"<>|]", "") + ".csv";

		CSVPrinter p = CsvConnection.getAnalyzeOutputFile(filename);

		if(p == null){
			return;
		}

		boolean printedSeparator = false;

		for (AnalyzeData s : array) {

			if (!printedSeparator && s.quantity >= 3) {
				printedSeparator = true;
				System.out.println();
				System.out.println("----");
				System.out.println();
			}

			System.out.println(s.quantity + ":" + s.cardName + " " + s.getStringOfRarities());

			String massbuy = "";

			if (s.quantity < 3) {
				if (s.cardType.equals(Const.CARD_TYPE_SKILL)) {
					if (s.quantity < 1) {
						massbuy = (1) + " " + s.cardName;
					} else {
						massbuy = "";
					}
				} else {

					massbuy = (3 - s.quantity) + " " + s.cardName;
				}
			}

			String massbuy1 = "";

			if (s.quantity < 1) {
				massbuy1 = (1 - s.quantity) + " " + s.cardName;
			}

			p.printRecord(s.quantity, s.cardName, s.cardType, s.getStringOfRarities(), s.getStringOfSetNames(),
					s.getStringOfSetNumbers(), massbuy, massbuy1);

		}
		p.flush();
		p.close();
	}

	public void addAnalyzeDataForSet(Map<String, AnalyzeData> h, String setName, SQLiteConnection db) throws SQLException {
		ArrayList<GamePlayCard> list = db.getDistinctCardNamesAndGamePlayCardUUIDsInSetByName(setName);
		boolean archetypeMode = false;

		if (list.isEmpty()) {
			ArrayList<SetMetaData> setNames = db.getSetMetaDataFromSetCode(setName.toUpperCase(Locale.ROOT));

			if (setNames == null || setNames.isEmpty() ) {

				list = db.getDistinctCardNamesAndIdsByArchetype(setName);
				archetypeMode = true;
				if (list.isEmpty()) {
					return;
				}
			}
			else {
				setName = setNames.get(0).setName;
				list = db.getDistinctCardNamesAndGamePlayCardUUIDsInSetByName(setName);
			}
		}

		ArrayList<SetMetaData> setMetaData = db.getSetMetaDataFromSetName(setName);

		for (GamePlayCard currentCardSet : list) {

			String currentCard = currentCardSet.cardName;
			String gamePlayCardUUID = currentCardSet.gamePlayCardUUID;
			int passcode = currentCardSet.passcode;

			ArrayList<OwnedCard> cardsList = db.getNumberOfOwnedCardsByGamePlayCardUUID(gamePlayCardUUID);

			ArrayList<CardSet> rarityList;
			if(!archetypeMode) {
				rarityList = db.getRaritiesOfCardInSetByGamePlayCardUUID(gamePlayCardUUID, setName);
			}
			else{
				rarityList = db.getRaritiesOfCardByGamePlayCardUUID(gamePlayCardUUID);
			}

			if (cardsList.isEmpty()) {

				AnalyzeData currentData = new AnalyzeData();

				if (currentCard == null) {
					currentData.cardName = "No cards found for id:" + gamePlayCardUUID;
					currentData.quantity = -1;
				} else {
					currentData.cardName = currentCard;
					currentData.quantity = 0;
				}

				if(!archetypeMode){
					for (CardSet rarity : rarityList) {
						currentData.setRarities.add(rarity.setRarity);

						if(rarity.setName.equalsIgnoreCase(setName)){
							currentData.mainSetCardSets.add(rarity);
						}

					}
					currentData.cardPriceAverage = currentData.getLowestPriceFromMainSet();
				}
				else{
					BigDecimal origSetPrice = new BigDecimal(Integer.MAX_VALUE);
					currentData.cardPriceAverage = origSetPrice;
					for (CardSet rarity : rarityList) {
						currentData.setName.add(rarity.setName);
						currentData.setRarities.add(rarity.setRarity);

						if(rarity.setPrice == null){
							rarity.setPrice = "0";
						}

						BigDecimal setPrice = new BigDecimal(rarity.setPrice);
						BigDecimal zero = new BigDecimal(0);

						if (!(zero.equals(setPrice)) && currentData.cardPriceAverage.compareTo(setPrice) > 0){
							currentData.cardPriceAverage = setPrice;
						}
					}
					if(origSetPrice.equals(currentData.cardPriceAverage)){
						currentData.cardPriceAverage = new BigDecimal(0);
					}
				}

				currentData.gamePlayCardUUID = gamePlayCardUUID;
				currentData.passcode = passcode;

				if(!archetypeMode) {
					currentData.setNumber.add(rarityList.get(0).setNumber);
					currentData.cardType = rarityList.get(0).cardType;
					currentData.setName.add(setName);
					currentData.mainSetName = setName;
					currentData.mainSetCode = setMetaData.get(0).setCode;
				}
				addToHashMap(h, currentData);
			}

			for (OwnedCard current : cardsList) {
				AnalyzeData currentData = new AnalyzeData();

				currentData.cardName = current.cardName;
				currentData.quantity = current.quantity;

				if(!archetypeMode) {
					for (CardSet rarity : rarityList) {
						currentData.setRarities.add(rarity.setRarity);

						if (rarity.setName.equalsIgnoreCase(setName)) {
							currentData.mainSetCardSets.add(rarity);
						}
					}
				}
				else{
					for (CardSet rarity : rarityList) {
						currentData.setName.add(rarity.setName);
						currentData.setRarities.add(rarity.setRarity);
					}
				}

				currentData.gamePlayCardUUID = gamePlayCardUUID;
				currentData.passcode = passcode;
				if(!archetypeMode) {
					currentData.setNumber.add(rarityList.get(0).setNumber);
					currentData.cardType = rarityList.get(0).cardType;
					currentData.mainSetName = setName;
					currentData.mainSetCode = setMetaData.get(0).setCode;
				}
				Collections.addAll(currentData.setName, current.setName.split(","));
				currentData.cardPriceAverage = new BigDecimal(current.priceBought);
				addToHashMap(h, currentData);
			}
		}
	}

	private void addToHashMap(Map<String, AnalyzeData> h, AnalyzeData s) {

		AnalyzeData existing = h.get(s.cardName);

		if (existing == null) {
			h.put(s.cardName, s);
		} else {
			existing.setName.addAll(s.setName);
			existing.setNumber.addAll(s.setNumber);
			existing.setRarities.addAll(s.setRarities);
		}

	}
}
