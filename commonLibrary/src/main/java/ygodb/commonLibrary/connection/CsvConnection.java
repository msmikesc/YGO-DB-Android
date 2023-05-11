package ygodb.commonLibrary.connection;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import javafx.util.Pair;
import ygodb.commonLibrary.analyze.AnalyzeCompareToDragonShieldCSV;
import ygodb.commonLibrary.bean.CardSet;
import ygodb.commonLibrary.bean.GamePlayCard;
import ygodb.commonLibrary.bean.OwnedCard;
import ygodb.commonLibrary.bean.SetMetaData;
import ygodb.commonLibrary.utility.Util;

public class CsvConnection {

	public static Iterator<CSVRecord> getIterator(String Filename, Charset charset) throws IOException {
		File f = new File(Filename);

		BufferedReader fr = new BufferedReader(new FileReader(f, charset));

		skipByteOrderMark(fr);

		CSVParser parser = CSVFormat.DEFAULT.withHeader().parse(fr);

		Iterator<CSVRecord> it = parser.iterator();

		return it;
	}

	public static CSVParser getParser(InputStream input, Charset charset) throws IOException {

		BufferedReader fr = new BufferedReader(new InputStreamReader(input, charset));

		skipByteOrderMark(fr);

		CSVParser parser = CSVFormat.DEFAULT.withHeader().parse(fr);

		return parser;
	}

	private static void skipByteOrderMark(Reader reader) throws IOException {
		reader.mark(1);
		char[] possibleBOM = new char[1];
		reader.read(possibleBOM);

		if (possibleBOM[0] != '\ufeff') {
			reader.reset();
		}
	}

	public static Iterator<CSVRecord> getIteratorSkipFirstLine(String Filename, Charset charset) throws IOException {
		File f = new File(Filename);

		FileReader fr = new FileReader(f, charset);

		BufferedReader s = new BufferedReader(fr);

		s.readLine();

		CSVParser parser = CSVFormat.DEFAULT.withHeader().parse(s);

		Iterator<CSVRecord> it = parser.iterator();

		return it;
	}

	public static CSVPrinter getExportOutputFile(String filename) {

		try {
			Writer fw = new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_16LE);
			CSVPrinter p = new CSVPrinter(fw, CSVFormat.DEFAULT);

			p.printRecord("Folder Name", "Quantity", "Card Name", "Set Code", "Set Name", "Card Number", "Condition",
					"Printing", "Price Bought", "Date Bought", "Rarity", "Rarity Color Variant", "Rarity Unsure",
					"gamePlayCardUUID", "UUID", "passcode");

			return p;

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}
	
	public static CSVPrinter getExportUploadFile(String filename) {

		try {
			Writer fw = new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_16LE);
			CSVPrinter p = new CSVPrinter(fw, CSVFormat.DEFAULT);

			p.printRecord("Folder Name", "Quantity", "Trade Quantity", "Card Name", "Set Code", "Set Name",
					"Card Number", "Condition", "Printing", "Language", "Price Bought", "Date Bought", "LOW", "MID",
					"MARKET");

			return p;

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}
	
	public static CSVPrinter getAnalyzeOutputFile(String filename) {

		try {
			Writer fw = new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_16LE);
			CSVPrinter p = new CSVPrinter(fw, CSVFormat.DEFAULT);

			p.printRecord("Quantity", "Card Name","Card Type", "Rarities","Set Name","Set Code", "TCGPlayer Mass Buy 3", "TCGPlayer Mass Buy 1");

			return p;

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}
	
	public static CSVPrinter getSellFile(String filename) {

		try {
			Writer fw = new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_16LE);
			CSVPrinter p = new CSVPrinter(fw, CSVFormat.DEFAULT);

			p.printRecord("Quantity", "Card Name", "Rarity", "Set Name", "Set Code", "Price Bought");

			return p;

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}
	
	public static CSVPrinter getAnalyzePrintedOnceOutputFile(String filename) {

		try {
			Writer fw = new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_16LE);
			CSVPrinter p = new CSVPrinter(fw, CSVFormat.DEFAULT);

			p.printRecord("gamePlayCardUUID", "Card Name","Card Type", "Rarities","Set Names","Set Codes", "Release Date", "Archetype");

			return p;

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}

	public static OwnedCard getOwnedCardFromDragonShieldCSV(CSVRecord current, SQLiteConnection db) throws SQLException {

		String folder = getStringOrNull(current,"Folder Name");
		String name = getStringOrNull(current,"Card Name");
		String quantity = getStringOrNull(current,"Quantity");
		String setCode = getStringOrNull(current,"Set Code");
		String setNumber = getStringOrNull(current,"Card Number");
		String setName = getStringOrNull(current,"Set Name");
		String condition = getStringOrNull(current,"Condition");
		String printing = getStringOrNull(current,"Printing");
		String priceBought = Util.normalizePrice(getStringOrNull(current,"Price Bought"));
		String dateBought = getStringOrNull(current,"Date Bought");
		
		String colorCode = Util.defaultColorVariant;
		
		String priceLow = Util.normalizePrice(getStringOrNull(current,"LOW"));
		String priceMid = Util.normalizePrice(getStringOrNull(current,"MID"));
		String priceMarket = Util.normalizePrice(getStringOrNull(current,"MARKET"));

		if (printing.equals("Foil")) {
			printing = "1st Edition";
		}

		name = Util.checkForTranslatedCardName(name);
		setName = Util.checkForTranslatedSetName(setName);
		setNumber = Util.checkForTranslatedSetNumber(setNumber);

		ArrayList<OwnedCard> ownedRarities = DatabaseHashMap.getExistingOwnedRaritesForCardFromHashMap(setNumber,
				priceBought, dateBought, folder, condition, printing, db);

		if (ownedRarities.size() == 0) {
			// try removing color code

			String newSetNumber = setNumber.substring(0, setNumber.length() - 1);
			String newColorCode = setNumber.substring(setNumber.length() - 1);

			ownedRarities = DatabaseHashMap.getExistingOwnedRaritesForCardFromHashMap(newSetNumber, priceBought,
					dateBought, folder, condition, printing, db);

			if (ownedRarities.size() > 0) {
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
					System.out.println("Unknown gamePlayCard for " + name);
				}
				else{
					passcode = gpc.passcode;
				}
				
				OwnedCard card = Util.formOwnedCard(folder, name, quantity, setCode, condition, printing, priceBought,
						dateBought, setIdentified, passcode);
				
				card.UUID = existingCard.UUID;
				
				return card;
			}
		}

		CardSet setIdentified = Util.findRarity(priceBought, dateBought, folder, condition, printing, setNumber,
				setName, name, db);
		
		setIdentified.colorVariant = colorCode;

		int passcode = -1;
		GamePlayCard gpc = db.getGamePlayCardByUUID(setIdentified.gamePlayCardUUID);

		if(gpc == null){
			System.out.println("Unknown gamePlayCard for " + name);
		}
		else{
			passcode = gpc.passcode;
		}
		
		OwnedCard card = Util.formOwnedCard(folder, name, quantity, setCode, condition, printing, priceBought,
				dateBought, setIdentified, passcode);

		return card;
	}

	public static OwnedCard getOwnedCardFromExportedCSV(CSVRecord current, SQLiteConnection db) throws SQLException {

		String folder = getStringOrNull(current,"Folder Name");
		String name = getStringOrNull(current,"Card Name");
		String quantity = getStringOrNull(current,"Quantity");
		String setCode = getStringOrNull(current,"Set Code");
		String setNumber = getStringOrNull(current,"Card Number");
		String setName = getStringOrNull(current,"Set Name");
		String condition = getStringOrNull(current,"Condition");
		String printing = getStringOrNull(current,"Printing");
		String priceBought = Util.normalizePrice(getStringOrNull(current,"Price Bought"));
		String dateBought = getStringOrNull(current,"Date Bought");
		String rarity = getStringOrNull(current,"Rarity");
		String rarityColorVariant = getStringOrNull(current,"Rarity Color Variant");
		String rarityUnsure = getStringOrNull(current,"Rarity Unsure");
		String gamePlayCardUUID = getStringOrNull(current, "gamePlayCardUUID");
		int passcode = getIntOrNegativeOne(current, "passcode");
		
		String UUID = getStringOrNull(current,"UUID");

		name = Util.checkForTranslatedCardName(name);
		rarity = Util.checkForTranslatedRarity(rarity);
		passcode = Util.checkForTranslatedPasscode(passcode);

		if (printing.equals("Foil")) {
			printing = "1st Edition";
		}


		if(gamePlayCardUUID == null) {
			gamePlayCardUUID = db.getGamePlayCardUUIDFromTitle(name);
		}

		if(passcode == -1) {

			GamePlayCard gpc = db.getGamePlayCardByUUID(gamePlayCardUUID);

			if (gpc == null) {
				System.out.println("Unknown gamePlayCard for " + name);
			} else {
				passcode = gpc.passcode;
			}
		}

		CardSet setIdentified = new CardSet();
		setIdentified.rarityUnsure = Integer.valueOf(rarityUnsure);
		setIdentified.colorVariant = rarityColorVariant;
		setIdentified.setRarity = rarity;
		setIdentified.setName = setName;
		setIdentified.setNumber = setNumber;
		setIdentified.gamePlayCardUUID = gamePlayCardUUID;
		
		OwnedCard card = Util.formOwnedCard(folder, name, quantity, setCode, condition, printing, priceBought,
				dateBought, setIdentified, passcode);
		
		card.UUID = UUID;

		return card;
	}
	
	public static OwnedCard getOwnedCardFromTCGPlayerCSV(CSVRecord current, SQLiteConnection db) throws SQLException {

		String folder = "UnSynced Folder";

		String items = getStringOrNull(current,"ITEMS");
		String details = getStringOrNull(current,"DETAILS");
		String price = getStringOrNull(current,"PRICE").replace("$", "");
		String quantity = getStringOrNull(current,"QUANTITY");
		
		String colorVariant = Util.defaultColorVariant;

		String[] nameAndSet = items.split("\n");

		// possible sold by line
		if (nameAndSet.length < 2 || nameAndSet.length > 3) {
			System.out.println("Unknown format: " + items);
			return null;
		}

		String name = nameAndSet[0].trim();
		String setName = nameAndSet[1].trim();

		// remove tcgplayer rarity id
		if (name.contains("(Duel Terminal)")) {
			name = name.replace("(Duel Terminal)", "").trim();
		}
		
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
			System.out.println("Unknown format: " + details);
			return null;
		}

		String rarity = rarityConditionPrinting[0].replace("Rarity:", "").trim();

		name = Util.checkForTranslatedCardName(name);
		rarity = Util.checkForTranslatedRarity(rarity);
		setName = Util.checkForTranslatedSetName(setName);

		String printing = "Limited";

		if (rarityConditionPrinting[1].contains("1st Edition")) {
			printing = "1st Edition";
		}
		if (rarityConditionPrinting[1].contains("Unlimited")) {
			printing = "Unlimited";
		}

		String condition = rarityConditionPrinting[1].replace("Unlimited", "").replace("Limited", "")
				.replace("1st Edition", "").replace("Condition:", "").replaceAll("\\s", "")
				.replace("LightlyPlayed", "LightPlayed").replace("ModeratelyPlayed", "Played")
				.replace("HeavilyPlayed", "Poor").replace("Damaged", "Poor");

		CardSet setIdentified = db.getFirstCardSetForCardInSet(name, setName);

		if (setIdentified == null) {
			System.out.println("Unknown setCode for card name and set: " + name + ":" + setName);
			setIdentified = new CardSet();
			setIdentified.rarityUnsure = 1;
			setIdentified.colorVariant = Util.defaultColorVariant;
			setIdentified.setName = setName;
			setIdentified.setNumber = null;
			setIdentified.gamePlayCardUUID = db.getGamePlayCardUUIDFromTitle(name);

		}

		setIdentified.setRarity = rarity;
		setIdentified.colorVariant = colorVariant;
		
		String setCode = null;

		ArrayList<SetMetaData> metaData = db.getSetMetaDataFromSetName(setName);

		if (metaData.size() != 1) {
			System.out.println("Unknown metaData for set: " + setName);
		} else {
			setCode = metaData.get(0).set_code;
		}

		String priceBought = Util.normalizePrice(price);
		String dateBought = java.time.LocalDate.now().toString();

		int passcode = -1;
		GamePlayCard gpc = db.getGamePlayCardByUUID(setIdentified.gamePlayCardUUID);

		if(gpc == null){
			System.out.println("Unknown gamePlayCard for " + name);
		}
		else{
			passcode = gpc.passcode;
		}

		OwnedCard card = Util.formOwnedCard(folder, name, quantity, setCode, condition, printing, priceBought,
				dateBought, setIdentified, passcode);

		return card;
	}

	public static Integer getIntOrNegativeOne(CSVRecord current, String recordName) {
		try {
			Integer returnVal = Integer.parseInt(current.get(recordName));
			return returnVal;
		} catch (Exception e) {
			return Integer.valueOf(-1);
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

		String name = getStringOrNull(current, "Card Name");
		String type = getStringOrNull(current, "Card Type");
		Integer passcode = getIntOrNegativeOne(current, "Passcode");
		String lore = getStringOrNull(current, "Card Text");
		String attribute = getStringOrNull(current, "Attribute");
		String race = getStringOrNull(current, "Race");
		String linkValue = getStringOrNull(current, "Link Value");
		String pendScale = getStringOrNull(current, "Pendulum Scale");
		String level = getStringOrNull(current, "Level/Rank");
		String atk = getStringOrNull(current, "Attack");
		String def = getStringOrNull(current, "Defense");
		String archetype = getStringOrNull(current, "Archetype");

		GamePlayCard GPC = new GamePlayCard();

		name = Util.checkForTranslatedCardName(name);
		passcode = Util.checkForTranslatedPasscode(passcode);

		GPC.cardName = name;
		GPC.cardType = type;
		GPC.archetype = archetype;
		GPC.passcode = passcode;

		Pair<String, String> UUIDAndName = Util.getGamePlayCardUUIDFromTitleOrGenerateNewWithSkillCheck(name, db);

		GPC.gamePlayCardUUID = UUIDAndName.getKey();
		GPC.cardName = UUIDAndName.getValue();

		GPC.desc = lore;
		GPC.attribute = attribute;
		GPC.race = race;
		GPC.linkval = linkValue;
		GPC.scale = pendScale;
		GPC.level = level;
		GPC.atk = atk;
		GPC.def = def;

		db.replaceIntoGamePlayCard(GPC);
	}

	public static void insertCardSetFromCSV(CSVRecord current, String defaultSetName, SQLiteConnection db) throws SQLException {

		String name = getStringOrNull(current,"Name");
		String cardNumber = getStringOrNull(current,"Card number");
		String rarity = getStringOrNull(current,"Rarity");

		String setName = null;

		try {
			setName = getStringOrNull(current,"Set Name");

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

		Pair<String, String> UUIDAndName = Util.getGamePlayCardUUIDFromTitleOrGenerateNewWithSkillCheck(name, db);

		String gamePlayCardUUID = UUIDAndName.getKey();
		name = UUIDAndName.getValue();

		db.replaceIntoCardSetWithSoftPriceUpdate(cardNumber, rarity, setName, gamePlayCardUUID, null, name);
	}

	public static void writeOwnedCardToCSV(CSVPrinter p, OwnedCard current) throws IOException {
		// p.printRecord("Folder Name","Quantity","Card Name","Set Code","Set
		// Name","Card Number","Condition","Printing","Price Bought","Date
		// Bought","Rarity","Rarity Color Variant", "Rarity Unsure","gamePlayCardUUID");
		// UUID
		p.printRecord(current.folderName, current.quantity, current.cardName, current.setCode, current.setName,
				current.setNumber, current.condition, current.editionPrinting, current.priceBought, current.dateBought,
				current.setRarity, current.colorVariant, current.rarityUnsure, current.gamePlayCardUUID, current.UUID, current.passcode);

	}
	
	public static void writeUploadCardToCSV(CSVPrinter p, OwnedCard current) throws IOException {
		// Folder Name	Quantity	Trade Quantity	Card Name	Set Code	Set Name	Card Number	
		//Condition	Printing	Language	Price Bought	Date Bought	LOW	MID	MARKET
		
		String printing = current.editionPrinting;
		
		if(printing.equals("1st Edition")) {
			printing = "Foil";
		}
		
		String outputSetNumber = current.setNumber;

		if (!current.colorVariant.equalsIgnoreCase(Util.defaultColorVariant)
				&& !AnalyzeCompareToDragonShieldCSV.setColorVariantUnsupportedDragonShield.contains(current.setName)) {
			outputSetNumber += current.colorVariant;
		}

		p.printRecord(current.folderName, current.quantity,0, current.cardName, current.setCode, current.setName,
				outputSetNumber, current.condition, printing, "English", current.priceBought, current.dateBought,
				0, 0, 0);

	}

}
