package ygodb.commonLibrary.utility;


import com.fasterxml.jackson.databind.JsonNode;
import javafx.util.Pair;
import ygodb.commonLibrary.bean.CardSet;
import ygodb.commonLibrary.bean.OwnedCard;
import ygodb.commonLibrary.bean.Rarity;
import ygodb.commonLibrary.bean.SetMetaData;
import ygodb.commonLibrary.connection.DatabaseHashMap;
import ygodb.commonLibrary.connection.SQLiteConnection;
import ygodb.commonLibrary.constant.Const;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Util {

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
			int countCardsinList = db.getCountDistinctCardsInSet(setData.setName);

			if (countCardsinList != setData.numOfCards) {
				YGOLogger.info("Issue for " + setData.setName + " metadata:" + setData.numOfCards + " count:"
						+ countCardsinList);
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
				YGOLogger.info("Issue for " + setName + " no metadata");
				continue;
			}

			int cardsInSet = db.getCountDistinctCardsInSet(setName);

			if (cardsInSet != meta.numOfCards) {
				YGOLogger.info("Issue for " + setName + " metadata:" + meta.numOfCards + " count:" + cardsInSet);
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

	public static CardSet findRarity(String priceBought, String dateBought, String folderName, String condition,
			String editionPrinting, String setNumber, String setName, String cardName, SQLiteConnection db) throws SQLException {

		List<CardSet> setRarities = DatabaseHashMap.getRaritiesOfCardInSetFromHashMap(setNumber, db);

		if (setRarities.isEmpty()) {
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
					System.out
							.println("Took a guess that " + setNumber + ":" + cardName + " is:" + match.setRarity);
					return match;
				}
			}
		}

		// try the closest price
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

		YGOLogger.info("Took a guess that " + setNumber + ":" + cardName + " is:" + rValue.setRarity);

		return rValue;

	}

	public static void checkForIssuesWithCardNamesInSet(String setName, SQLiteConnection db) throws SQLException {
		ArrayList<String> list = db.getDistinctGamePlayCardUUIDsInSetByName(setName);
		for (String i : list) {
			String title = db.getCardTitleFromGamePlayCardUUID(i);

			if(title == null) {
				YGOLogger.info("Not exactly 1 gameplaycard found for ID " + i);
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
				lastPrefix = identifiedPrefix;
				lastLang = identifiedLang;
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
}
