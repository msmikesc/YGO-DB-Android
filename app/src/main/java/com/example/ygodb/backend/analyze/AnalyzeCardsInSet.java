package com.example.ygodb.backend.analyze;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import org.apache.commons.csv.CSVPrinter;

import com.example.ygodb.backend.bean.AnalyzeData;
import com.example.ygodb.backend.bean.CardSet;
import com.example.ygodb.backend.bean.OwnedCard;
import com.example.ygodb.backend.bean.SetMetaData;
import com.example.ygodb.backend.connection.CsvConnection;
import com.example.ygodb.backend.connection.SQLiteConnection;

public class AnalyzeCardsInSet {

	public static void main(String[] args) throws SQLException, IOException {
		AnalyzeCardsInSet mainObj = new AnalyzeCardsInSet();
		mainObj.run();
		
	}

	public ArrayList<AnalyzeData> runFor(String setName) throws SQLException {
		HashMap<String, AnalyzeData> h = new HashMap<String, AnalyzeData>();

		String[] sets = setName.split(";");

		for (String individualSet : sets) {
			addAnalyzeDataForSet(h, individualSet);
		}

		ArrayList<AnalyzeData> array = new ArrayList<AnalyzeData>(h.values());

		return array;
	}

	public void run() throws SQLException, IOException {

		System.out.print("Set Name or Code: ");

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		String setName = reader.readLine();
		String finalFileName = setName;

		if (setName.isBlank()) {
			setName = "HAC1;BLVO;SDFC;MAMA;SGX2;SDCB;MP22;TAMA;POTE;"
					+ "LDS3;LED9;DIFO;GFP2;SDAZ;SGX1;BACH;GRCR;BROL;"
					+ "MGED;BODE;LED8;SDCS;MP21;DAMA;KICO;EGO1;EGS1;"
					+ "LIOV;ANGU;GEIM;SBCB;SDCH";
			finalFileName = "Combined";
		}

		HashMap<String, AnalyzeData> h = new HashMap<String, AnalyzeData>();

		String[] sets = setName.split(";");

		for (String individualSet : sets) {
			addAnalyzeDataForSet(h, individualSet);
		}

		ArrayList<AnalyzeData> array = new ArrayList<AnalyzeData>(h.values());

		printOutput(array, finalFileName);

	}

	public void printOutput(ArrayList<AnalyzeData> array, String setName) throws IOException {
		Collections.sort(array);

		String filename = "C:\\Users\\Mike\\Documents\\GitHub\\YGO-DB\\YGO-DB\\csv\\Analyze-"
				+ setName.replaceAll("[\\s\\\\/:*?\"<>|]", "") + ".csv";

		CSVPrinter p = CsvConnection.getAnalyzeOutputFile(filename);

		boolean printedSeparator = false;

		for (AnalyzeData s : array) {

			if (!printedSeparator && s.quantity >= 3) {
				printedSeparator = true;
				System.out.println("");
				System.out.println("----");
				System.out.println("");
			}

			System.out.println(s.quantity + ":" + s.cardName + " " + s.getStringOfRarities());

			String massbuy = "";

			if (s.quantity < 3) {
				if (s.cardType.equals("Skill Card")) {
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

	public void addAnalyzeDataForSet(HashMap<String, AnalyzeData> h, String setName) throws SQLException {
		ArrayList<Integer> list = SQLiteConnection.getObj().getDistinctCardIDsInSetByName(setName);

		if (list.size() == 0) {
			ArrayList<SetMetaData> setNames = SQLiteConnection.getObj().getSetMetaDataFromSetCode(setName.toUpperCase());

			if (setNames == null || setNames.isEmpty() ) {
				System.out.println("Unable to identify card set:" + setName);
				return;
			}

			setName = setNames.get(0).set_name;
			list = SQLiteConnection.getObj().getDistinctCardIDsInSetByName(setName);
		}

		ArrayList<SetMetaData> setMetaData = SQLiteConnection.getObj().getSetMetaDataFromSetName(setName);

		for (int i : list) {
			ArrayList<OwnedCard> cardsList = SQLiteConnection.getObj().getNumberOfOwnedCardsById(i);

			ArrayList<CardSet> rarityList = SQLiteConnection.getObj().getRaritiesOfCardInSetByID(i, setName);

			if (cardsList.size() == 0) {

				String title = SQLiteConnection.getObj().getCardTitleFromID(i);

				AnalyzeData currentData = new AnalyzeData();

				if (title == null) {
					currentData.cardName = "No cards found for id:" + i;
					currentData.quantity = -1;
				} else {
					currentData.cardName = title;
					currentData.quantity = 0;
				}

				for (CardSet rarity : rarityList) {
					currentData.setRarities.add(rarity.setRarity);

					if(rarity.setName.equalsIgnoreCase(setName)){
						currentData.mainSetRarities.add(rarity.setRarity);
						currentData.mainSetNumber.add(rarity.setNumber);
					}

				}
				currentData.id = i;
				currentData.setNumber.add(rarityList.get(0).setNumber);
				currentData.cardType = rarityList.get(0).cardType;
				currentData.setName.add(setName);
				currentData.mainSetName = setName;
				currentData.mainSetCode = setMetaData.get(0).set_code;
				addToHashMap(h, currentData);
			}

			for (OwnedCard current : cardsList) {
				AnalyzeData currentData = new AnalyzeData();

				currentData.cardName = current.cardName;
				currentData.quantity = current.quantity;

				for (CardSet rarity : rarityList) {
					currentData.setRarities.add(rarity.setRarity);

					if(rarity.setName.equalsIgnoreCase(setName)){
						currentData.mainSetRarities.add(rarity.setRarity);
						currentData.mainSetNumber.add(rarity.setNumber);
					}
				}

				currentData.id = i;
				currentData.setNumber.add(rarityList.get(0).setNumber);
				currentData.cardType = rarityList.get(0).cardType;
				currentData.mainSetName = setName;
				currentData.mainSetCode = setMetaData.get(0).set_code;

				for(String s: current.setName.split(",")){
					currentData.setName.add(s);
				}
				currentData.cardPriceAverage = new BigDecimal(current.priceBought);
				addToHashMap(h, currentData);
			}
		}
	}

	private void addToHashMap(HashMap<String, AnalyzeData> h, AnalyzeData s) {

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
