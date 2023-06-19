package ygodb.commonlibrary.utility;


import com.fasterxml.jackson.databind.JsonNode;
import javafx.util.Pair;
import ygodb.commonlibrary.bean.CardSet;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.bean.Rarity;
import ygodb.commonlibrary.bean.SetMetaData;
import ygodb.commonlibrary.connection.DatabaseHashMap;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.constant.Const;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class Util {

	private static KeyUpdateMap setNameMap = null;
	private static KeyUpdateMap cardNameMap = null;
	private static KeyUpdateMap rarityMap = null;
	private static KeyUpdateMap setNumberMap = null;
	private static HashMap<Integer, Integer> passcodeMap = null;
	private static QuadKeyUpdateMap quadKeyUpdateMap = null;

	private Util(){}

	public static OwnedCard formOwnedCard(String folder, String name, String quantity, String setCode, String condition,
			String printing, String priceBought, String dateBought, CardSet setIdentified, int passcode) {
		OwnedCard card = new OwnedCard();
		
		card.folderName = folder;
		card.cardName = name;
		card.quantity = Integer.parseInt(quantity);
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

		card.uuid = UUID.randomUUID().toString();
		
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
			int countCardsInList = db.getCountDistinctCardsInSet(setData.setName);

			if (countCardsInList != setData.numOfCards) {
				YGOLogger.info("Issue for " + setData.setName + " metadata:" + setData.numOfCards + " count:"
						+ countCardsInList);
			}
		}

		HashMap<String, SetMetaData> setMetaDataHashMap = new HashMap<>();

		for (SetMetaData s : list) {
			setMetaDataHashMap.put(s.setName, s);
		}

		ArrayList<String> setNames = db.getDistinctSetNames();

		for (String setName : setNames) {
			
			if (setName.contains("Tip Card") || setName.contains("(POR)")) {
				continue;
			}
			
			SetMetaData meta = setMetaDataHashMap.get(setName);

			if (meta == null) {
				YGOLogger.error("Issue for " + setName + " no metadata");
			}
			else {
				int cardsInSet = db.getCountDistinctCardsInSet(setName);

				if (cardsInSet != meta.numOfCards) {
					YGOLogger.info("Issue for " + setName + " metadata:" + meta.numOfCards + " count:" + cardsInSet);
				}
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
			YGOLogger.info("Invalid price input:" + input);
			price = new BigDecimal("0");
		}

		price = price.setScale(2, RoundingMode.HALF_UP);

		return price.toString();
	}

	public static CardSet findRarity(String priceBought,
									 String setNumber, String setName, String cardName, SQLiteConnection db) throws SQLException {

		List<CardSet> setRarities = DatabaseHashMap.getRaritiesOfCardInSetFromHashMap(setNumber, db);

		if (setRarities.isEmpty()) {
			// try removing color code

			String newSetNumber = setNumber.substring(0, setNumber.length() - 1);
			String colorCode = setNumber.substring(setNumber.length() - 1);

			setRarities = DatabaseHashMap.getRaritiesOfCardInSetFromHashMap(newSetNumber, db);

			for (CardSet c : setRarities) {
				c.colorVariant = colorCode;
			}
		}

		if (setRarities.size() == 1) {
			CardSet match = setRarities.get(0);

			match.rarityUnsure = 0;

			return match;
		}

		// if we haven't found any at all give up
		if (setRarities.isEmpty()) {
			YGOLogger.info("Unable to find anything for " + setNumber);
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
					CardSet match;
					if (i == 0) {
						match = setRarities.get(1);
					}
					else{
						match = setRarities.get(0);
					}
					match.rarityUnsure = 0;
					YGOLogger.info("Took a guess that " + setNumber + ":" + cardName + " is:" + match.setRarity);
					return match;
				}
			}
		}

		// try the closest price
		BigDecimal priceBoughtDec = new BigDecimal(priceBought);
		BigDecimal distance = new BigDecimal(setRarities.get(0).setPrice).subtract(priceBoughtDec).abs();
		int idx = 0;
		for (int c = 1; c < setRarities.size(); c++) {
			BigDecimal cDistance = new BigDecimal(setRarities.get(c).setPrice).subtract(priceBoughtDec).abs();
			if (cDistance.compareTo(distance) <= 0) {
				idx = c;
				distance = cDistance;
			}
		}

		CardSet rValue = setRarities.get(idx);
		rValue.rarityUnsure = 1;

		YGOLogger.info("Took a guess that " + setNumber + ":" + cardName + " is:" + rValue.setRarity);

		return rValue;

	}

	public static void checkForIssuesWithCardNamesInSet(String setName, SQLiteConnection db) throws SQLException {
		ArrayList<String> list = db.getDistinctGamePlayCardUUIDsInSetByName(setName);
		for (String i : list) {
			String title = db.getCardTitleFromGamePlayCardUUID(i);

			if(title == null) {
				YGOLogger.info("Not exactly 1 gamePlayCard found for ID " + i);
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
				YGOLogger.info("Issue found with " + setName + ": " + lastFullString + " and " + currentCode);
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
				YGOLogger.info("Issue found with " + setName + ": " + lastFullString + " and " + currentCode);
				lastPrefix = identifiedPrefix;
				lastLang = identifiedLang;
				lastNum = -1;
				lastFullString = currentCode;
				continue;
			}

			// check for changed set id
			if (!identifiedPrefix.equals(lastPrefix) || !identifiedLang.equals(lastLang)) {
				lastNum = identifiedNumber;
				lastFullString = currentCode;
			}

			if (!(lastNum == identifiedNumber || lastNum == (identifiedNumber - 1))) {
				// issue found
				YGOLogger.info("Issue found with " + setName + ": " + lastFullString + " and " + currentCode);
				lastPrefix = identifiedPrefix;
				lastLang = identifiedLang;
				lastNum = identifiedNumber;
				lastFullString = currentCode;
				continue;
			}
			lastPrefix = identifiedPrefix;
			lastLang = identifiedLang;
			lastNum = identifiedNumber;
			lastFullString = currentCode;
		}
	}

	public static Pair<String, String> getGamePlayCardUUIDFromTitleOrGenerateNewWithSkillCheck(String name, SQLiteConnection db) throws SQLException {
		String gamePlayCardUUID = db.getGamePlayCardUUIDFromTitle(name);
		// try skill card
		if (gamePlayCardUUID == null) {
			gamePlayCardUUID = db.getGamePlayCardUUIDFromTitle(name + Const.SKILL_CARD_NAME_APPEND);
			if (gamePlayCardUUID != null) {
				name = name + Const.SKILL_CARD_NAME_APPEND;
			}
		}

		if (gamePlayCardUUID == null) {
			gamePlayCardUUID = UUID.randomUUID().toString();
		}

		return new Pair<>(gamePlayCardUUID, name);
	}

	public static Pair<String, String> getGamePlayCardUUIDFromTitleOrNullWithSkillCheck(String name, SQLiteConnection db) throws SQLException {
		String gamePlayCardUUID = db.getGamePlayCardUUIDFromTitle(name);
		// try skill card
		if (gamePlayCardUUID == null) {
			gamePlayCardUUID = db.getGamePlayCardUUIDFromTitle(name + Const.SKILL_CARD_NAME_APPEND);
			if (gamePlayCardUUID != null) {
				name = name + Const.SKILL_CARD_NAME_APPEND;
			}
		}

		return new Pair<>(gamePlayCardUUID, name);
	}

	public static String getStringOrNull(JsonNode current, String id) {
		try {
			return current.get(id).asText().trim();
		} catch (Exception e) {
			return null;
		}
	}

	public static Integer getIntOrNegativeOne(JsonNode current, String id) {
		try {
			return current.get(id).asInt();
		} catch (Exception e) {
			return -1;
		}
	}

	public static String getApiResponseFromURL(URL url) throws IOException {
		String inline = "";
		InputStream inputStreamFromURL = url.openStream();

		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		for (int length; (length = inputStreamFromURL.read(buffer)) != -1; ) {
			result.write(buffer, 0, length);
		}

		inline = result.toString(StandardCharsets.UTF_8.name());
		inputStreamFromURL.close();
		return inline;
	}

	public static QuadKeyUpdateMap getQuadKeyUpdateMapInstance() {
		if (quadKeyUpdateMap == null) {

			try {
				String filename = "quadUpdateMapping.csv";

				InputStream inputStream = Util.class.getResourceAsStream("/" + filename);

				quadKeyUpdateMap = new QuadKeyUpdateMap(inputStream, "|");
			} catch (IOException e) {
				throw new UncheckedIOException(e);
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
				throw new UncheckedIOException(e);
			}

		}

		return setNameMap;
	}

	public static KeyUpdateMap getRarityMapInstance() {
		if (rarityMap == null) {
			try {
				String filename = "rarityUpdateMapping.csv";

				InputStream inputStream = Util.class.getResourceAsStream("/" + filename);

				rarityMap = new KeyUpdateMap(inputStream);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		return rarityMap;
	}

	public static KeyUpdateMap getSetNumberMapInstance() {
		if (setNumberMap == null) {
			try {
				String filename = "setNumberUpdateMapping.csv";

				InputStream inputStream = Util.class.getResourceAsStream("/" + filename);

				setNumberMap = new KeyUpdateMap(inputStream);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		return setNumberMap;
	}

	public static Map<Integer, Integer> getPasscodeMapInstance() {
		if (passcodeMap == null) {
			passcodeMap = new HashMap<>();

			passcodeMap.put(74677427, 74677422);
			passcodeMap.put(89943724, 89943723);


			//passcodeMap.put("", "");

		}

		return passcodeMap;
	}

	public static KeyUpdateMap getCardNameMapInstance() {
		if (cardNameMap == null) {
			try {
				String filename = "cardNameUpdateMapping.csv";

				InputStream inputStream = Util.class.getResourceAsStream("/" + filename);

				cardNameMap = new KeyUpdateMap(inputStream);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
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

		if(setName == null){
			return null;
		}

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
		KeyUpdateMap instance = getRarityMapInstance();

		String newRarity = instance.getValue(rarity);

		if(newRarity == null) {
			return rarity;
		}

		return newRarity;
	}

	public static String checkForTranslatedSetNumber(String setNumber) {
		KeyUpdateMap instance = getSetNumberMapInstance();

		String newSetNumber = instance.getValue(setNumber);

		if(newSetNumber == null) {
			return setNumber;
		}

		return newSetNumber;
	}

	public static String checkForTranslatedCardName(String cardName) {

		if(cardName == null){
			return null;
		}

		KeyUpdateMap instance = getCardNameMapInstance();

		String newName = instance.getValue(cardName.toLowerCase(Locale.ROOT));

		if(newName == null) {
			return cardName;
		}

		return newName;
	}

	public static int checkForTranslatedPasscode(int passcode) {
		Map<Integer, Integer> instance = getPasscodeMapInstance();

		Integer newPasscode = instance.get(passcode);

		if(newPasscode == null) {
			return passcode;
		}

		return newPasscode;
	}
}
