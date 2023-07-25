package ygodb.commonlibrary.utility;


import com.fasterxml.jackson.databind.JsonNode;
import javafx.util.Pair;
import ygodb.commonlibrary.bean.CardSet;
import ygodb.commonlibrary.bean.GamePlayCard;
import ygodb.commonlibrary.bean.NameAndColor;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.bean.Rarity;
import ygodb.commonlibrary.bean.SetMetaData;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Util {

	private static KeyUpdateMap setNameMap = null;
	private static KeyUpdateMap cardNameMap = null;
	private static KeyUpdateMap rarityMap = null;
	private static KeyUpdateMap setNumberMap = null;
	private static HashMap<Integer, Integer> passcodeMap = null;
	private static QuadKeyUpdateMap quadKeyUpdateMap = null;

	private static Set<String> setUrlsThatDontExist = null;

	private Util(){}

	public static OwnedCard formOwnedCard(String folder, String name, String quantity, String condition,
			String printing, String priceBought, String dateBought, CardSet setIdentified, int passcode) {
		OwnedCard card = new OwnedCard();
		
		card.setFolderName(folder);
		card.setCardName(name);
		card.setQuantity(Integer.parseInt(quantity));
		card.setSetCode(setIdentified.getSetCode());
		card.setCondition(condition);
		card.setEditionPrinting(printing);
		card.setPriceBought(normalizePrice(priceBought));
		card.setDateBought(dateBought);
		card.setSetRarity(setIdentified.getSetRarity());
		card.setGamePlayCardUUID(setIdentified.getGamePlayCardUUID());
		card.setColorVariant(setIdentified.getColorVariant());
		card.setSetName(setIdentified.getSetName());
		card.setSetNumber(setIdentified.getSetNumber());
		card.setRarityUnsure(setIdentified.getRarityUnsure());
		card.setPasscode(passcode);
		
		return card;
	}
	
	public static boolean doesCardExactlyMatchWithColor(String folder, String name, String setCode, String setNumber,
			String condition, String printing, String priceBought, String dateBought, String colorVariant, String rarity,
			String setName, int passcode, String gamePlayCardUUID, OwnedCard existingCard) {
		try{
			return setNumber.equals(existingCard.getSetNumber()) && priceBought.equals(existingCard.getPriceBought())
					&& dateBought.equals(existingCard.getDateBought()) && folder.equals(existingCard.getFolderName())
					&& condition.equals(existingCard.getCondition()) && printing.equals(existingCard.getEditionPrinting())
					&& name.equals(existingCard.getCardName()) && setCode.equals(existingCard.getSetCode())
					&& colorVariant.equals(existingCard.getColorVariant()) && rarity.equals(existingCard.getSetRarity())
					&& setName.equals(existingCard.getSetName()) && passcode == existingCard.getPasscode()
					&& gamePlayCardUUID.equals(existingCard.getGamePlayCardUUID()
			);
		}catch (Exception e){
			YGOLogger.logException(e);
			throw e;
		}
    }

	public static void checkSetCounts(SQLiteConnection db) throws SQLException {
		ArrayList<SetMetaData> list = db.getAllSetMetaDataFromSetData();

		for (SetMetaData setData : list) {
			int countCardsInList = db.getCountDistinctCardsInSet(setData.getSetName());

			if (countCardsInList != setData.getNumOfCards()) {
				YGOLogger.info("Issue for " + setData.getSetName() + " metadata:" + setData.getNumOfCards() + " count:"
						+ countCardsInList);
			}
		}

		HashMap<String, SetMetaData> setMetaDataHashMap = new HashMap<>();

		for (SetMetaData s : list) {
			setMetaDataHashMap.put(s.getSetName(), s);
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

				if (cardsInSet != meta.getNumOfCards()) {
					YGOLogger.info("Issue for " + setName + " metadata:" + meta.getNumOfCards() + " count:" + cardsInSet);
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

	public static void checkForIssuesWithCardNamesInSet(String setName, SQLiteConnection db) throws SQLException {
		ArrayList<String> list = db.getDistinctGamePlayCardUUIDsInSetByName(setName);
		for (String i : list) {
			String title = db.getCardTitleFromGamePlayCardUUID(i);

			if(title == null) {
				YGOLogger.info("Not exactly 1 gamePlayCard found for ID " + i);
			}

		}
	}

	public static String getPrefixFromSetNumber(String setNumber){
		String[] splitStrings = setNumber.split("-");

		if(splitStrings.length != 2){
			return null;
		}

		return splitStrings[0];
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
		InputStream inputStreamFromURL = null;
		try {
			inputStreamFromURL = url.openStream();

			ByteArrayOutputStream result = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			for (int length; (length = inputStreamFromURL.read(buffer)) != -1; ) {
				result.write(buffer, 0, length);
			}
			inline = result.toString(StandardCharsets.UTF_8.name());
		} catch (Exception e) {
			YGOLogger.logException(e);
			throw e;
		} finally {
			if (inputStreamFromURL != null) {
				try {
					inputStreamFromURL.close();
				} catch (IOException e) {
					YGOLogger.logException(e);
				}
			}
		}

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
			passcodeMap.put(27847700, 24094653);


			//passcodeMap.put("", "");

		}

		return passcodeMap;
	}

	public static Set<String> getSetUrlsThatDontExistInstance() {
		if (setUrlsThatDontExist == null) {
			setUrlsThatDontExist = new HashSet<>();

			setUrlsThatDontExist.add("https://store.tcgplayer.com/yugioh/duelist-league-promo/enemy-controller?partner=YGOPRODeck&utm_campaign=affiliate&utm_medium=card_set_url_api&utm_source=YGOPRODeck");
			setUrlsThatDontExist.add("https://store.tcgplayer.com/yugioh/legendary-duelists-season-3/number-15-gimmick-puppet-giant-grinder?partner=YGOPRODeck&utm_campaign=affiliate&utm_medium=card_set_url_api&utm_source=YGOPRODeck");
			setUrlsThatDontExist.add("https://store.tcgplayer.com/yugioh/duelist-league-promo/axe-of-despair?partner=YGOPRODeck&utm_campaign=affiliate&utm_medium=card_set_url_api&utm_source=YGOPRODeck");
			setUrlsThatDontExist.add("https://store.tcgplayer.com/yugioh/tactical-evolution/gemini-summoner-taev-ensp1?partner=YGOPRODeck&utm_campaign=affiliate&utm_medium=card_set_url_api&utm_source=YGOPRODeck");
			setUrlsThatDontExist.add("https://store.tcgplayer.com/yugioh/5ds-2008-starter-deck/colossal-fighter-common?partner=YGOPRODeck&utm_campaign=affiliate&utm_medium=card_set_url_api&utm_source=YGOPRODeck");
			setUrlsThatDontExist.add("https://store.tcgplayer.com/yugioh/5ds-2008-starter-deck/gaia-knight-the-force-of-earth-common?partner=YGOPRODeck&utm_campaign=affiliate&utm_medium=card_set_url_api&utm_source=YGOPRODeck");
			setUrlsThatDontExist.add("https://store.tcgplayer.com/yugioh/5ds-2008-starter-deck/junk-warrior-common?partner=YGOPRODeck&utm_campaign=affiliate&utm_medium=card_set_url_api&utm_source=YGOPRODeck");
			setUrlsThatDontExist.add("https://store.tcgplayer.com/yugioh/maze-of-memories/psi-beast-cr?partner=YGOPRODeck&utm_campaign=affiliate&utm_medium=card_set_url_api&utm_source=YGOPRODeck");
			setUrlsThatDontExist.add("https://store.tcgplayer.com/yugioh/toon-chaos/psy-frame-driver-cr?partner=YGOPRODeck&utm_campaign=affiliate&utm_medium=card_set_url_api&utm_source=YGOPRODeck");
			setUrlsThatDontExist.add("https://store.tcgplayer.com/yugioh/dark-legends-promo-card/gorz-the-emissary-of-darkness?partner=YGOPRODeck&utm_campaign=affiliate&utm_medium=card_set_url_api&utm_source=YGOPRODeck");
			//setUrlsThatDontExist.add();
		}

		return setUrlsThatDontExist;
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

		String lowerCaseName = cardName.toLowerCase(Locale.ROOT);

		String newName = instance.getValue(lowerCaseName);

		if(newName == null || newName.equals(lowerCaseName)) {
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
	
	public static String getLowestPriceString(String input1, String input2){
		BigDecimal zero = new BigDecimal(0);
		BigDecimal price;
		BigDecimal priceFirst;
		boolean noFirstOption = false;
		boolean noSecondOption = false;

		if(input1 == null){
			price = new BigDecimal(Integer.MAX_VALUE);
			noFirstOption = true;
		}
		else{
			price = new BigDecimal(input1);
		}

		if(input2 == null){
			priceFirst = new BigDecimal(Integer.MAX_VALUE);
			noSecondOption = true;
		}
		else{
			priceFirst = new BigDecimal(input2);
		}

		if(noFirstOption && noSecondOption){
			return Const.ZERO_PRICE_STRING;
		}
		if(noFirstOption || zero.compareTo(price) == 0){
			return input2;
		}
		if(noSecondOption || zero.compareTo(priceFirst) == 0){
			return input1;
		}

		if(price.compareTo(priceFirst) < 0){
			return input1;
		}
		else{
			return input2;
		}
	}

	public static String removeRarityStringsFromName(String name) {
		if (name == null) {
			return null;
		}

		StringBuilder builder = new StringBuilder(name);
		for (Rarity rarity : Rarity.values()) {
			String rarityWithParens = "("+rarity.toString()+")";
			int index = builder.indexOf(rarityWithParens);
			while (index != -1) {
				builder.delete(index, index + rarityWithParens.length());
				index = builder.indexOf(rarityWithParens);
			}
		}

		return builder.toString().trim();
	}

	public static CardSet createUnknownCardSet(String name, String setName, SQLiteConnection db) throws SQLException {
		YGOLogger.error("Unknown setCode for card name and set: " + name + ":" + setName);
		CardSet setIdentified = new CardSet();
		setIdentified.setRarityUnsure(1);
		setIdentified.setColorVariant(Const.DEFAULT_COLOR_VARIANT);
		setIdentified.setSetName(setName);
		setIdentified.setSetNumber(null);
		setIdentified.setSetCode(null);
		setIdentified.setGamePlayCardUUID(db.getGamePlayCardUUIDFromTitle(name));
		return setIdentified;
	}

	public static NameAndColor getNameAndColor(String name) {
		String colorVariant = Const.DEFAULT_COLOR_VARIANT;
		String[] colorVariants = {"(Red)", "(Blue)", "(Green)", "(Purple)", "(Alternate Art)"};

		for (String variant : colorVariants) {
			if (name.contains(variant)) {
				name = name.replace(variant, "").trim();

				switch (variant) {
					case "(Red)":
						colorVariant = "r";
						break;
					case "(Blue)":
						colorVariant = "b";
						break;
					case "(Green)":
						colorVariant = "g";
						break;
					case "(Purple)":
						colorVariant = "p";
						break;
					case "(Alternate Art)":
						colorVariant = "a";
						break;
					default:
						break;
				}
				break;
			}
		}
		return new NameAndColor(name, colorVariant);
	}

	public static String millisToShortDHMS(long duration) {
		String res = "";
		long days = TimeUnit.MILLISECONDS.toDays(duration);
		long hours = TimeUnit.MILLISECONDS.toHours(duration) -
				TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(duration));
		long minutes = TimeUnit.MILLISECONDS.toMinutes(duration) -
				TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration));
		long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) -
				TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration));
		long millis = TimeUnit.MILLISECONDS.toMillis(duration) -
				TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(duration));

		if (days == 0) res = String.format(Locale.ENGLISH, "%02d:%02d:%02d.%04d", hours, minutes, seconds, millis);
		else res = String.format(Locale.ENGLISH, "%dd %02d:%02d:%02d.%04d", days, hours, minutes, seconds, millis);
		return res;
	}

	public static GamePlayCard insertGameplayCardFromYGOPRO(JsonNode current, List<OwnedCard> ownedCardsToCheck, SQLiteConnection db) throws SQLException {

		String name = getStringOrNull(current, Const.YGOPRO_CARD_NAME);
		String type = getStringOrNull(current, Const.YGOPRO_CARD_TYPE);
		Integer passcode = getIntOrNegativeOne(current, Const.YGOPRO_CARD_PASSCODE);
		String desc = getStringOrNull(current, Const.YGOPRO_CARD_TEXT);
		String attribute = getStringOrNull(current, Const.YGOPRO_ATTRIBUTE);
		String race = getStringOrNull(current, Const.YGOPRO_RACE);
		String linkValue = getStringOrNull(current, Const.YGOPRO_LINK_VALUE);
		String level = getStringOrNull(current, Const.YGOPRO_LEVEL_RANK);
		String scale = getStringOrNull(current, Const.YGOPRO_PENDULUM_SCALE);
		String atk = getStringOrNull(current, Const.YGOPRO_ATTACK);
		String def = getStringOrNull(current, Const.YGOPRO_DEFENSE);
		String archetype = getStringOrNull(current, Const.YGOPRO_ARCHETYPE);

		GamePlayCard gamePlayCard = new GamePlayCard();

		name = checkForTranslatedCardName(name);
		passcode = checkForTranslatedPasscode(passcode);

		gamePlayCard.setCardName(name);
		gamePlayCard.setCardType(type);
		gamePlayCard.setArchetype(archetype);
		gamePlayCard.setPasscode(passcode);

		gamePlayCard.setGamePlayCardUUID(db.getGamePlayCardUUIDFromPasscode(passcode));

		if (gamePlayCard.getGamePlayCardUUID() == null) {
			Pair<String, String> uuidAndName = getGamePlayCardUUIDFromTitleOrGenerateNewWithSkillCheck(name, db);

			gamePlayCard.setGamePlayCardUUID(uuidAndName.getKey());
			gamePlayCard.setCardName(uuidAndName.getValue());
		}

		gamePlayCard.setDesc(desc);
		gamePlayCard.setAttribute(attribute);
		gamePlayCard.setRace(race);
		gamePlayCard.setLinkVal(linkValue);
		gamePlayCard.setScale(scale);
		gamePlayCard.setLevel(level);
		gamePlayCard.setAtk(atk);
		gamePlayCard.setDef(def);

		db.replaceIntoGamePlayCard(gamePlayCard);

		for (OwnedCard currentOwnedCard : ownedCardsToCheck) {
			if (currentOwnedCard.getGamePlayCardUUID().equals(gamePlayCard.getGamePlayCardUUID())) {
				currentOwnedCard.setPasscode(passcode);
				db.updateOwnedCardByUUID(currentOwnedCard);
			}
		}

		return gamePlayCard;
	}

	public static void insertOrIgnoreCardSetsForOneCard(Iterator<JsonNode> setIterator, String name, String gamePlayCardUUID, SQLiteConnection db)
			throws SQLException {

		while (setIterator.hasNext()) {

			JsonNode currentSet = setIterator.next();

			String setCode = null;
			String setName = null;
			String setRarity = null;
			//String setPrice = null;

			try {
				setCode = getStringOrNull(currentSet, Const.YGOPRO_SET_CODE);
				setName = getStringOrNull(currentSet, Const.YGOPRO_SET_NAME);
				setRarity = getStringOrNull(currentSet, Const.YGOPRO_SET_RARITY);
				//setPrice = Util.getStringOrNull(currentSet, Const.YGOPRO_SET_PRICE);
			} catch (Exception e) {
				YGOLogger.error("issue found on " + name);
				continue;
			}

			name = checkForTranslatedCardName(name);
			setRarity = checkForTranslatedRarity(setRarity);
			setName = checkForTranslatedSetName(setName);
			setCode = checkForTranslatedSetNumber(setCode);

			List<String> translatedList = checkForTranslatedQuadKey(name, setCode, setRarity, setName);
			name = translatedList.get(0);
			setCode = translatedList.get(1);
			setRarity = translatedList.get(2);
			setName = translatedList.get(3);

			db.insertOrIgnoreIntoCardSet(setCode, setRarity, setName, gamePlayCardUUID, name, null, null);
		}
	}
}
