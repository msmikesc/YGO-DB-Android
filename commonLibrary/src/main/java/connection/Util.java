package connection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import bean.CardSet;
import bean.OwnedCard;
import bean.SetMetaData;
import bean.Rarity;

public class Util {

	public static BigDecimal one = new BigDecimal(1);
	public static BigDecimal two = new BigDecimal(2);
	public static BigDecimal cent50 = new BigDecimal(.5);
	public static BigDecimal ten = new BigDecimal(10);
	public static BigDecimal thirty = new BigDecimal(30);
	public static BigDecimal oneCent = new BigDecimal(0.01);
	
	public static String defaultColorVariant = "-1";
	
	private static HashMap<String, String> setNameMap = null;
	

	public static HashMap<String, String> getSetNameMapInstance() {
		if (setNameMap == null) {
			setNameMap = new HashMap<String,String>();
			
			setNameMap.put("King of Games: Yugi's Legendary Decks", "Yugi's Legendary Decks");
			setNameMap.put("Yugi'S Legendary Decks", "Yugi's Legendary Decks");
			setNameMap.put("Legendary Collection 2", "Legendary Collection 2: The Duel Academy Years Mega Pack");
			setNameMap.put("2018 Mega-Tins Mega Pack", "2018 Mega-Tin Mega Pack");
			setNameMap.put("2017 Mega-Tins Mega Pack", "2017 Mega-Tin Mega Pack");
			setNameMap.put("2016 Mega-Tins Mega Pack", "2016 Mega-Tin Mega Pack");
			setNameMap.put("2015 Mega-Tins Mega Pack", "2015 Mega-Tin Mega Pack");
			setNameMap.put("2014 Mega-Tins Mega Pack", "2014 Mega-Tin Mega Pack");
			setNameMap.put("Return of the Duelist SE", "Return of the Duelist: Special Edition");
			setNameMap.put("Duelist Pack 7: Jesse Anderson", "Duelist Pack: Jesse Anderson");
			setNameMap.put("Yu-Gi-Oh! Movie Exclusive Pack", "Exclusive Pack");
			setNameMap.put("Collectible Tins 2013 Wave 2", "Collectible Tins 2013");
			setNameMap.put("Collectible Tins 2012 Wave 2", "Collectible Tins 2012");
			setNameMap.put("Collectible Tins 2011 Wave 2", "Collectible Tins 2011");
			setNameMap.put("Collectible Tins 2006 Wave 2", "Collectible Tins 2006");
			setNameMap.put("Collectible Tins 2007 Wave 2", "Collectible Tins 2007");
			setNameMap.put("Collectible Tins 2008 Wave 2", "Collectible Tins 2008");
			setNameMap.put("Collectible Tins 2009 Wave 2", "Collectible Tins 2009");
			setNameMap.put("Collectible Tins 2010 Wave 2", "Collectible Tins 2010");
			setNameMap.put("Collectible Tins 2013 Wave 1", "Collectible Tins 2013");
			setNameMap.put("Collectible Tins 2012 Wave 1", "Collectible Tins 2012");
			setNameMap.put("Collectible Tins 2011 Wave 1", "Collectible Tins 2011");
			setNameMap.put("Collectible Tins 2006 Wave 1", "Collectible Tins 2006");
			setNameMap.put("Collectible Tins 2007 Wave 1", "Collectible Tins 2007");
			setNameMap.put("Collectible Tins 2008 Wave 1", "Collectible Tins 2008");
			setNameMap.put("Collectible Tins 2009 Wave 1", "Collectible Tins 2009");
			setNameMap.put("Collectible Tins 2010 Wave 1", "Collectible Tins 2010");
			setNameMap.put("2013 Collectible Tins", "Collectible Tins 2013");
			setNameMap.put("2012 Collectible Tins", "Collectible Tins 2012");
			setNameMap.put("2011 Collectible Tins", "Collectible Tins 2011");
			setNameMap.put("2006 Collectible Tins", "Collectible Tins 2006");
			setNameMap.put("2007 Collectible Tins", "Collectible Tins 2007");
			setNameMap.put("2008 Collectible Tins", "Collectible Tins 2008");
			setNameMap.put("2009 Collectible Tins", "Collectible Tins 2009");
			setNameMap.put("2010 Collectible Tins", "Collectible Tins 2010");
			setNameMap.put("2013 Collectors Tins", "Collectible Tins 2013");
			setNameMap.put("2012 Collectors Tins", "Collectible Tins 2012");
			setNameMap.put("2011 Collectors Tins", "Collectible Tins 2011");
			setNameMap.put("2006 Collectors Tins", "Collectible Tins 2006");
			setNameMap.put("2007 Collectors Tins", "Collectible Tins 2007");
			setNameMap.put("2008 Collectors Tins", "Collectible Tins 2008");
			setNameMap.put("2009 Collectors Tins", "Collectible Tins 2009");
			setNameMap.put("2010 Collectors Tins", "Collectible Tins 2010");
			setNameMap.put("Collectible Tins 2012 Wave 2.5", "Collectible Tins 2012");
			setNameMap.put("2013 Collectible Tins Wave 1", "Collectible Tins 2013");
			setNameMap.put("2013 Collectible Tins Wave 2", "Collectible Tins 2013");
			setNameMap.put("Duelist Pack 8: Yusei Fudo", "Duelist Pack: Yusei");
			setNameMap.put("Duelist Pack Collection Tin", "Duelist Pack Collection Tin 2009");
			
			
			//setNameMap.put("", "");
			
		}

		return setNameMap;
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
		
		setName = flipStructureEnding(setName, "Starter Deck");
		setName = flipStructureEnding(setName, "Structure Deck");
		
		HashMap<String, String> instance = getSetNameMapInstance();
		
		String newSetName = instance.get(setName);
		
		if(newSetName == null) {
			return setName;
		}
		
		return newSetName;
	}
	
	public static OwnedCard formOwnedCard(String folder, String name, String quantity, String setCode, String condition,
			String printing, String priceBought, String dateBought, CardSet setIdentified, String priceLow, String priceMid,
			String priceMarket) {
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
		card.id = setIdentified.id;
		card.colorVariant = setIdentified.colorVariant;
		card.setName = setIdentified.setName;
		card.setNumber = setIdentified.setNumber;
		card.rarityUnsure = setIdentified.rarityUnsure;
		
		card.priceLow = priceLow;
		card.priceMid = priceMid;
		card.priceMarket = priceMarket;
		card.UUID = UUID.randomUUID().toString();
		
		return card;
	}
	
	public static boolean doesCardExactlyMatch(String folder, String name, String setCode, String setNumber,
			String condition, String printing, String priceBought, String dateBought, OwnedCard existingCard)
			throws SQLException {
		if (setNumber.equals(existingCard.setNumber) && priceBought.equals(existingCard.priceBought)
				&& dateBought.equals(existingCard.dateBought) && folder.equals(existingCard.folderName)
				&& condition.equals(existingCard.condition) && printing.equals(existingCard.editionPrinting)) {
			return true;
		}
		return false;
	}
	
	public static boolean doesCardExactlyMatchWithColor(String folder, String name, String setCode, String setNumber,
			String condition, String printing, String priceBought, String dateBought, String colorVariant,
			OwnedCard existingCard) throws SQLException {
		if (setNumber.equals(existingCard.setNumber) && priceBought.equals(existingCard.priceBought)
				&& dateBought.equals(existingCard.dateBought) && folder.equals(existingCard.folderName)
				&& condition.equals(existingCard.condition) && printing.equals(existingCard.editionPrinting)
				&& colorVariant.equals(existingCard.colorVariant)) {
			return true;
		}
		return false;
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
		
		BigDecimal price = new BigDecimal(input);

		price = price.setScale(2, RoundingMode.HALF_UP);

		return price.toString();
	}

	public static CardSet getFromOwnedCard(OwnedCard o) {
		CardSet c = new CardSet();

		c.cardName = o.cardName;
		c.colorVariant = o.colorVariant;
		c.id = o.id;
		c.rarityUnsure = o.rarityUnsure;
		c.setName = o.setName;
		c.setNumber = o.setNumber;
		c.setRarity = o.setRarity;
		c.setPrice = o.setRarity;

		return c;
	}

	public static CardSet findRarity(String priceBought, String dateBought, String folderName, String condition,
			String editionPrinting, String setNumber, String setName, String cardName, SQLiteConnection db) throws SQLException {

		ArrayList<CardSet> setRarities = DatabaseHashMap.getRaritiesOfCardInSetFromHashMap(setNumber, db);

		if (setRarities.size() == 0) {
			// try removing color code

			String newSetNumber = setNumber.substring(0, setNumber.length() - 1);
			String colorcode = setNumber.substring(setNumber.length() - 1, setNumber.length());

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
			setIdentified.id = db.getCardIdFromTitle(cardName);

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

	public static String getAdjustedPriceFromRarity(String rarity, String inputPrice) {

		BigDecimal price = new BigDecimal(inputPrice);

		if (price.compareTo(oneCent) < 0) {
			price = oneCent;
		}

		if (rarity.contains("Collector")) {
			price = price.add(thirty);
		}

		if (rarity.contains("Ultimate")) {
			price = price.add(ten);
		}

		if (rarity.contains("Starlight")) {
			price = price.add(thirty);
		}

		if (rarity.contains("Ghost")) {
			price = price.add(thirty);
		}

		if (rarity.contains("Duel Terminal")) {
			price = price.add(cent50);
		}

		if (rarity.contains("Gold")) {
			price = price.add(one);
		}

		if (rarity.contains("Starfoil")) {
			price = price.add(cent50);
		}

		if (rarity.contains("Shatterfoil")) {
			price = price.add(cent50);
		}

		if (rarity.contains("Mosaic")) {
			price = price.add(cent50);
		}

		if (rarity.contains("Super")) {
			price = price.add(cent50);
		}

		if (rarity.contains("Ultra")) {
			price = price.add(one);
		}

		if (rarity.contains("Secret")) {
			price = price.add(two);
		}

		price = price.setScale(2, RoundingMode.HALF_UP);

		return price.toString();

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

			String identifiedNumString = splitStrings[1].substring(numIndex, splitStrings[1].length());

			Integer identifiedNumber = null;

			try {
				identifiedNumber = new Integer(identifiedNumString);
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

	public static String getStringOrNull(JSONObject current, String id) {
		try {
			String value = current.getString(id);
			return value;
		} catch (JSONException e) {
			return null;
		}
	}

	public static Integer getIntOrNull(JSONObject current, String id) {
		try {
			int value = current.getInt(id);
			return value;
		} catch (JSONException e) {
			return null;
		}
	}

}
