package ygodb.commonlibrary.connection;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import javafx.util.Pair;
import ygodb.commonlibrary.bean.CardSet;
import ygodb.commonlibrary.bean.GamePlayCard;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.bean.ReadCSVRecord;
import ygodb.commonlibrary.bean.SetMetaData;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.Util;
import ygodb.commonlibrary.utility.YGOLogger;

public class CsvConnection {

	private CsvConnection(){}

	private static final CSVFormat format = CSVFormat.DEFAULT.builder()
			.setHeader()
			.setSkipHeaderRecord(true)
			.build();

	public static CSVParser getParser(String filename, Charset charset) throws IOException {
		File f = new File(filename);

		InputStream fileStream = new FileInputStream(f);

		return getParser(fileStream, charset);
	}

	public static CSVParser getParser(InputStream input, Charset charset) throws IOException {

		BufferedReader fr = new BufferedReader(new InputStreamReader(input, charset));

		skipByteOrderMark(fr);

		return format.parse(fr);
	}

	private static void skipByteOrderMark(Reader reader) throws IOException {
		reader.mark(1);
		char[] possibleBOM = new char[1];
		int amountRead = reader.read(possibleBOM);

		if (amountRead != 1 || possibleBOM[0] != '\ufeff') {
			reader.reset();
		}
	}

	public static CSVParser getParserSkipFirstLine(String filename, Charset charset) throws IOException {
		File f = new File(filename);

		InputStream fileStream = new FileInputStream(f);

		return getParserSkipFirstLine(fileStream, charset);
	}

	public static CSVParser getParserSkipFirstLine(InputStream input, Charset charset) throws IOException {

		BufferedReader fr = new BufferedReader(new InputStreamReader(input, charset));

		fr.readLine();

		return format.parse(fr);
	}

	public static CSVPrinter getExportOutputFile(String filename) {

		try {
			Writer fw = new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_16LE);
			CSVPrinter p = new CSVPrinter(fw, CSVFormat.DEFAULT);

			p.printRecord(Const.FOLDER_NAME_CSV, Const.QUANTITY_CSV, Const.CARD_NAME_CSV, Const.SET_CODE_CSV, Const.SET_NAME_CSV,
					Const.CARD_NUMBER_CSV, Const.CONDITION_CSV,
					Const.PRINTING_CSV, Const.PRICE_BOUGHT_CSV, Const.DATE_BOUGHT_CSV, Const.RARITY_CSV,
					Const.RARITY_COLOR_VARIANT_CSV, Const.RARITY_UNSURE_CSV,
					Const.GAME_PLAY_CARD_UUID_CSV, Const.UUID_CSV, Const.PASSCODE_CSV);

			return p;

		} catch (IOException e) {
			YGOLogger.logException(e);
			throw new UncheckedIOException(e);
		}

	}

	public static CSVPrinter getTCGPlayerOutputFile(String filename) {

		try {
			Writer fw = new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_16LE);
			CSVPrinter p = new CSVPrinter(fw, CSVFormat.DEFAULT);

			p.printRecord(Const.TCGPLAYER_ITEMS_CSV, Const.TCGPLAYER_DETAILS_CSV, Const.TCGPLAYER_PRICE_CSV,
					Const.TCGPLAYER_QUANTITY_CSV, Const.TCGPLAYER_IMPORT_TIME);

			return p;

		} catch (IOException e) {
			YGOLogger.logException(e);
			throw new UncheckedIOException(e);
		}

	}
	
	public static CSVPrinter getExportUploadFile(String filename) {

		try {
			Writer fw = new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_16LE);
			CSVPrinter p = new CSVPrinter(fw, CSVFormat.DEFAULT);

			p.printRecord(Const.FOLDER_NAME_CSV, Const.QUANTITY_CSV, Const.TRADE_QUANTITY_CSV,
					Const.CARD_NAME_CSV, Const.SET_CODE_CSV, Const.SET_NAME_CSV,
					Const.CARD_NUMBER_CSV, Const.CONDITION_CSV, Const.PRINTING_CSV,
					Const.LANGUAGE_CSV, Const.PRICE_BOUGHT_CSV, Const.DATE_BOUGHT_CSV,
					Const.LOW_CSV, Const.MID_CSV, Const.MARKET_CSV);

			return p;

		} catch (IOException e) {
			YGOLogger.logException(e);
			throw new UncheckedIOException(e);
		}

	}
	
	public static CSVPrinter getAnalyzeOutputFile(String filename) {

		try {
			Writer fw = new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_16LE);
			CSVPrinter p = new CSVPrinter(fw, CSVFormat.DEFAULT);

			p.printRecord(Const.QUANTITY_CSV, Const.CARD_NAME_CSV, Const.CARD_TYPE_CSV, Const.RARITIES_CSV,
					Const.SET_NAME_CSV,Const.SET_CODE_CSV, Const.TCGPLAYER_MASS_BUY_3_CSV, Const.TCGPLAYER_MASS_BUY_1_CSV);

			return p;

		} catch (IOException e) {
			YGOLogger.logException(e);
			throw new UncheckedIOException(e);
		}

	}
	
	public static CSVPrinter getSellFile(String filename) {

		try {
			Writer fw = new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_16LE);
			CSVPrinter p = new CSVPrinter(fw, CSVFormat.DEFAULT);

			p.printRecord(Const.QUANTITY_CSV, Const.CARD_NAME_CSV, Const.RARITY_CSV, Const.SET_NAME_CSV, Const.SET_CODE_CSV, Const.PRICE_BOUGHT_CSV);

			return p;

		} catch (IOException e) {
			YGOLogger.logException(e);
			throw new UncheckedIOException(e);
		}

	}
	
	public static CSVPrinter getAnalyzePrintedOnceOutputFile(String filename) {

		try {
			Writer fw = new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_16LE);
			CSVPrinter p = new CSVPrinter(fw, CSVFormat.DEFAULT);

			p.printRecord(Const.GAME_PLAY_CARD_UUID_CSV, Const.CARD_NAME_CSV,Const.CARD_TYPE_CSV, Const.RARITIES_CSV,
					Const.SET_NAMES_CSV,Const.SET_CODES_CSV, Const.RELEASE_DATE_CSV, Const.ARCHETYPE_CSV);

			return p;

		} catch (IOException e) {
			YGOLogger.logException(e);
			throw new UncheckedIOException(e);
		}

	}

	public static OwnedCard getOwnedCardFromExportedCSV(CSVRecord current, SQLiteConnection db) throws SQLException {

		String folder = getStringOrNull(current,Const.FOLDER_NAME_CSV);
		String name = getStringOrNull(current,Const.CARD_NAME_CSV);
		String quantity = getStringOrNull(current,Const.QUANTITY_CSV);
		String setCode = getStringOrNull(current,Const.SET_CODE_CSV);
		String setNumber = getStringOrNull(current,Const.CARD_NUMBER_CSV);
		String setName = getStringOrNull(current,Const.SET_NAME_CSV);
		String condition = getStringOrNull(current,Const.CONDITION_CSV);
		String printing = getStringOrNull(current,Const.PRINTING_CSV);
		String priceBought = Util.normalizePrice(getStringOrNull(current,Const.PRICE_BOUGHT_CSV));
		String dateBought = getStringOrNull(current,Const.DATE_BOUGHT_CSV);
		String rarity = getStringOrNull(current,Const.RARITY_CSV);
		String rarityColorVariant = getStringOrNull(current,Const.RARITY_COLOR_VARIANT_CSV);
		int rarityUnsure = getIntOrNegativeOne(current,Const.RARITY_UNSURE_CSV);
		String gamePlayCardUUID = getStringOrNull(current, Const.GAME_PLAY_CARD_UUID_CSV);
		int passcode = getIntOrNegativeOne(current, Const.PASSCODE_CSV);
		
		String uuid = getStringOrNull(current,Const.UUID_CSV);

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

		if(gamePlayCardUUID == null) {
			gamePlayCardUUID = db.getGamePlayCardUUIDFromTitle(name);
		}

		if(passcode == -1) {

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
		setIdentified.setSetCode(setCode);
		
		OwnedCard card = Util.formOwnedCard(folder, name, quantity, condition, printing, priceBought,
				dateBought, setIdentified, passcode);
		
		card.setUuid(uuid);

		return card;
	}
	
	public static OwnedCard getOwnedCardFromTCGPlayerCSV(CSVRecord current, SQLiteConnection db) throws SQLException {

		String folder = Const.FOLDER_UNSYNCED;

		String items = getStringOrNull(current,Const.TCGPLAYER_ITEMS_CSV);
		String details = getStringOrNull(current,Const.TCGPLAYER_DETAILS_CSV);
		String price = getStringOrNull(current,Const.TCGPLAYER_PRICE_CSV);
		String quantity = getStringOrNull(current,Const.TCGPLAYER_QUANTITY_CSV);
		String importTime = getStringOrNull(current,Const.TCGPLAYER_IMPORT_TIME);

		if(items == null || details == null || price == null || quantity == null || importTime != null){
			return null;
		}

		price = price.replace("$", "");
		
		String colorVariant = Const.DEFAULT_COLOR_VARIANT;

		String[] nameAndSet = items.split("\n");

		// possible sold by line
		if (nameAndSet.length < 2 || nameAndSet.length > 3) {
			YGOLogger.error("Unknown format: " + items);
			return null;
		}

		String name = nameAndSet[0].trim();
		String setName = nameAndSet[1].trim();

		// remove tcgplayer rarity id
		if (name.contains("(Duel Terminal)")) {
			name = name.replace("(Duel Terminal)", "").trim();
		}

		if (name.contains("(Secret Rare)")) {
			name = name.replace("(Secret Rare)", "").trim();
		}
		//TODO generic rarity removal
		
		if (name.contains("(Red)")) {
			name = name.replace("(Red)", "").trim();
			colorVariant = "r";
		}
		
		if (name.contains("(Blue)")) {
			name = name.replace("(Blue)", "").trim();
			colorVariant = "b";
		}
		
		if (name.contains("(Green)")) {
			name = name.replace("(Green)", "").trim();
			colorVariant = "g";
		}
		
		if (name.contains("(Purple)")) {
			name = name.replace("(Purple)", "").trim();
			colorVariant = "p";
		}
		
		if (name.contains("(Alternate Art)")) {
			name = name.replace("(Alternate Art)", "").trim();
			colorVariant = "a";
		}

		String[] rarityConditionPrinting = details.split("\n");

		if (rarityConditionPrinting.length != 2) {
			YGOLogger.error("Unknown format: " + details);
			return null;
		}

		String rarity = rarityConditionPrinting[0].replace("Rarity:", "").trim();

		name = Util.checkForTranslatedCardName(name);
		rarity = Util.checkForTranslatedRarity(rarity);
		setName = Util.checkForTranslatedSetName(setName);

		String printing = Const.CARD_PRINTING_LIMITED;

		if (rarityConditionPrinting[1].contains(Const.CARD_PRINTING_FIRST_EDITION)) {
			printing = Const.CARD_PRINTING_FIRST_EDITION;
		}
		if (rarityConditionPrinting[1].contains(Const.CARD_PRINTING_UNLIMITED)) {
			printing = Const.CARD_PRINTING_UNLIMITED;
		}

		String condition = rarityConditionPrinting[1].replace(Const.CARD_PRINTING_UNLIMITED, "")
				.replace(Const.CARD_PRINTING_LIMITED, "")
				.replace(Const.CARD_PRINTING_FIRST_EDITION, "").replace("Condition:", "")
				.replaceAll("\\s", "")
				.replace("LightlyPlayed", "LightPlayed")
				.replace("ModeratelyPlayed", "Played")
				.replace("HeavilyPlayed", "Poor").replace("Damaged", "Poor");

		CardSet setIdentified = db.getFirstCardSetForCardInSet(name, setName);

		if (setIdentified == null) {
			YGOLogger.error("Unknown setCode for card name and set: " + name + ":" + setName);
			setIdentified = new CardSet();
			setIdentified.setRarityUnsure(1);
			setIdentified.setColorVariant(Const.DEFAULT_COLOR_VARIANT);
			setIdentified.setSetName(setName);
			setIdentified.setSetNumber(null);
			setIdentified.setSetCode(null);
			setIdentified.setGamePlayCardUUID(db.getGamePlayCardUUIDFromTitle(name));
		}

		setIdentified.setSetRarity(rarity);
		setIdentified.setColorVariant(colorVariant);

		String priceBought = Util.normalizePrice(price);

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

		String dateBought = dateFormat.format(new Date());

		int passcode = -1;
		GamePlayCard gpc = db.getGamePlayCardByUUID(setIdentified.getGamePlayCardUUID());

		if(gpc == null){
			YGOLogger.error("Unknown gamePlayCard for " + name);
		}
		else{
			passcode = gpc.getPasscode();
		}

		return Util.formOwnedCard(folder, name, quantity, condition, printing, priceBought,
				dateBought, setIdentified, passcode);
	}

	public static Integer getIntOrNegativeOne(CSVRecord current, String recordName) {
		try {
			return Integer.parseInt(current.get(recordName));
		} catch (Exception e) {
			return -1;
		}
	}

	public static String getStringOrNull(CSVRecord current, String recordName) {
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

	public static void insertGamePlayCardFromCSV(CSVRecord current, SQLiteConnection db) throws SQLException {
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

		name = Util.checkForTranslatedCardName(name);
		passcode = Util.checkForTranslatedPasscode(passcode);

		if(passcode == -1){
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

	public static void insertCardSetFromCSV(CSVRecord current, String defaultSetName, SQLiteConnection db) throws SQLException {

		String name = getStringOrNull(current,Const.CARD_NAME_CSV);
		String cardNumber = getStringOrNull(current,Const.CARD_NUMBER_CSV);
		String rarity = getStringOrNull(current,Const.RARITY_CSV);

		String setName = null;

		try {
			setName = getStringOrNull(current,Const.SET_NAME_CSV);

			if(setName == null){
				setName = defaultSetName;
			}
		} catch (Exception e) {
			setName = defaultSetName;
		}

		name = Util.checkForTranslatedCardName(name);
		rarity = Util.checkForTranslatedRarity(rarity);
		setName = Util.checkForTranslatedSetName(setName);
		cardNumber = Util.checkForTranslatedSetNumber(cardNumber);

		Pair<String, String> uuidAndName = Util.getGamePlayCardUUIDFromTitleOrNullWithSkillCheck(name, db);

		String gamePlayCardUUID = uuidAndName.getKey();
		name = uuidAndName.getValue();

		if(gamePlayCardUUID == null){
			gamePlayCardUUID = UUID.randomUUID().toString();

			GamePlayCard newGPC = new GamePlayCard();

			newGPC.setCardName(name);
			newGPC.setGamePlayCardUUID(gamePlayCardUUID);
			newGPC.setArchetype(Const.ARCHETYPE_AUTOGENERATE);
			newGPC.setPasscode(db.getNewLowestPasscode());
			db.replaceIntoGamePlayCard(newGPC);

		}

		db.replaceIntoCardSetWithSoftPriceUpdate(cardNumber, rarity, setName, gamePlayCardUUID, null, name);
	}

	public static void writeOwnedCardToCSV(CSVPrinter p, OwnedCard current) throws IOException {
		p.printRecord(current.getFolderName(), current.getQuantity(), current.getCardName(), current.getSetCode(), current.getSetName(),
				current.getSetNumber(), current.getCondition(), current.getEditionPrinting(), current.getPriceBought(), current.getDateBought(),
				current.getSetRarity(), current.getColorVariant(), current.getRarityUnsure(), current.getGamePlayCardUUID(), current.getUuid(), current.getPasscode());

	}

	public static void writeTCGPlayerRecordToCSV(CSVPrinter p, ReadCSVRecord current) throws IOException {
		p.printRecord(getStringOrNull(current.getCsvRecord(), Const.TCGPLAYER_ITEMS_CSV),
				getStringOrNull(current.getCsvRecord(), Const.TCGPLAYER_DETAILS_CSV),
				getStringOrNull(current.getCsvRecord(), Const.TCGPLAYER_PRICE_CSV),
				getStringOrNull(current.getCsvRecord(), Const.TCGPLAYER_QUANTITY_CSV),
				current.getReadTime());

	}

}
