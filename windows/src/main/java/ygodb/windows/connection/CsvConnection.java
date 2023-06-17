package ygodb.windows.connection;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
import ygodb.commonlibrary.connection.DatabaseHashMap;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.Util;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.windows.utility.WindowsUtil;

public class CsvConnection {

	private CsvConnection(){}

	private static final CSVFormat format = CSVFormat.DEFAULT.builder()
			.setHeader()
			.setSkipHeaderRecord(true)
			.build();

	public static CSVParser getParser(String filename, Charset charset) throws IOException {
		File f = new File(filename);

		BufferedReader fr = new BufferedReader(new FileReader(f, charset));

		skipByteOrderMark(fr);

		return format.parse(fr);
	}

	public static CSVParser getParser(InputStream input, Charset charset) throws IOException {

		BufferedReader fr = new BufferedReader(new InputStreamReader(input, charset));

		skipByteOrderMark(fr);

		return format.parse(fr);
	}

	private static void skipByteOrderMark(Reader reader) throws IOException {
		reader.mark(1);
		char[] possibleBOM = new char[1];
		reader.read(possibleBOM);

		if (possibleBOM[0] != '\ufeff') {
			reader.reset();
		}
	}

	public static CSVParser getParserSkipFirstLine(String filename, Charset charset) throws IOException {
		File f = new File(filename);

		FileReader fr = new FileReader(f, charset);

		BufferedReader s = new BufferedReader(fr);

		s.readLine();

		return format.parse(s);
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

	public static OwnedCard getOwnedCardFromDragonShieldCSV(CSVRecord current, SQLiteConnection db) throws SQLException {

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
		
		String colorCode = Const.DEFAULT_COLOR_VARIANT;

		if (Const.CARD_PRINTING_FOIL.equals(printing)) {
			printing = Const.CARD_PRINTING_FIRST_EDITION;
		}

		name = WindowsUtil.checkForTranslatedCardName(name);
		setName = WindowsUtil.checkForTranslatedSetName(setName);
		setNumber = WindowsUtil.checkForTranslatedSetNumber(setNumber);

		List<OwnedCard> ownedRarities = DatabaseHashMap.getExistingOwnedRaritesForCardFromHashMap(setNumber,
				priceBought, dateBought, folder, condition, printing, db);

		if (ownedRarities.isEmpty()) {
			// try removing color code

			String newSetNumber = setNumber.substring(0, setNumber.length() - 1);
			String newColorCode = setNumber.substring(setNumber.length() - 1);

			ownedRarities = DatabaseHashMap.getExistingOwnedRaritesForCardFromHashMap(newSetNumber, priceBought,
					dateBought, folder, condition, printing, db);

			if (!ownedRarities.isEmpty()) {
				setNumber = newSetNumber;
				colorCode = newColorCode;
			}
		}

		for (OwnedCard existingCard : ownedRarities) {
			if (Util.doesCardExactlyMatch(folder, name, setCode, setNumber, condition, printing, priceBought,
					dateBought, existingCard)) {
				
				CardSet setIdentified = new CardSet();
				
				setIdentified.colorVariant = existingCard.colorVariant;
				setIdentified.setName = existingCard.setName;
				setIdentified.setNumber = existingCard.setNumber;
				setIdentified.gamePlayCardUUID = existingCard.gamePlayCardUUID;
				setIdentified.setRarity = existingCard.setRarity;
				setIdentified.rarityUnsure = existingCard.rarityUnsure;

				int passcode = -1;
				GamePlayCard gpc = db.getGamePlayCardByUUID(setIdentified.gamePlayCardUUID);

				if(gpc == null){
					YGOLogger.error("Unknown gamePlayCard for " + name);
				}
				else{
					passcode = gpc.passcode;
				}
				
				OwnedCard card = Util.formOwnedCard(folder, name, quantity, setCode, condition, printing, priceBought,
						dateBought, setIdentified, passcode);
				
				card.uuid = existingCard.uuid;
				
				return card;
			}
		}

		CardSet setIdentified = Util.findRarity(priceBought, setNumber,
				setName, name, db);
		
		setIdentified.colorVariant = colorCode;

		int passcode = -1;
		GamePlayCard gpc = db.getGamePlayCardByUUID(setIdentified.gamePlayCardUUID);

		if(gpc == null){
			YGOLogger.error("Unknown gamePlayCard for " + name);
		}
		else{
			passcode = gpc.passcode;
		}

		return Util.formOwnedCard(folder, name, quantity, setCode, condition, printing, priceBought,
				dateBought, setIdentified, passcode);
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

		name = WindowsUtil.checkForTranslatedCardName(name);
		rarity = WindowsUtil.checkForTranslatedRarity(rarity);
		passcode = WindowsUtil.checkForTranslatedPasscode(passcode);

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
				passcode = gpc.passcode;
			}
		}

		CardSet setIdentified = new CardSet();
		setIdentified.rarityUnsure = rarityUnsure;
		setIdentified.colorVariant = rarityColorVariant;
		setIdentified.setRarity = rarity;
		setIdentified.setName = setName;
		setIdentified.setNumber = setNumber;
		setIdentified.gamePlayCardUUID = gamePlayCardUUID;
		
		OwnedCard card = Util.formOwnedCard(folder, name, quantity, setCode, condition, printing, priceBought,
				dateBought, setIdentified, passcode);
		
		card.uuid = uuid;

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

		name = WindowsUtil.checkForTranslatedCardName(name);
		rarity = WindowsUtil.checkForTranslatedRarity(rarity);
		setName = WindowsUtil.checkForTranslatedSetName(setName);

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
			setIdentified.rarityUnsure = 1;
			setIdentified.colorVariant = Const.DEFAULT_COLOR_VARIANT;
			setIdentified.setName = setName;
			setIdentified.setNumber = null;
			setIdentified.gamePlayCardUUID = db.getGamePlayCardUUIDFromTitle(name);

		}

		setIdentified.setRarity = rarity;
		setIdentified.colorVariant = colorVariant;
		
		String setCode = null;

		ArrayList<SetMetaData> metaData = db.getSetMetaDataFromSetName(setName);

		if (metaData.size() != 1) {
			YGOLogger.error("Unknown metaData for set: " + setName);
		} else {
			setCode = metaData.get(0).setCode;
		}

		String priceBought = Util.normalizePrice(price);
		String dateBought = java.time.LocalDate.now().toString();

		int passcode = -1;
		GamePlayCard gpc = db.getGamePlayCardByUUID(setIdentified.gamePlayCardUUID);

		if(gpc == null){
			YGOLogger.error("Unknown gamePlayCard for " + name);
		}
		else{
			passcode = gpc.passcode;
		}

		return Util.formOwnedCard(folder, name, quantity, setCode, condition, printing, priceBought,
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

		name = WindowsUtil.checkForTranslatedCardName(name);
		passcode = WindowsUtil.checkForTranslatedPasscode(passcode);

		gamePlayCard.cardName = name;
		gamePlayCard.cardType = type;
		gamePlayCard.archetype = archetype;
		gamePlayCard.passcode = passcode;

		Pair<String, String> uuidAndName = Util.getGamePlayCardUUIDFromTitleOrGenerateNewWithSkillCheck(name, db);

		gamePlayCard.gamePlayCardUUID = uuidAndName.getKey();
		gamePlayCard.cardName = uuidAndName.getValue();

		gamePlayCard.desc = lore;
		gamePlayCard.attribute = attribute;
		gamePlayCard.race = race;
		gamePlayCard.linkval = linkValue;
		gamePlayCard.scale = pendScale;
		gamePlayCard.level = level;
		gamePlayCard.atk = atk;
		gamePlayCard.def = def;

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

		name = WindowsUtil.checkForTranslatedCardName(name);
		rarity = WindowsUtil.checkForTranslatedRarity(rarity);
		setName = WindowsUtil.checkForTranslatedSetName(setName);
		cardNumber = WindowsUtil.checkForTranslatedSetNumber(cardNumber);

		Pair<String, String> uuidAndName = Util.getGamePlayCardUUIDFromTitleOrNullWithSkillCheck(name, db);

		String gamePlayCardUUID = uuidAndName.getKey();
		name = uuidAndName.getValue();

		if(gamePlayCardUUID == null){
			gamePlayCardUUID = UUID.randomUUID().toString();

			GamePlayCard newGPC = new GamePlayCard();

			newGPC.cardName = name;
			newGPC.gamePlayCardUUID = gamePlayCardUUID;
			newGPC.archetype = Const.ARCHETYPE_AUTOGENERATE;
			db.replaceIntoGamePlayCard(newGPC);

		}

		db.replaceIntoCardSetWithSoftPriceUpdate(cardNumber, rarity, setName, gamePlayCardUUID, null, name);
	}

	public static void writeOwnedCardToCSV(CSVPrinter p, OwnedCard current) throws IOException {
		p.printRecord(current.folderName, current.quantity, current.cardName, current.setCode, current.setName,
				current.setNumber, current.condition, current.editionPrinting, current.priceBought, current.dateBought,
				current.setRarity, current.colorVariant, current.rarityUnsure, current.gamePlayCardUUID, current.uuid, current.passcode);

	}

	public static void writeTCGPlayerRecordToCSV(CSVPrinter p, ReadCSVRecord current) throws IOException {
		p.printRecord(getStringOrNull(current.getCsvRecord(), Const.TCGPLAYER_ITEMS_CSV),
				getStringOrNull(current.getCsvRecord(), Const.TCGPLAYER_DETAILS_CSV),
				getStringOrNull(current.getCsvRecord(), Const.TCGPLAYER_PRICE_CSV),
				getStringOrNull(current.getCsvRecord(), Const.TCGPLAYER_QUANTITY_CSV),
				current.getReadTime());

	}
	
	public static void writeUploadCardToCSV(CSVPrinter p, OwnedCard current) throws IOException {
		String printing = current.editionPrinting;
		
		if(printing.equals(Const.CARD_PRINTING_FIRST_EDITION)) {
			printing = Const.CARD_PRINTING_FOIL;
		}
		
		String outputSetNumber = current.setNumber;

		if (!current.colorVariant.equalsIgnoreCase(Const.DEFAULT_COLOR_VARIANT)
				&& !Const.setColorVariantUnsupportedDragonShield.contains(current.setName)) {
			outputSetNumber += current.colorVariant;
		}

		p.printRecord(current.folderName, current.quantity,0, current.cardName, current.setCode, current.setName,
				outputSetNumber, current.condition, printing, "English", current.priceBought, current.dateBought,
				0, 0, 0);

	}

}
