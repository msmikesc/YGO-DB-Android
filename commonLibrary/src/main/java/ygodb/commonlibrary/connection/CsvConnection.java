package ygodb.commonlibrary.connection;

import javafx.util.Pair;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import ygodb.commonlibrary.bean.CardSet;
import ygodb.commonlibrary.bean.GamePlayCard;
import ygodb.commonlibrary.bean.NameAndColor;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.bean.Rarity;
import ygodb.commonlibrary.bean.ReadCSVRecord;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.Util;
import ygodb.commonlibrary.utility.YGOLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class CsvConnection {

	private static final CSVFormat format = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build();

	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

	public CSVParser getParser(String filename, Charset charset) throws IOException {
		File f = new File(filename);

		InputStream fileStream = new FileInputStream(f);

		return getParser(fileStream, charset);
	}

	public CSVParser getParser(InputStream input, Charset charset) throws IOException {

		BufferedReader fr = new BufferedReader(new InputStreamReader(input, charset));

		skipByteOrderMark(fr);

		return format.parse(fr);
	}

	private void skipByteOrderMark(Reader reader) throws IOException {
		reader.mark(1);
		char[] possibleBOM = new char[1];
		int amountRead = reader.read(possibleBOM);

		if (amountRead != 1 || possibleBOM[0] != '\ufeff') {
			reader.reset();
		}
	}

	public CSVParser getParserSkipFirstLine(String filename, Charset charset) throws IOException {
		File f = new File(filename);

		InputStream fileStream = new FileInputStream(f);

		return getParserSkipFirstLine(fileStream, charset);
	}

	public CSVParser getParserSkipFirstLine(InputStream input, Charset charset) throws IOException {

		BufferedReader fr = new BufferedReader(new InputStreamReader(input, charset));

		fr.readLine();

		return format.parse(fr);
	}

	public CSVPrinter getExportOutputFile(String filename) {

		try {
			Writer fw = new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_16LE);
			CSVPrinter p = new CSVPrinter(fw, CSVFormat.DEFAULT);

			p.printRecord(Const.FOLDER_NAME_CSV, Const.QUANTITY_CSV, Const.CARD_NAME_CSV, Const.SET_CODE_CSV, Const.SET_NAME_CSV,
						  Const.CARD_NUMBER_CSV, Const.CONDITION_CSV, Const.PRINTING_CSV, Const.PRICE_BOUGHT_CSV, Const.DATE_BOUGHT_CSV,
						  Const.RARITY_CSV, Const.RARITY_COLOR_VARIANT_CSV, Const.RARITY_UNSURE_CSV, Const.GAME_PLAY_CARD_UUID_CSV,
						  Const.UUID_CSV, Const.PASSCODE_CSV);

			return p;

		} catch (IOException e) {
			YGOLogger.logException(e);
			throw new UncheckedIOException(e);
		}

	}

	public CSVPrinter getTCGPlayerOutputFile(String filename) {

		try {
			Writer fw = new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_16LE);
			CSVPrinter p = new CSVPrinter(fw, CSVFormat.DEFAULT);

			p.printRecord(Const.TCGPLAYER_ITEMS_CSV, Const.TCGPLAYER_DETAILS_CSV, Const.TCGPLAYER_PRICE_CSV, Const.TCGPLAYER_QUANTITY_CSV,
						  Const.TCGPLAYER_IMPORT_TIME);

			return p;

		} catch (IOException e) {
			YGOLogger.logException(e);
			throw new UncheckedIOException(e);
		}

	}

	public CSVPrinter getExportUploadFile(String filename) {

		try {
			Writer fw = new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_16LE);
			CSVPrinter p = new CSVPrinter(fw, CSVFormat.DEFAULT);

			p.printRecord(Const.FOLDER_NAME_CSV, Const.QUANTITY_CSV, Const.TRADE_QUANTITY_CSV, Const.CARD_NAME_CSV, Const.SET_CODE_CSV,
						  Const.SET_NAME_CSV, Const.CARD_NUMBER_CSV, Const.CONDITION_CSV, Const.PRINTING_CSV, Const.LANGUAGE_CSV,
						  Const.PRICE_BOUGHT_CSV, Const.DATE_BOUGHT_CSV, Const.LOW_CSV, Const.MID_CSV, Const.MARKET_CSV);

			return p;

		} catch (IOException e) {
			YGOLogger.logException(e);
			throw new UncheckedIOException(e);
		}
	}

	public CSVPrinter getWikiOutputFile(String filename) {

		try {
			Writer fw = new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_16LE);
			CSVPrinter p = new CSVPrinter(fw, CSVFormat.DEFAULT);

			p.printRecord(Const.CARD_NUMBER_CSV, Const.CARD_NAME_CSV, Const.RARITY_CSV, Const.CATEGORY_CSV, Const.SET_NAME_CSV);

			return p;

		} catch (IOException e) {
			YGOLogger.logException(e);
			throw new UncheckedIOException(e);
		}

	}

	public void writeUploadCardToCSV(CSVPrinter p, OwnedCard current) throws IOException {
		String printing = current.getEditionPrinting();

		if (printing.equals(Const.CARD_PRINTING_FIRST_EDITION)) {
			printing = Const.CARD_PRINTING_FOIL;
		}

		String outputSetNumber = current.getSetNumber();

		if (!current.getColorVariant().equalsIgnoreCase(Const.DEFAULT_COLOR_VARIANT) &&
				!Const.setColorVariantUnsupportedDragonShield.contains(current.getSetName())) {

			String colorModifier = current.getColorVariant();

			if(colorModifier.equals("s")){
				colorModifier = "S";
			}

			outputSetNumber += colorModifier;
		}

		p.printRecord(current.getFolderName(), current.getQuantity(), 0, current.getCardName(), current.getSetPrefix(),
					  current.getSetName(), outputSetNumber, current.getCondition(), printing, "English", current.getPriceBought(),
					  current.getDateBought(), 0, 0, 0);

	}

	public CSVPrinter getAnalyzeOutputFile(String filename) {

		try {
			Writer fw = new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_16LE);
			CSVPrinter p = new CSVPrinter(fw, CSVFormat.DEFAULT);

			p.printRecord(Const.QUANTITY_CSV, Const.CARD_NAME_CSV, Const.CARD_TYPE_CSV, Const.RARITIES_CSV, Const.SET_NAME_CSV,
						  Const.SET_CODE_CSV, Const.TCGPLAYER_MASS_BUY_3_CSV, Const.TCGPLAYER_MASS_BUY_1_CSV);

			return p;

		} catch (IOException e) {
			YGOLogger.logException(e);
			throw new UncheckedIOException(e);
		}

	}

	public CSVPrinter getSellFile(String filename) {

		try {
			Writer fw = new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_16LE);
			CSVPrinter p = new CSVPrinter(fw, CSVFormat.DEFAULT);

			p.printRecord(Const.QUANTITY_CSV, Const.CARD_NAME_CSV, Const.RARITY_CSV, Const.SET_NAME_CSV, Const.SET_CODE_CSV,
						  Const.PRICE_BOUGHT_CSV, "Api price current");

			return p;

		} catch (IOException e) {
			YGOLogger.logException(e);
			throw new UncheckedIOException(e);
		}

	}

	public CSVPrinter getAnalyzePrintedOnceOutputFile(String filename) {

		try {
			Writer fw = new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_16LE);
			CSVPrinter p = new CSVPrinter(fw, CSVFormat.DEFAULT);

			p.printRecord(Const.GAME_PLAY_CARD_UUID_CSV, Const.CARD_NAME_CSV, Const.CARD_TYPE_CSV, Const.RARITIES_CSV, Const.SET_NAMES_CSV,
						  Const.SET_CODES_CSV, Const.RELEASE_DATE_CSV, Const.ARCHETYPE_CSV);

			return p;

		} catch (IOException e) {
			YGOLogger.logException(e);
			throw new UncheckedIOException(e);
		}

	}

	public OwnedCard getOwnedCardFromExportedCSV(CSVRecord current, SQLiteConnection db) throws SQLException {

		String folder = getStringOrNull(current, Const.FOLDER_NAME_CSV);
		String name = getStringOrNull(current, Const.CARD_NAME_CSV);
		String quantity = getStringOrNull(current, Const.QUANTITY_CSV);
		String setPrefix = getStringOrNull(current, Const.SET_CODE_CSV);
		String setNumber = getStringOrNull(current, Const.CARD_NUMBER_CSV);
		String setName = getStringOrNull(current, Const.SET_NAME_CSV);
		String condition = getStringOrNull(current, Const.CONDITION_CSV);
		String printing = getStringOrNull(current, Const.PRINTING_CSV);
		String priceBought = Util.normalizePrice(getStringOrNull(current, Const.PRICE_BOUGHT_CSV));
		String dateBought = getStringOrNull(current, Const.DATE_BOUGHT_CSV);
		String rarity = getStringOrNull(current, Const.RARITY_CSV);
		String rarityColorVariant = getStringOrNull(current, Const.RARITY_COLOR_VARIANT_CSV);
		int rarityUnsure = getIntOrNegativeOne(current, Const.RARITY_UNSURE_CSV);
		String gamePlayCardUUID = getStringOrNull(current, Const.GAME_PLAY_CARD_UUID_CSV);
		int passcode = getIntOrNegativeOne(current, Const.PASSCODE_CSV);

		String uuid = getStringOrNull(current, Const.UUID_CSV);

		name = Util.checkForTranslatedCardName(name);
		rarity = Util.checkForTranslatedRarity(rarity);
		passcode = Util.checkForTranslatedPasscode(passcode);
		setName = Util.checkForTranslatedSetName(setName);
		setNumber = Util.checkForTranslatedSetNumber(setNumber);

		List<String> translatedList = Util.checkForTranslatedQuadKey(name, setNumber, rarity, setName);
		name = translatedList.get(0);
		setNumber = translatedList.get(1);
		rarity = translatedList.get(2);
		setName = translatedList.get(3);

		if ((Const.CARD_PRINTING_FOIL).equals(printing)) {
			printing = Const.CARD_PRINTING_FIRST_EDITION;
		}

		if (gamePlayCardUUID == null) {
			gamePlayCardUUID = db.getGamePlayCardUUIDFromTitle(name);
		}

		if (passcode == -1) {

			GamePlayCard gpc = db.getGamePlayCardByUUID(gamePlayCardUUID);

			if (gpc == null) {
				YGOLogger.error("Unknown gamePlayCard for " + name);
			} else {
				passcode = gpc.getPasscode();
			}
		}

		CardSet setIdentified = new CardSet();
		setIdentified.setRarityUnsure(rarityUnsure);
		setIdentified.setColorVariant(rarityColorVariant);
		setIdentified.setSetRarity(rarity);
		setIdentified.setSetName(setName);
		setIdentified.setSetNumber(setNumber);
		setIdentified.setGamePlayCardUUID(gamePlayCardUUID);
		setIdentified.setSetPrefix(setPrefix);

		OwnedCard card = new OwnedCard(folder, name, quantity, condition, printing, priceBought, dateBought, setIdentified, passcode);

		card.setUuid(uuid);

		return card;
	}

	public OwnedCard getOwnedCardFromTCGPlayerCSV(CSVRecord current, SQLiteConnection db) throws SQLException {
		String folder = Const.FOLDER_UNSYNCED;

		String items = getStringOrNull(current, Const.TCGPLAYER_ITEMS_CSV);
		String details = getStringOrNull(current, Const.TCGPLAYER_DETAILS_CSV);
		String price = getStringOrNull(current, Const.TCGPLAYER_PRICE_CSV);
		String quantity = getStringOrNull(current, Const.TCGPLAYER_QUANTITY_CSV);
		String importTime = getStringOrNull(current, Const.TCGPLAYER_IMPORT_TIME);

		if (items == null || details == null || price == null || quantity == null || importTime != null) {
			return null;
		}

		String[] nameAndSet = items.split("\n");

		if (nameAndSet.length < 2 || nameAndSet.length > 3) {
			YGOLogger.error("Unknown format: " + items);
			return null;
		}

		String name = nameAndSet[0].trim();
		String setName = nameAndSet[1].trim();

		name = name.replace("(Duel Terminal)", "").trim();
		name = name.replace("(UR)", "").trim();
		name = name.replace("(PUR)", "").trim();
		name = name.replace("(UTR)", "").trim();
		name = name.replace("(PCR)", "").trim();
		name = Util.removeRarityStringsFromName(name);

		NameAndColor nameAndColor = Util.getNameAndColor(name);
		name = nameAndColor.name;
		String colorVariant = nameAndColor.colorVariant;

		String[] rarityConditionPrinting = details.split("\n");
		if (rarityConditionPrinting.length != 2) {
			YGOLogger.error("Unknown format: " + details);
			return null;
		}
		String rarity = rarityConditionPrinting[0].replace("Rarity:", "").trim();
		String printing = Util.identifyEditionPrinting(rarityConditionPrinting[1]);
		String condition = getCondition(rarityConditionPrinting[1]);

		name = Util.checkForTranslatedCardName(name);
		rarity = Util.checkForTranslatedRarity(rarity);
		setName = Util.checkForTranslatedSetName(setName);
		price = price.replace("$", "");
		String priceBought = Util.normalizePrice(price);
		String dateBought = dateFormat.format(new Date());

		CardSet setIdentified = getCardSetMatchingDetails(db, name, setName, colorVariant, rarity);
		int passcode = getPasscodeOrNegativeOne(db, name, setIdentified.getGamePlayCardUUID());

		return new OwnedCard(folder, name, quantity, condition, printing, priceBought, dateBought, setIdentified, passcode);
	}

	public String getCondition(String input) {
		return input.replace(Const.CARD_PRINTING_UNLIMITED, "").replace(Const.CARD_PRINTING_LIMITED, "")
				.replace(Const.CARD_PRINTING_FIRST_EDITION, "").replace("Condition:", "").replaceAll("\\s", "")
				.replace("LightlyPlayed", "LightPlayed").replace("ModeratelyPlayed", "Played").replace("HeavilyPlayed", "Poor")
				.replace("Damaged", "Poor");
	}

	public int getPasscodeOrNegativeOne(SQLiteConnection db, String name, String uuid) throws SQLException {
		GamePlayCard gpc = db.getGamePlayCardByUUID(uuid);
		if (gpc != null) {
			return gpc.getPasscode();
		}

		YGOLogger.error("Unknown gamePlayCard for " + name);
		return -1;
	}

	public CardSet getCardSetMatchingDetails(SQLiteConnection db, String name, String setName, String colorVariant, String rarity)
			throws SQLException {
		CardSet setIdentified = db.getFirstCardSetForCardInSet(name, setName);
		if (setIdentified == null) {
			setIdentified = Util.createUnknownCardSet(name, setName, db);
		}
		setIdentified.setSetRarity(rarity);
		setIdentified.setColorVariant(colorVariant);
		return setIdentified;
	}

	public Integer getIntOrNegativeOne(CSVRecord current, String recordName) {
		try {
			return Integer.parseInt(current.get(recordName));
		} catch (Exception e) {
			return -1;
		}
	}

	public String getStringOrNull(CSVRecord current, String recordName) {
		try {
			String returnVal = current.get(recordName);

			if (returnVal == null || returnVal.isBlank()) {
				return null;
			}

			return returnVal.trim();
		} catch (Exception e) {
			return null;
		}
	}

	public void insertGamePlayCardFromCSV(CSVRecord current, SQLiteConnection db) throws SQLException {
		String name = getStringOrNull(current, Const.CARD_NAME_CSV);
		String type = getStringOrNull(current, Const.CARD_TYPE_CSV);
		Integer passcode = getIntOrNegativeOne(current, Const.PASSCODE_CSV);
		String lore = getStringOrNull(current, Const.CARD_TEXT_CSV);
		String attribute = getStringOrNull(current, Const.ATTRIBUTE_CSV);
		String race = getStringOrNull(current, Const.RACE_CSV);
		String linkValue = getStringOrNull(current, Const.LINK_VALUE_CSV);
		String pendScale = getStringOrNull(current, Const.PENDULUM_SCALE_CSV);
		String level = getStringOrNull(current, Const.LEVEL_RANK_CSV);
		String atk = getStringOrNull(current, Const.ATTACK_CSV);
		String def = getStringOrNull(current, Const.DEFENSE_CSV);
		String archetype = getStringOrNull(current, Const.ARCHETYPE_CSV);

		GamePlayCard gamePlayCard = new GamePlayCard();

		name = Util.removeSurroundingQuotes(name);
		name = Util.checkForTranslatedCardName(name);
		passcode = Util.checkForTranslatedPasscode(passcode);

		if (passcode == -1) {
			passcode = db.getNewLowestPasscode();
		}

		gamePlayCard.setCardName(name);
		gamePlayCard.setCardType(type);
		gamePlayCard.setArchetype(archetype);
		gamePlayCard.setPasscode(passcode);

		gamePlayCard.setGamePlayCardUUID(db.getGamePlayCardUUIDFromPasscode(passcode));

		if (gamePlayCard.getGamePlayCardUUID() == null) {
			Pair<String, String> uuidAndName = Util.getGamePlayCardUUIDFromTitleOrGenerateNewWithSkillCheck(name, db);
			gamePlayCard.setGamePlayCardUUID(uuidAndName.getKey());
			gamePlayCard.setCardName(uuidAndName.getValue());
		}

		gamePlayCard.setDesc(lore);
		gamePlayCard.setAttribute(attribute);
		gamePlayCard.setRace(race);
		gamePlayCard.setLinkVal(linkValue);
		gamePlayCard.setScale(pendScale);
		gamePlayCard.setLevel(level);
		gamePlayCard.setAtk(atk);
		gamePlayCard.setDef(def);

		db.replaceIntoGamePlayCard(gamePlayCard);
	}

	public void insertCardSetFromCSV(CSVRecord current, SQLiteConnection db) throws SQLException {

		String name = getStringOrNull(current, Const.CARD_NAME_CSV);
		String cardNumber = getCardNumberFromCSVRecord(current);
		String rawRarityInput = getStringOrNull(current, Const.RARITY_CSV);
		String setName = getSetNameFromCSVRecord(current);

		name = Util.removeSurroundingQuotes(name);
		name = Util.checkForTranslatedCardName(name);
		cardNumber = Util.removeSurroundingQuotes(cardNumber);
		rawRarityInput = Util.removeSurroundingQuotes(rawRarityInput);
		List<String> rarityInputList = List.of(rawRarityInput.split("\\r?\\n"));
		setName = Util.removeSurroundingQuotes(setName);

		ArrayList<String> confirmedRarites = new ArrayList<>();

		for(String inputRarity: rarityInputList){
			String currentRarity = Util.checkForTranslatedRarity(inputRarity.trim());
			Rarity rarityObject = Rarity.fromString(currentRarity);

			if(rarityObject.equals(Rarity.NULL_RARITY)){
				YGOLogger.error("Rarity unable to be read:" + currentRarity);
			}
			else{
				confirmedRarites.add(rarityObject.toString());
			}
		}

		Pair<String, String> uuidAndName = Util.getGamePlayCardUUIDFromTitleOrNullWithSkillCheck(name, db);

		String gamePlayCardUUID = uuidAndName.getKey();
		name = uuidAndName.getValue();

		if (gamePlayCardUUID == null) {
			gamePlayCardUUID = UUID.randomUUID().toString();

			GamePlayCard newGPC = new GamePlayCard();

			newGPC.setCardName(name);
			newGPC.setGamePlayCardUUID(gamePlayCardUUID);
			newGPC.setArchetype(Const.ARCHETYPE_AUTOGENERATE);
			newGPC.setPasscode(db.getNewLowestPasscode());
			db.replaceIntoGamePlayCard(newGPC);

		}

		for(String rarity: confirmedRarites) {
			db.insertOrIgnoreIntoCardSet(cardNumber, rarity, setName, gamePlayCardUUID, name, null, null);
		}
	}

	public String getCardNumberFromCSVRecord(CSVRecord current) {
		String cardNumber = getStringOrNull(current, Const.CARD_NUMBER_CSV);
		cardNumber = Util.checkForTranslatedSetNumber(cardNumber);
		return cardNumber;
	}

	public String getSetNameFromCSVRecord(CSVRecord current) {
		String setName = getStringOrNull(current, Const.SET_NAME_CSV);
		setName = Util.checkForTranslatedSetName(setName);
		return setName;
	}

	public OwnedCard getStaticSetOwnedCardFromCSV(CSVRecord current, SQLiteConnection db, int quantityMultiplier) throws SQLException {
		String name = getStringOrNull(current, Const.CARD_NAME_CSV);
		String cardNumber = getStringOrNull(current, Const.CARD_NUMBER_CSV);
		String rarity = getStringOrNull(current, Const.RARITY_CSV);
		int quantity = getIntOrNegativeOne(current, Const.QUANTITY_CSV);
		String setName = getStringOrNull(current, Const.SET_NAME_CSV);
		String rarityColorVariant = getStringOrNull(current, Const.RARITY_COLOR_VARIANT_CSV);
		if(rarityColorVariant == null || rarityColorVariant.isBlank()){
			rarityColorVariant = Const.DEFAULT_COLOR_VARIANT;
		}
		String printing = getStringOrNull(current, Const.PRINTING_CSV);

		if(printing == null || printing.isBlank()){
			YGOLogger.error("printing not found for:" + name);
			return null;
		}

		if (quantity < 1) {
			YGOLogger.error("quantity not found for:" + name);
			return null;
		}
		quantity = quantity * quantityMultiplier;

		name = Util.removeSurroundingQuotes(name);

		name = Util.checkForTranslatedCardName(name);
		rarity = Util.checkForTranslatedRarity(rarity);
		cardNumber = Util.checkForTranslatedSetNumber(cardNumber);
		setName = Util.checkForTranslatedSetName(setName);

		List<String> translatedList = Util.checkForTranslatedQuadKey(name, cardNumber, rarity, setName);
		name = translatedList.get(0);
		cardNumber = translatedList.get(1);
		rarity = translatedList.get(2);
		setName = translatedList.get(3);

		Pair<String, String> uuidAndName = Util.getGamePlayCardUUIDFromTitleOrNullWithSkillCheck(name, db);

		String gamePlayCardUUID = uuidAndName.getKey();

		if (gamePlayCardUUID == null) {
			YGOLogger.error("gamePlayCardUUID not found for:" + name);
			return null;
		}

		CardSet cardSet = db.getRarityOfExactCardInSet(
				gamePlayCardUUID, cardNumber, rarity, rarityColorVariant, setName);

		if (cardSet == null) {
			YGOLogger.error("CardSet not found for:" + name);
			return null;
		}
		name = cardSet.getCardName();

		String dateBought = dateFormat.format(new Date());
		int passcode = getPasscodeOrNegativeOne(db, name, gamePlayCardUUID);

		return new OwnedCard(Const.FOLDER_UNSYNCED, name, String.valueOf(quantity), "NearMint",
							 printing, cardSet.getBestExistingPrice(printing),
							 dateBought, cardSet, passcode);
	}

	public void writeOwnedCardToCSV(CSVPrinter p, OwnedCard current) throws IOException {
		p.printRecord(current.getFolderName(), current.getQuantity(), current.getCardName(), current.getSetPrefix(), current.getSetName(),
					  current.getSetNumber(), current.getCondition(), current.getEditionPrinting(), current.getPriceBought(),
					  current.getDateBought(), current.getSetRarity(), current.getColorVariant(), current.getRarityUnsure(),
					  current.getGamePlayCardUUID(), current.getUuid(), current.getPasscode());

	}

	public void writeTCGPlayerRecordToCSV(CSVPrinter p, ReadCSVRecord current) throws IOException {
		p.printRecord(getStringOrNull(current.getCsvRecord(), Const.TCGPLAYER_ITEMS_CSV),
					  getStringOrNull(current.getCsvRecord(), Const.TCGPLAYER_DETAILS_CSV),
					  getStringOrNull(current.getCsvRecord(), Const.TCGPLAYER_PRICE_CSV),
					  getStringOrNull(current.getCsvRecord(), Const.TCGPLAYER_QUANTITY_CSV), current.getReadTime());

	}

	public void writeWikiCardToCSV(CSVPrinter p, Map<String,String> rowValues) throws IOException {
		p.printRecord(rowValues.get(Const.CARD_NUMBER_CSV), rowValues.get(Const.CARD_NAME_CSV),
					  rowValues.get(Const.RARITY_CSV), rowValues.get(Const.CATEGORY_CSV),
					  rowValues.get(Const.SET_NAME_CSV));
	}

}
