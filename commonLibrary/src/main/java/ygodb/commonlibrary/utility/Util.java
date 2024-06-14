package ygodb.commonlibrary.utility;


import javafx.util.Pair;
import ygodb.commonlibrary.bean.CardSet;
import ygodb.commonlibrary.bean.NameAndColor;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.bean.Rarity;
import ygodb.commonlibrary.bean.SetMetaData;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.constant.Const;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Util {

	private static KeyUpdateMap setNameMap = null;
	private static KeyUpdateMap cardNameMap = null;
	private static KeyUpdateMap rarityMap = null;
	private static KeyUpdateMap setNumberMap = null;
	private static KeyUpdateMap editionMap = null;
	private static KeyUpdateMap passcodeMap = null;
	private static KeyUpdateMap ygoProImagePasscodeMap = null;
	private static QuadKeyUpdateMap quadKeyUpdateMap = null;

	private static Set<String> setUrlsThatDoNotExist = null;

	private Util() {
	}

	public static void checkSetCounts(SQLiteConnection db) throws SQLException {
		List<SetMetaData> list = db.getAllSetMetaDataFromSetData();

		for (SetMetaData setData : list) {
			int countCardsInList = db.getCountDistinctCardsInSet(setData.getSetName());

			if (countCardsInList != setData.getNumOfCards()) {
				YGOLogger.info("Issue for " + setData.getSetName() + " metadata:" + setData.getNumOfCards() + " count:" + countCardsInList);
			}
		}

		HashMap<String, SetMetaData> setMetaDataHashMap = new HashMap<>();

		for (SetMetaData s : list) {
			setMetaDataHashMap.put(s.getSetName(), s);
		}

		List<String> setNames = db.getDistinctSetNames();

		for (String setName : setNames) {

			if (setName.contains("Tip Card") || setName.contains("(POR)")) {
				continue;
			}

			SetMetaData meta = setMetaDataHashMap.get(setName);

			if (meta == null) {
				YGOLogger.error("Issue for " + setName + " no metadata");
			} else {
				int cardsInSet = db.getCountDistinctCardsInSet(setName);

				if (cardsInSet != meta.getNumOfCards()) {
					YGOLogger.info("Issue for " + setName + " metadata:" + meta.getNumOfCards() + " count:" + cardsInSet);
				}
			}
		}

	}

	public static String normalizePrice(String input) {

		if (input == null || input.trim().equals("")) {
			return null;
		}

		BigDecimal price;

		try {
			price = new BigDecimal(input.replace(",", ""));
		} catch (Exception e) {
			YGOLogger.info("Invalid price input:" + input);
			price = new BigDecimal("0");
		}

		price = price.setScale(2, RoundingMode.HALF_UP);

		return price.toString();
	}

	public static void checkForIssuesWithCardNamesInSet(String setName, SQLiteConnection db) throws SQLException {
		List<String> list = db.getDistinctGamePlayCardUUIDsInSetByName(setName);
		for (String i : list) {
			String title = db.getCardTitleFromGamePlayCardUUID(i);

			if (title == null) {
				YGOLogger.info("Not exactly 1 gamePlayCard found for ID " + i);
			}

		}
	}

	public static String getPrefixFromSetNumber(String setNumber) {
		String[] splitStrings = setNumber.split("-");

		if (splitStrings.length != 2) {
			return null;
		}

		return splitStrings[0];
	}

	public static void checkForIssuesWithSet(String setName, SQLiteConnection db) throws SQLException {

		List<String> cardsInSetList = db.getSortedSetNumbersInSetByName(setName);

		String lastPrefix = null;
		String lastLang = null;
		int lastNum = -1;
		String lastFullString = null;
		for (String currentCode : cardsInSetList) {
			String[] splitStrings = currentCode.split("-");

			if (currentCode.equals("BLAR-EN10K")) {
				continue;
			}
			if (currentCode.contains("ENTKN")) {
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

	public static Pair<String, String> getGamePlayCardUUIDFromTitleOrGenerateNewWithSkillCheck(String name, SQLiteConnection db)
			throws SQLException {
		Pair<String, String> data = getGamePlayCardUUIDFromTitleOrNullWithSkillCheck(name, db);

		if (data.getKey() == null) {
			return new Pair<>(UUID.randomUUID().toString(), data.getValue());
		}

		return data;
	}

	public static Pair<String, String> getGamePlayCardUUIDFromTitleOrNullWithSkillCheck(String name, SQLiteConnection db)
			throws SQLException {
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

	public static KeyUpdateMap getEditionMapInstance() {
		if (editionMap == null) {
			try {
				String filename = "editionUpdateMapping.csv";

				InputStream inputStream = Util.class.getResourceAsStream("/" + filename);

				editionMap = new KeyUpdateMap(inputStream);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		return editionMap;
	}

	public static KeyUpdateMap getPasscodeMapInstance() {
		if (passcodeMap == null) {
			try {
				String filename = "passcodeUpdateMapping.csv";

				InputStream inputStream = Util.class.getResourceAsStream("/" + filename);

				passcodeMap = new KeyUpdateMap(inputStream);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		return passcodeMap;
	}

	public static KeyUpdateMap getYgoProImagePasscodeMapInstance() {
		if (ygoProImagePasscodeMap == null) {
			try {
				String filename = "ygoProImagePasscodeUpdateMapping.csv";

				InputStream inputStream = Util.class.getResourceAsStream("/" + filename);

				ygoProImagePasscodeMap = new KeyUpdateMap(inputStream);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}

		}

		return ygoProImagePasscodeMap;
	}

	public static Set<String> getSetUrlsThatDoNotExistInstance() {
		if (setUrlsThatDoNotExist == null) {
			setUrlsThatDoNotExist = new HashSet<>();

			setUrlsThatDoNotExist.add(
					"https://tcgplayer.pxf.io/c/4913399/1830156/21018?u=https%3A%2F%2Fstore.tcgplayer.com%2Fyugioh%2Fduelist-league-promo%2Fenemy-controller");
			setUrlsThatDoNotExist.add(
					"https://tcgplayer.pxf.io/c/4913399/1830156/21018?u=https%3A%2F%2Fstore.tcgplayer.com%2Fyugioh%2Flegendary-duelists-season-3%2Fnumber-15-gimmick-puppet-giant-grinder");
			setUrlsThatDoNotExist.add(
					"https://tcgplayer.pxf.io/c/4913399/1830156/21018?u=https%3A%2F%2Fstore.tcgplayer.com%2Fyugioh%2Fduelist-league-promo%2Faxe-of-despair");
			setUrlsThatDoNotExist.add(
					"https://tcgplayer.pxf.io/c/4913399/1830156/21018?u=https%3A%2F%2Fstore.tcgplayer.com%2Fyugioh%2Ftactical-evolution%2Fgemini-summoner-taev-ensp1");
			setUrlsThatDoNotExist.add(
					"https://tcgplayer.pxf.io/c/4913399/1830156/21018?u=https%3A%2F%2Fstore.tcgplayer.com%2Fyugioh%2F5ds-2008-starter-deck%2Fcolossal-fighter-common");
			setUrlsThatDoNotExist.add(
					"https://tcgplayer.pxf.io/c/4913399/1830156/21018?u=https%3A%2F%2Fstore.tcgplayer.com%2Fyugioh%2F5ds-2008-starter-deck%2Fgaia-knight-the-force-of-earth-common");
			setUrlsThatDoNotExist.add(
					"https://tcgplayer.pxf.io/c/4913399/1830156/21018?u=https%3A%2F%2Fstore.tcgplayer.com%2Fyugioh%2F5ds-2008-starter-deck%2Fjunk-warrior-common");
			setUrlsThatDoNotExist.add(
					"https://tcgplayer.pxf.io/c/4913399/1830156/21018?u=https%3A%2F%2Fstore.tcgplayer.com%2Fyugioh%2Fmaze-of-memories%2Fpsi-beast-cr");
			setUrlsThatDoNotExist.add(
					"https://tcgplayer.pxf.io/c/4913399/1830156/21018?u=https%3A%2F%2Fstore.tcgplayer.com%2Fyugioh%2Ftoon-chaos%2Fpsy-frame-driver-cr");
			setUrlsThatDoNotExist.add(
					"https://tcgplayer.pxf.io/c/4913399/1830156/21018?u=https%3A%2F%2Fstore.tcgplayer.com%2Fyugioh%2Fdark-legends-promo-card%2Fgorz-the-emissary-of-darkness");
			setUrlsThatDoNotExist.add(
					"https://tcgplayer.pxf.io/c/4913399/1830156/21018?u=https%3A%2F%2Fstore.tcgplayer.com%2Fyugioh%2Fpremium-pack-2%2Fwar-lion-ritual-sr");
			setUrlsThatDoNotExist.add(
					"https://tcgplayer.pxf.io/c/4913399/1830156/21018?u=https%3A%2F%2Fwww.tcgplayer.com%2Fproduct%2F543919%2Fyugioh-speed-duel-gx-midterm-destruction-white-veil-secret-rare");
			setUrlsThatDoNotExist.add(
					"https://tcgplayer.pxf.io/c/4913399/1830156/21018?u=https%3A%2F%2Fwww.tcgplayer.com%2Fproduct%2F543835%2Fyugioh-speed-duel-gx-midterm-destruction-en-shuffle-secret-rare");
			setUrlsThatDoNotExist.add(
					"https://tcgplayer.pxf.io/c/4913399/1830156/21018?u=https%3A%2F%2Fwww.tcgplayer.com%2Fproduct%2F91461%2Fyugioh-duelist-league-promo-enemy-controller");
			setUrlsThatDoNotExist.add(
					"https://tcgplayer.pxf.io/c/4913399/1830156/21018?u=https%3A%2F%2Fwww.tcgplayer.com%2Fproduct%2F183032%2Fyugioh-5ds-2008-starter-deck-colossal-fighter");
			setUrlsThatDoNotExist.add(
					"https://tcgplayer.pxf.io/c/4913399/1830156/21018?u=https%3A%2F%2Fwww.tcgplayer.com%2Fproduct%2F183031%2Fyugioh-5ds-2008-starter-deck-gaia-knight-the-force-of-earth-common");
			setUrlsThatDoNotExist.add(
					"https://tcgplayer.pxf.io/c/4913399/1830156/21018?u=https%3A%2F%2Fwww.tcgplayer.com%2Fproduct%2F183029%2Fyugioh-5ds-2008-starter-deck-junk-warrior");
			setUrlsThatDoNotExist.add(
					"https://tcgplayer.pxf.io/c/4913399/1830156/21018?u=https%3A%2F%2Fstore.tcgplayer.com%2Fyugioh%2Fmaximum-gold%2Fharpies-feather-duster");
			//setUrlsThatDoNotExist.add();
		}

		return setUrlsThatDoNotExist;
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

		if (input.endsWith(match)) {
			input = match + ": " + input.replace(match, "").trim();
		}
		return input;

	}

	public static String checkForTranslatedSetName(String setName) {

		if (setName == null) {
			return null;
		}

		if (setName.contains("The Lost Art Promotion")) {
			setName = "The Lost Art Promotion";
		}

		if (setName.contains("(Worldwide English)")) {
			setName = setName.replace("(Worldwide English)", "");
			setName = setName.trim();
		}

		if (setName.contains("Sneak Peek Participation Card")) {
			setName = setName.replace("Sneak Peek Participation Card", "");
			setName = setName.trim();
		}

		if (setName.contains("Sneak Peek participation cards")) {
			setName = setName.replace("Sneak Peek participation cards", "");
			setName = setName.trim();
		}

		if (setName.contains(": Special Edition")) {
			setName = setName.replace(": Special Edition", "");
			setName = setName.trim();
		}

		if (setName.contains("Special Edition")) {
			setName = setName.replace("Special Edition", "");
			setName = setName.trim();
		}

		if (setName.contains(": Super Edition")) {
			setName = setName.replace(": Super Edition", "");
			setName = setName.trim();
		}

		if (setName.contains("Super Edition")) {
			setName = setName.replace("Super Edition", "");
			setName = setName.trim();
		}

		if (!setName.equals("Structure Deck: Deluxe Edition") && setName.contains(": Deluxe Edition")) {
			setName = setName.replace(": Deluxe Edition", "");
			setName = setName.trim();
		}

		if (!setName.equals("Structure Deck: Deluxe Edition") && setName.contains("Deluxe Edition")) {
			setName = setName.replace("Deluxe Edition", "");
			setName = setName.trim();
		}

		if (setName.contains("Premiere! promotional card")) {
			setName = setName.replace("Premiere! promotional card", "");
			setName = setName.trim();
		}

		if (setName.contains("Launch Event participation card")) {
			setName = setName.replace("Launch Event participation card", "");
			setName = setName.trim();
		}

		if (setName.endsWith(" SE")) {
			setName = setName.substring(0, setName.length() - 3);
			setName = setName.trim();
		}

		setName = flipStructureEnding(setName, "Starter Deck");
		setName = flipStructureEnding(setName, "Structure Deck");

		return getSetNameMapInstance().getValue(setName);
	}

	public static String checkForTranslatedRarity(String rarity) {
		KeyUpdateMap instance = getRarityMapInstance();

		return Util.standardizeSingleQuotes(instance.getValue(rarity));
	}

	public static String checkForTranslatedSetNumber(String setNumber) {
		KeyUpdateMap instance = getSetNumberMapInstance();

		return instance.getValue(setNumber);
	}

	public static String checkForTranslatedEdition(String setNumber, String editionPrinting) {
		KeyUpdateMap instance = getEditionMapInstance();

		String newEdition = instance.getRawValue(setNumber);

		if (newEdition == null) {
			return editionPrinting;
		}

		return newEdition;
	}

	public static String checkForTranslatedCardName(String cardName) {

		if (cardName == null) {
			return null;
		}

		KeyUpdateMap instance = getCardNameMapInstance();

		return instance.getValue(cardName);
	}

	public static int checkForTranslatedPasscode(int passcode) {
		KeyUpdateMap instance = getPasscodeMapInstance();

		String newPasscode = instance.getValue(String.valueOf(passcode));

		try {
			return Integer.parseInt(newPasscode);
		} catch (Exception e) {
			YGOLogger.error("Issue with passcode map for value:" + passcode);
			YGOLogger.logException(e);
			return passcode;
		}
	}

	public static int checkForTranslatedYgoProImagePasscode(int passcode) {
		KeyUpdateMap instance = getYgoProImagePasscodeMapInstance();

		String newPasscode = instance.getValue(String.valueOf(passcode));

		try {
			return Integer.parseInt(newPasscode);
		} catch (Exception e) {
			YGOLogger.error("Issue with ygo pro image passcode map for value:" + passcode);
			YGOLogger.logException(e);
			return passcode;
		}
	}

	public static String getLowestPriceString(String input1, String input2) {

		if (input1 == null && input2 == null) {
			return Const.ZERO_PRICE_STRING;
		}

		BigDecimal price1 = input1 == null ? BigDecimal.valueOf(Integer.MAX_VALUE) : new BigDecimal(input1);
		BigDecimal price2 = input2 == null ? BigDecimal.valueOf(Integer.MAX_VALUE) : new BigDecimal(input2);

		if (BigDecimal.ZERO.equals(price1)) {
			return input2;
		}

		if (BigDecimal.ZERO.equals(price2)) {
			return input1;
		}

		if (price1.compareTo(price2) < 0) {
			return input1;
		} else {
			return input2;
		}
	}

	public static String getLowestPriceString(String input1, String input2, String input3) {
		if (input1 == null && input2 == null && input3 == null) {
			return Const.ZERO_PRICE_STRING;
		}

		BigDecimal price1 = input1 == null ? BigDecimal.valueOf(Integer.MAX_VALUE) : new BigDecimal(input1);
		BigDecimal price2 = input2 == null ? BigDecimal.valueOf(Integer.MAX_VALUE) : new BigDecimal(input2);
		BigDecimal price3 = input3 == null ? BigDecimal.valueOf(Integer.MAX_VALUE) : new BigDecimal(input3);

		if (BigDecimal.ZERO.equals(price1)) {
			return getLowestPriceString(input2, input3);
		}

		if (BigDecimal.ZERO.equals(price2)) {
			return getLowestPriceString(input1, input3);
		}

		if (BigDecimal.ZERO.equals(price3)) {
			return getLowestPriceString(input1, input2);
		}

		if (price1.compareTo(price2) < 0 && price1.compareTo(price3) < 0) {
			return input1;
		} else if (price2.compareTo(price3) < 0) {
			return input2;
		} else {
			return input3;
		}
	}


	public static String removeRarityStringsFromName(String name) {
		if (name == null) {
			return null;
		}

		StringBuilder builder = new StringBuilder(name);
		for (Rarity rarity : Rarity.values()) {
			String rarityWithParens = "(" + rarity.toString() + ")";
			int index = builder.indexOf(rarityWithParens);
			while (index != -1) {
				builder.delete(index, index + rarityWithParens.length());
				index = builder.indexOf(rarityWithParens);
			}
		}

		return builder.toString().trim();
	}

	public static CardSet createUnknownCardSet(String name, String setName, SQLiteConnection db) throws SQLException {
		YGOLogger.error("Unknown set number for card name and set: " + name + ":" + setName);
		CardSet setIdentified = new CardSet();
		setIdentified.setRarityUnsure(Const.RARITY_UNSURE_TRUE);
		setIdentified.setColorVariant(Const.DEFAULT_COLOR_VARIANT);
		setIdentified.setSetName(setName);
		setIdentified.setSetNumber(null);
		setIdentified.setSetPrefix(null);
		setIdentified.setGamePlayCardUUID(db.getGamePlayCardUUIDFromTitle(name));
		return setIdentified;
	}

	public static NameAndColor getNameAndColor(String name) {
		String colorVariant = Const.DEFAULT_COLOR_VARIANT;
		String[] colorVariants = {"(Red)", "(Blue)", "(Green)", "(Purple)", "(Bronze)", "(Silver)", "(Alternate Art)"};

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
					case "(Bronze)":
						colorVariant = "brz";
						break;
					case "(Silver)":
						colorVariant = "s";
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

	public static String extractColorFromUrl(String url) {
		String tester = url.replace("blue-eyes", "").replace("red-eyes", "").replace("eyes-of-blue", "");

		if(tester.endsWith("-version-4")){
			return "v4";
		}
		if(tester.endsWith("-version-3")){
			return "v3";
		}
		if(tester.endsWith("-version-2")){
			return "v2";
		}
		if (tester.endsWith("-red")) {
			return "r";
		}
		if (tester.endsWith("-blue")) {
			return "b";
		}
		if (tester.endsWith("-green")) {
			return "g";
		}
		if (tester.endsWith("-purple")) {
			return "p";
		}
		if (tester.endsWith("-bronze")) {
			return "brz";
		}
		if (tester.endsWith("-silver")) {
			return "s";
		}
		if (tester.endsWith("-chaos-neos-misprint")) {
			return "original";
		}
		if (tester.endsWith("-trial-of-hell")) {
			return "original";
		}
		if (tester.endsWith("-harpie-lady-original-artwork")) {
			return "original";
		}
		if (tester.endsWith("-dark-paladin-1st-edition-artwork")) {
			return "original";
		}
		if (tester.endsWith("-chaos-neos-ghost-rare-misprint")) {
			return "original";
		}
		if (tester.contains("-alternate-art")) {
			return "a";
		}
		return Const.DEFAULT_COLOR_VARIANT;
	}

	public static String millisToShortDHMS(long duration) {
		String res = "";
		long days = TimeUnit.MILLISECONDS.toDays(duration);
		long hours = TimeUnit.MILLISECONDS.toHours(duration) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(duration));
		long minutes = TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration));
		long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration));
		long millis = TimeUnit.MILLISECONDS.toMillis(duration) - TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(duration));

		if (days == 0) {
			res = String.format(Locale.ENGLISH, "%02d:%02d:%02d.%04d", hours, minutes, seconds, millis);
		} else {
			res = String.format(Locale.ENGLISH, "%dd %02d:%02d:%02d.%04d", days, hours, minutes, seconds, millis);
		}
		return res;
	}

	public static String getEstimatePriceFromRarity(String rarity) {
		String trimmed = rarity.trim();

		if (trimmed.equalsIgnoreCase(Rarity.COMMON.toString())) {
			return "0.15";
		}

		if (trimmed.equalsIgnoreCase(Rarity.RARE.toString())) {
			return "0.15";
		}

		if (trimmed.equalsIgnoreCase(Rarity.SUPER_RARE.toString())) {
			return "0.25";
		}

		return Const.ZERO_PRICE_STRING;
	}

	public static String getAPIPriceFromRarity(OwnedCard card, SQLiteConnection db) {

		try {
			if (card.getAnalyzeResultsCardSets() == null) {
				card.setAnalyzeResultsCardSets(db.getRaritiesOfCardInSetByGamePlayCardUUID(card.getGamePlayCardUUID(), card.getSetName()));
			}
		} catch (Exception e) {
			YGOLogger.error("Error getting getRaritiesOfCardInSetByGamePlayCardUUID");
			YGOLogger.logException(e);
			return Const.ZERO_PRICE_STRING;
		}
		List<CardSet> analyzeResultsCardSets = card.getAnalyzeResultsCardSets();

		if (analyzeResultsCardSets.size() == 1) {
			return analyzeResultsCardSets.get(0).getBestExistingPrice(card.getEditionPrinting());
		}

		for (CardSet cardSet : analyzeResultsCardSets) {
			if (cardSet.getSetRarity().equalsIgnoreCase(card.getSetRarity()) &&
					cardSet.getSetNumber().equalsIgnoreCase(card.getSetNumber()) &&
					cardSet.getSetName().equalsIgnoreCase(card.getSetName()) &&
					cardSet.getColorVariant().equalsIgnoreCase(card.getColorVariant())) {
				return cardSet.getBestExistingPrice(card.getEditionPrinting());
			}
		}

		return getEstimatePriceFromRarity(card.getSetRarity());
	}

	public static String identifyEditionPrinting(String input) {

		if (input == null) {
			return Const.CARD_PRINTING_UNLIMITED;
		}

		if (input.contains(Const.CARD_PRINTING_CONTAINS_FIRST)) {
			return Const.CARD_PRINTING_FIRST_EDITION;
		} else if (input.contains(Const.CARD_PRINTING_UNLIMITED)) {
			return Const.CARD_PRINTING_UNLIMITED;
		}
		return Const.CARD_PRINTING_LIMITED;
	}

	private static final Set<Character> validQuoteChars = new HashSet<>();

	static {
		validQuoteChars.add('"');
		validQuoteChars.add('“');
		validQuoteChars.add('”');
		validQuoteChars.add('ʺ');
		validQuoteChars.add('„');
		validQuoteChars.add('‟');
		validQuoteChars.add('＂');
		validQuoteChars.add('⹂');
		validQuoteChars.add('❝');
		validQuoteChars.add('❞');
	}

	public static String removeSurroundingQuotes(String input) {
		if(input.contains("(alternate art)")){
			input = input.replace("(alternate art)","").trim();
		}

		if (input.length() >= 2) {
			char startChar = input.charAt(0);
			char endChar = input.charAt(input.length() - 1);

			if (validQuoteChars.contains(startChar) && validQuoteChars.contains(endChar)) {
				return input.substring(1, input.length() - 1).trim();
			}
		}

		return input;
	}

	public static String standardizeSingleQuotes(String input){
		// Define a regex pattern for different single quote characters
		String singleQuotePattern = "[‘’'`]";

		// Replace all variants of single quote characters with the standard single quote
		return input.replaceAll(singleQuotePattern, "'");
	}
}
