package com.example.ygodb.backend.analyze;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

public class AnalyzeCardsInArchetype {

	public static void main(String[] args) throws SQLException, IOException {
		AnalyzeCardsInArchetype mainObj = new AnalyzeCardsInArchetype();
		mainObj.run();
		
	}

	public void run() throws SQLException, IOException {

		System.out.print("Archetype Name: ");

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		String setName = reader.readLine();
		String finalFileName = setName;

		if (setName.isBlank()) {
			setName = "Elemental HERO;Destiny HERO;Evil HERO;Vision HERO;Masked HERO;Neo-Spacian;Neo Space;Chrysalis";
			finalFileName = "HERO";
		}

		HashMap<String, AnalyzeData> h = new HashMap<String, AnalyzeData>();

		String[] sets = setName.split(";");

		for (String individualSet : sets) {
			addAnalyzeDataForArchetype(h, individualSet);
		}

		ArrayList<AnalyzeData> array = new ArrayList<AnalyzeData>(h.values());

		printOutput(array, finalFileName);

	}

	public void printOutput(ArrayList<AnalyzeData> array, String setName) throws IOException {
		Collections.sort(array);

		String filename = "C:\\Users\\Mike\\Documents\\GitHub\\YGO-DB\\YGO-DB\\csv\\Archetype-"
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
				if (s.cardType != null && s.cardType.equals("Skill Card")) {
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

	public void addAnalyzeDataForArchetype(HashMap<String, AnalyzeData> h, String setName) throws SQLException {
		ArrayList<Integer> list = SQLiteConnection.getObj().getDistinctCardIDsByArchetype(setName);

		for (int i : list) {
			ArrayList<OwnedCard> cardsList = SQLiteConnection.getObj().getNumberOfOwnedCardsById(i);

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
				
				addToHashMap(h, currentData);
			}

			for (OwnedCard current : cardsList) {
				AnalyzeData currentData = new AnalyzeData();

				currentData.cardName = current.cardName;
				currentData.quantity = current.quantity;
				
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
