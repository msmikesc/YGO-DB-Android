package ygodb.commonLibrary.utility;


import com.fasterxml.jackson.databind.JsonNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.*;

import javafx.util.Pair;
import ygodb.commonLibrary.bean.CardSet;
import ygodb.commonLibrary.bean.OwnedCard;
import ygodb.commonLibrary.bean.SetMetaData;
import ygodb.commonLibrary.bean.Rarity;
import ygodb.commonLibrary.connection.DatabaseHashMap;
import ygodb.commonLibrary.connection.SQLiteConnection;

public class Util {
	
	public static String defaultColorVariant = "-1";
	
	private static KeyUpdateMap setNameMap = null;
	private static HashMap<String, String> rarityMap = null;
	private static HashMap<String, String> setNumberMap = null;
	private static HashMap<String, String> cardNameMap = null;
	private static HashMap<Integer, Integer> passcodeMap = null;

	private static QuadKeyUpdateMap quadKeyUpdateMap = null;

	public static QuadKeyUpdateMap getQuadKeyUpdateMapInstance() {
		if (quadKeyUpdateMap == null) {

			try {
				String filename = "quadUpdateMapping.csv";

				InputStream inputStream = Util.class.getResourceAsStream("/" + filename);

				quadKeyUpdateMap = new QuadKeyUpdateMap(inputStream, "|");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

		}

		return quadKeyUpdateMap;
	}

	public static List<String> checkForTranslatedQuadKey(String cardName, String setNumber, String rarity, String setName) {
		QuadKeyUpdateMap instance = getQuadKeyUpdateMapInstance();

		return instance.getValues(cardName, setNumber, rarity, setName);
	}

	public static KeyUpdateMap getSetNameMapInstance() {
		if (setNameMap == null) {

			try {
				String filename = "setNameUpdateMapping.csv";

				InputStream inputStream = Util.class.getResourceAsStream("/" + filename);

				setNameMap = new KeyUpdateMap(inputStream);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

		}

		return setNameMap;
	}

	public static HashMap<String, String> getRarityMapInstance() {
		if (rarityMap == null) {
			rarityMap = new HashMap<>();

			rarityMap.put("Collectors Rare", "Collector's Rare");
			rarityMap.put("URPR", "Ultra Rare (Pharaoh's Rare)");
			rarityMap.put("Super Short Print", "Short Print");
			rarityMap.put("SSP", "Short Print");
			rarityMap.put("Duel Terminal Technology Common", "Duel Terminal Normal Parallel Rare");
			rarityMap.put("Secret Pharaohâ€™s Rare", "Secret Rare (Pharaoh's Rare)");
			rarityMap.put("Ultra Pharaohâ€™s Rare", "Ultra Rare (Pharaoh's Rare)");
			rarityMap.put("Duel Terminal Technology Ultra Rare", "Duel Terminal Ultra Parallel Rare");
			rarityMap.put("Ultra Pharaoh’s Rare", "Ultra Rare (Pharaoh's Rare)");
			rarityMap.put("Secret Pharaoh’s Rare", "Secret Rare (Pharaoh's Rare)");

			//rarityMap.put("", "");

		}

		return rarityMap;
	}

	public static HashMap<String, String> getSetNumberMapInstance() {
		if (setNumberMap == null) {
			setNumberMap = new HashMap<>();

			setNumberMap.put("GTP2-EN176", "GFP2-EN176");
			setNumberMap.put("SSD-E001", "SDD-E001");
			setNumberMap.put("SSD-E002", "SDD-E002");
			setNumberMap.put("SSD-E003", "SDD-E003");
			setNumberMap.put("OTPT-EN001", "OPTP-EN001");

			//setNumberMap.put("", "");

		}

		return setNumberMap;
	}

	public static HashMap<Integer, Integer> getPasscodeMapInstance() {
		if (passcodeMap == null) {
			passcodeMap = new HashMap<>();

			passcodeMap.put(74677427, 74677422);
			passcodeMap.put(89943724, 89943723);


			//passcodeMap.put("", "");

		}

		return passcodeMap;
	}

	public static HashMap<String, String> getCardNameMapInstance() {
		if (cardNameMap == null) {
			cardNameMap = new HashMap<>();

			cardNameMap.put("after genocide","After the Struggle");
			cardNameMap.put("amazon archer" ,"Amazoness Archer");
			cardNameMap.put("armityle the chaos phantom","Armityle the Chaos Phantasm");
			cardNameMap.put("big core" ,"B.E.S. Big Core");
			cardNameMap.put("cliff the trap remover","Dark Scorpion - Cliff the Trap Remover");
			cardNameMap.put("dark assassin","Dark Assailant");
			cardNameMap.put("dark trap hole","Darkfall");
			cardNameMap.put("forbidden graveyard","Silent Graveyard");
			cardNameMap.put("frog the jam","Slime Toad");
			cardNameMap.put("harpie's brother","Sky Scout");
			cardNameMap.put("hidden book of spell","Hidden Spellbook");
			cardNameMap.put("judgment of the pharaoh","Judgment of Pharaoh");
			cardNameMap.put("kinetic soldier","Cipher Soldier");
			cardNameMap.put("marie the fallen one","Darklord Marie");
			cardNameMap.put("metaphysical regeneration","Supernatural Regeneration");
			cardNameMap.put("null and void","Muko");
			cardNameMap.put("nurse reficule the fallen one","Darklord Nurse Reficule");
			cardNameMap.put("oscillo hero #2","Wattkid");
			cardNameMap.put("pigeonholing books of spell","Spellbook Organization");
			cardNameMap.put("red-eyes b. chick","Black Dragon's Chick");
			cardNameMap.put("red-eyes b. dragon","Red-Eyes Black Dragon");
			cardNameMap.put("red-moon baby","Vampire Baby");
			cardNameMap.put("trial of hell","Trial of Nightmare");
			cardNameMap.put("d. d. assailant","D.D. Assailant");
			cardNameMap.put("d. d. borderline","D.D. Borderline");
			cardNameMap.put("d. d. designator","D.D. Designator");
			cardNameMap.put("d. d. scout plane","D.D. Scout Plane");
			cardNameMap.put("d. d. trainer","D.D. Trainer");
			cardNameMap.put("d. d. warrior lady","D.D. Warrior Lady");
			cardNameMap.put("gradius's option","Gradius' Option");
			cardNameMap.put("hundred-eyes dragon","Hundred Eyes Dragon");
			cardNameMap.put("necrolancer the timelord","Necrolancer the Time-lord");
			cardNameMap.put("sephylon,the Ultimate Time Lord","Sephylon, the Ultimate Timelord");
			cardNameMap.put("winged dragon	Guardian of the Fortress #1","Winged Dragon, Guardian of the Fortress #1");
			cardNameMap.put("blackwing  armed wing","Blackwing Armed Wing");
			cardNameMap.put("b. skull dragon","Black Skull Dragon");

			//cardNameMap.put("", "");

		}

		return cardNameMap;
	}
	
	public static String flipStructureEnding(String input, String match) {
		
		input = input.trim();
		
		if(input.endsWith(match)) {
			input = match + ": " + input.replace(match, "").trim();
		}
		return input;
		
	}


	public static String checkForTranslatedSetName(String setName) {

		if(setName.contains("The Lost Art Promotion")) {
			setName = "The Lost Art Promotion";
		}

		if(setName.contains("(Worldwide English)")) {
			setName = setName.replace("(Worldwide English)", "");
			setName = setName.trim();
		}

		if(setName.contains("Sneak Peek Participation Card")) {
			setName = setName.replace("Sneak Peek Participation Card", "");
			setName = setName.trim();
		}

		if(setName.contains(": Special Edition")) {
			setName = setName.replace(": Special Edition", "");
			setName = setName.trim();
		}

		if(setName.contains("Special Edition")) {
			setName = setName.replace("Special Edition", "");
			setName = setName.trim();
		}

		if(setName.contains(": Super Edition")) {
			setName = setName.replace(": Super Edition", "");
			setName = setName.trim();
		}

		if(setName.contains("Super Edition")) {
			setName = setName.replace("Super Edition", "");
			setName = setName.trim();
		}

		if(!setName.equals("Structure Deck: Deluxe Edition") && setName.contains(": Deluxe Edition")) {
			setName = setName.replace(": Deluxe Edition", "");
			setName = setName.trim();
		}

		if(!setName.equals("Structure Deck: Deluxe Edition") && setName.contains("Deluxe Edition")) {
			setName = setName.replace("Deluxe Edition", "");
			setName = setName.trim();
		}

		if(setName.contains("Premiere! promotional card")) {
			setName = setName.replace("Premiere! promotional card", "");
			setName = setName.trim();
		}

		if(setName.contains("Launch Event participation card")) {
			setName = setName.replace("Launch Event participation card", "");
			setName = setName.trim();
		}

		if(setName.endsWith(" SE")) {
			setName = setName.substring(0, setName.length()-3);
			setName = setName.trim();
		}

		setName = flipStructureEnding(setName, "Starter Deck");
		setName = flipStructureEnding(setName, "Structure Deck");

		return getSetNameMapInstance().getValue(setName);
	}

	public static String checkForTranslatedRarity(String rarity) {
		HashMap<String, String> instance = getRarityMapInstance();

		String newRarity = instance.get(rarity);

		if(newRarity == null) {
			return rarity;
		}

		return newRarity;
	}

	public static String checkForTranslatedSetNumber(String setNumber) {
		HashMap<String, String> instance = getSetNumberMapInstance();

		String newSetNumber = instance.get(setNumber);

		if(newSetNumber == null) {
			return setNumber;
		}

		return newSetNumber;
	}



	public static String checkForTranslatedCardName(String cardName) {
		HashMap<String, String> instance = getCardNameMapInstance();

		String newName = instance.get(cardName.toLowerCase(Locale.ROOT));

		if(newName == null) {
			return cardName;
		}

		return newName;
	}

	public static int checkForTranslatedPasscode(int passcode) {
		HashMap<Integer, Integer> instance = getPasscodeMapInstance();

		Integer newPasscode = instance.get(passcode);

		if(newPasscode == null) {
			return passcode;
		}

		return newPasscode;
	}
	
	public static OwnedCard formOwnedCard(String folder, String name, String quantity, String setCode, String condition,
			String printing, String priceBought, String dateBought, CardSet setIdentified, int passcode) {
		OwnedCard card = new OwnedCard();
		
		card.folderName = folder;
		card.cardName = name;
		card.quantity = Integer.valueOf(quantity);
		card.setCode = setCode;
		card.condition = condition;
		card.editionPrinting = printing;
		card.priceBought = normalizePrice(priceBought);
		card.dateBought = dateBought;
		card.setRarity = setIdentified.setRarity;
		card.gamePlayCardUUID = setIdentified.gamePlayCardUUID;
		card.colorVariant = setIdentified.colorVariant;
		card.setName = setIdentified.setName;
		card.setNumber = setIdentified.setNumber;
		card.rarityUnsure = setIdentified.rarityUnsure;
		card.passcode = passcode;

		card.UUID = UUID.randomUUID().toString();
		
		return card;
	}
	
	public static boolean doesCardExactlyMatch(String folder, String name, String setCode, String setNumber,
			String condition, String printing, String priceBought, String dateBought, OwnedCard existingCard) {
        return setNumber.equals(existingCard.setNumber) && priceBought.equals(existingCard.priceBought)
                && dateBought.equals(existingCard.dateBought) && folder.equals(existingCard.folderName)
                && condition.equals(existingCard.condition) && printing.equals(existingCard.editionPrinting)
                && name.equals(existingCard.cardName) && setCode.equals(existingCard.setCode);
    }
	
	public static boolean doesCardExactlyMatchWithColor(String folder, String name, String setCode, String setNumber,
			String condition, String printing, String priceBought, String dateBought, String colorVariant,
			OwnedCard existingCard) {
        return setNumber.equals(existingCard.setNumber) && priceBought.equals(existingCard.priceBought)
                && dateBought.equals(existingCard.dateBought) && folder.equals(existingCard.folderName)
                && condition.equals(existingCard.condition) && printing.equals(existingCard.editionPrinting)
                && name.equals(existingCard.cardName) && setCode.equals(existingCard.setCode)
                && colorVariant.equals(existingCard.colorVariant);
    }

	public static void checkSetCounts(SQLiteConnection db) throws SQLException {
		ArrayList<SetMetaData> list = db.getAllSetMetaDataFromSetData();

		for (SetMetaData setData : list) {
			int countCardsinList = db.getCountDistinctCardsInSet(setData.set_name);

			if (countCardsinList != setData.num_of_cards) {
				System.out.println("Issue for " + setData.set_name + " metadata:" + setData.num_of_cards + " count:"
						+ countCardsinList);
			}
		}

		HashMap<String, SetMetaData> SetMetaDataMap = new HashMap<String, SetMetaData>();

		for (SetMetaData s : list) {
			SetMetaDataMap.put(s.set_name, s);
		}

		ArrayList<String> setNames = db.getDistinctSetNames();

		for (String setName : setNames) {
			
			if (setName.contains("Tip Card") || setName.contains("(POR)")) {
				continue;
			}
			
			SetMetaData meta = SetMetaDataMap.get(setName);

			if (meta == null) {
				System.out.println("Issue for " + setName + " no metadata");
				continue;
			}

			int cardsInSet = db.getCountDistinctCardsInSet(setName);

			if (cardsInSet != meta.num_of_cards) {
				System.out.println("Issue for " + setName + " metadata:" + meta.num_of_cards + " count:" + cardsInSet);
			}

		}

	}

	public static String normalizePrice(String input) {

		if(input == null || input.trim().equals("")) {
			return null;
		}

		BigDecimal price;

		try {
			price = new BigDecimal(input.replace(",", ""));
		}
		catch(Exception e) {
			System.out.println("Invalid price input:" + input);
			price = new BigDecimal("0");
		}

		price = price.setScale(2, RoundingMode.HALF_UP);

		return price.toString();
	}

	public static CardSet findRarity(String priceBought, String dateBought, String folderName, String condition,
			String editionPrinting, String setNumber, String setName, String cardName, SQLiteConnection db) throws SQLException {

		ArrayList<CardSet> setRarities = DatabaseHashMap.getRaritiesOfCardInSetFromHashMap(setNumber, db);

		if (setRarities.size() == 0) {
			// try removing color code

			String newSetNumber = setNumber.substring(0, setNumber.length() - 1);
			String colorcode = setNumber.substring(setNumber.length() - 1);

			setRarities = DatabaseHashMap.getRaritiesOfCardInSetFromHashMap(newSetNumber, db);

			for (CardSet c : setRarities) {
				c.colorVariant = colorcode;
			}
		}

		if (setRarities.size() == 1) {
			CardSet match = setRarities.get(0);

			match.rarityUnsure = 0;

			return match;
		}

		// if we haven't found any at all give up
		if (setRarities.size() == 0) {
			System.out.println("Unable to find anything for " + setNumber);
			CardSet setIdentified = new CardSet();

			setIdentified.setName = setName;
			setIdentified.setNumber = setNumber;
			setIdentified.setRarity = "Unknown";
			setIdentified.colorVariant = "Unknown";
			setIdentified.rarityUnsure = 1;

			// check for name
			setIdentified.gamePlayCardUUID = db.getGamePlayCardUUIDFromTitle(cardName);

			return setIdentified;
		}

		// assume NOT starlight, ultimate, or collectors
		if (setRarities.size() == 2) {
			for (int i = 0; i < 2; i++) {
				String name = setRarities.get(i).setRarity;
				if (name.equals((Rarity.StarlightRare.toString())) || name.equals((Rarity.UltimateRare.toString()))
						|| name.equals((Rarity.CollectorsRare.toString()))) {
					if (i == 0) {
						CardSet match = setRarities.get(1);

						match.rarityUnsure = 0;

						System.out
								.println("Took a guess that " + setNumber + ":" + cardName + " is:" + match.setRarity);

						return match;
					}
					if (i == 1) {
						CardSet match = setRarities.get(0);

						match.rarityUnsure = 0;

						System.out
								.println("Took a guess that " + setNumber + ":" + cardName + " is:" + match.setRarity);

						return match;
					}
				}
			}
		}

		// try closest price
		BigDecimal priceBoughtDec = new BigDecimal(priceBought);
		BigDecimal distance = new BigDecimal(setRarities.get(0).setPrice).subtract(priceBoughtDec).abs();
		int idx = 0;
		for (int c = 1; c < setRarities.size(); c++) {
			BigDecimal cdistance = new BigDecimal(setRarities.get(c).setPrice).subtract(priceBoughtDec).abs();
			if (cdistance.compareTo(distance) <= 0) {
				idx = c;
				distance = cdistance;
			}
		}

		CardSet rValue = setRarities.get(idx);
		rValue.rarityUnsure = 1;

		System.out.println("Took a guess that " + setNumber + ":" + cardName + " is:" + rValue.setRarity);

		return rValue;

	}

	public static void checkForIssuesWithCardNamesInSet(String setName, SQLiteConnection db) throws SQLException {
		ArrayList<String> list = db.getDistinctGamePlayCardUUIDsInSetByName(setName);
		for (String i : list) {
			String title = db.getCardTitleFromGamePlayCardUUID(i);

			if(title == null) {
				System.out.println("Not exactly 1 gameplaycard found for ID " + i);
			}

		}
	}

	public static void checkForIssuesWithSet(String setName, SQLiteConnection db) throws SQLException {

		ArrayList<String> cardsInSetList = db.getSortedCardsInSetByName(setName);

		String lastPrefix = null;
		String lastLang = null;
		int lastNum = -1;
		String lastFullString = null;
		for (String currentCode : cardsInSetList) {
			String[] splitStrings = currentCode.split("-");
			
			if(currentCode.equals("BLAR-EN10K")) {
				continue;
			}
			if(currentCode.contains("ENTKN")) {
				continue;
			}

			int numIndex = 0;

			try {
				while (!Character.isDigit(splitStrings[1].charAt(numIndex))) {
					numIndex++;
				}
			} catch (Exception e) {
				System.out.println("Issue found with " + setName + ": " + lastFullString + " and " + currentCode);
				lastFullString = currentCode;
				continue;
			}

			String identifiedPrefix = splitStrings[0];

			String identifiedLang = splitStrings[1].substring(0, numIndex);

			String identifiedNumString = splitStrings[1].substring(numIndex);

			Integer identifiedNumber = null;

			try {
				identifiedNumber = Integer.valueOf(identifiedNumString);
			} catch (Exception e) {
				System.out.println("Issue found with " + setName + ": " + lastFullString + " and " + currentCode);
				lastPrefix = identifiedPrefix;
				lastLang = identifiedLang;
				lastNum = -1;
				lastFullString = currentCode;
				continue;
			}

			// check for changed set id
			if (!identifiedPrefix.equals(lastPrefix) || !identifiedLang.equals(lastLang)) {
				lastPrefix = identifiedPrefix;
				lastLang = identifiedLang;
				lastNum = identifiedNumber;
				lastFullString = currentCode;
			}

			if (!(lastNum == identifiedNumber || lastNum == (identifiedNumber - 1))) {
				// issue found
				System.out.println("Issue found with " + setName + ": " + lastFullString + " and " + currentCode);
				lastPrefix = identifiedPrefix;
				lastLang = identifiedLang;
				lastNum = identifiedNumber;
				lastFullString = currentCode;
				continue;
			} else {
				lastPrefix = identifiedPrefix;
				lastLang = identifiedLang;
				lastNum = identifiedNumber;
				lastFullString = currentCode;
			}

		}
	}

	public static Pair<String, String> getGamePlayCardUUIDFromTitleOrGenerateNewWithSkillCheck(String name, SQLiteConnection db) throws SQLException {
		String gamePlayCardUUID = db.getGamePlayCardUUIDFromTitle(name);
		// try skill card
		if (gamePlayCardUUID == null) {
			gamePlayCardUUID = db.getGamePlayCardUUIDFromTitle(name + " (Skill Card)");
			if (gamePlayCardUUID != null) {
				name = name + " (Skill Card)";
			}
		}

		if (gamePlayCardUUID == null) {
			gamePlayCardUUID = UUID.randomUUID().toString();
		}

		return new Pair<>(gamePlayCardUUID, name);
	}

	public static String getStringOrNull(JsonNode current, String id) {
		try {
			String value = current.get(id).asText().trim();
			return value;
		} catch (Exception e) {
			return null;
		}
	}

	public static Integer getIntOrNegativeOne(JsonNode current, String id) {
		try {
			int value = current.get(id).asInt();
			return value;
		} catch (Exception e) {
			return -1;
		}
	}

}
