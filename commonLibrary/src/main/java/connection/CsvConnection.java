package connection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import analyze.AnalyzeCompareToDragonShieldCSV;
import bean.CardSet;
import bean.GamePlayCard;
import bean.OwnedCard;
import bean.SetMetaData;

public class CsvConnection {

	public static Iterator<CSVRecord> getIterator(String Filename, Charset charset) throws IOException {
		File f = new File(Filename);

		BufferedReader fr = new BufferedReader(new FileReader(f, charset));

		skipByteOrderMark(fr);

		CSVParser parser = CSVFormat.DEFAULT.withHeader().parse(fr);

		Iterator<CSVRecord> it = parser.iterator();

		return it;
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
					"Passcode", "LOW", "MID", "MARKET", "UUID");

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

			p.printRecord("Quantity", "Card Name", "Rarity", "Set Name", "Set Code", "Price Bought", "LOW",
					"MID", "MARKET");

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

			p.printRecord("wikiID", "Card Name","Card Type", "Rarities","Set Names","Set Codes", "Release Date", "Archetype");

			return p;

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}

	public static OwnedCard getOwnedCardFromDragonShieldCSV(CSVRecord current, SQLiteConnection db) throws SQLException {

		String folder = current.get("Folder Name").trim();
		String name = current.get("Card Name").trim();
		String quantity = current.get("Quantity").trim();
		String setCode = current.get("Set Code").trim();
		String setNumber = current.get("Card Number").trim();
		String setName = current.get("Set Name").trim();
		String condition = current.get("Condition").trim();
		String printing = current.get("Printing").trim();
		String priceBought = Util.normalizePrice(current.get("Price Bought"));
		String dateBought = current.get("Date Bought").trim();
		
		String colorCode = Util.defaultColorVariant;
		
		String priceLow = Util.normalizePrice(current.get("LOW"));
		String priceMid = Util.normalizePrice(current.get("MID"));
		String priceMarket = Util.normalizePrice(current.get("MARKET"));

		if (printing.equals("Foil")) {
			printing = "1st Edition";
		}
		
		setName = Util.checkForTranslatedSetName(setName);

		ArrayList<OwnedCard> ownedRarities = DatabaseHashMap.getExistingOwnedRaritesForCardFromHashMap(setNumber,
				priceBought, dateBought, folder, condition, printing, db);

		if (ownedRarities.size() == 0) {
			// try removing color code

			String newSetNumber = setNumber.substring(0, setNumber.length() - 1);
			String newColorCode = setNumber.substring(setNumber.length() - 1, setNumber.length());

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
				setIdentified.id = existingCard.id;
				setIdentified.setRarity = existingCard.setRarity;
				setIdentified.rarityUnsure = existingCard.rarityUnsure;
				
				OwnedCard card = Util.formOwnedCard(folder, name, quantity, setCode, condition, printing, priceBought,
						dateBought, setIdentified, priceLow, priceMid, priceMarket);
				
				card.UUID = existingCard.UUID;
				
				return card;
			}
		}

		CardSet setIdentified = Util.findRarity(priceBought, dateBought, folder, condition, printing, setNumber,
				setName, name, db);
		
		setIdentified.colorVariant = colorCode;
		
		OwnedCard card = Util.formOwnedCard(folder, name, quantity, setCode, condition, printing, priceBought,
				dateBought, setIdentified, priceLow, priceMid, priceMarket);

		return card;
	}

	public static OwnedCard getOwnedCardFromExportedCSV(CSVRecord current, SQLiteConnection db) throws SQLException {

		String folder = current.get("Folder Name").trim();
		String name = current.get("Card Name").trim();
		String quantity = current.get("Quantity").trim();
		String setCode = current.get("Set Code").trim();
		String setNumber = current.get("Card Number").trim();
		String setName = current.get("Set Name").trim();
		String condition = current.get("Condition").trim();
		String printing = current.get("Printing").trim();
		String priceBought = Util.normalizePrice(current.get("Price Bought").trim());
		String dateBought = current.get("Date Bought").trim();
		String rarity = current.get("Rarity").trim();
		String rarityColorVariant = current.get("Rarity Color Variant").trim();
		String rarityUnsure = current.get("Rarity Unsure").trim();
		Integer wikiID = getIntOrNull(current, "Passcode");
		
		String priceLow = Util.normalizePrice(current.get("LOW"));
		String priceMid = Util.normalizePrice(current.get("MID"));
		String priceMarket = Util.normalizePrice(current.get("MARKET"));
		
		String UUID = current.get("UUID");

		if (printing.equals("Foil")) {
			printing = "1st Edition";
		}
		
		if(wikiID == null) {
			wikiID = db.getCardIdFromTitle(name);
		}

		CardSet setIdentified = new CardSet();
		setIdentified.rarityUnsure = new Integer(rarityUnsure);
		setIdentified.colorVariant = rarityColorVariant;
		setIdentified.setRarity = rarity;
		setIdentified.setName = setName;
		setIdentified.setNumber = setNumber;
		setIdentified.id = wikiID;
		
		OwnedCard card = Util.formOwnedCard(folder, name, quantity, setCode, condition, printing, priceBought,
				dateBought, setIdentified, priceLow, priceMid, priceMarket);
		
		card.UUID = UUID;

		return card;
	}
	
	public static OwnedCard getOwnedCardFromTCGPlayerCSV(CSVRecord current, SQLiteConnection db) throws SQLException {

		String folder = "UnSynced Folder";

		String items = current.get("ITEMS").trim();
		String details = current.get("DETAILS").trim();
		String price = current.get("PRICE").trim().replace("$", "");
		String quantity = current.get("QUANTITY").trim();
		
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
		
		setName = Util.checkForTranslatedSetName(setName);

		CardSet setIdentified = db.getFirstCardSetForCardInSet(name, setName);

		if (setIdentified == null) {
			System.out.println("Unknown setCode for card name and set: " + name + ":" + setName);
			setIdentified = new CardSet();
			setIdentified.rarityUnsure = 1;
			setIdentified.colorVariant = Util.defaultColorVariant;
			setIdentified.setName = setName;
			setIdentified.setNumber = "";
			setIdentified.id = -1;
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

		OwnedCard card = Util.formOwnedCard(folder, name, quantity, setCode, condition, printing, priceBought,
				dateBought, setIdentified, null, null, null);

		return card;
	}

	public static Integer getIntOrNull(CSVRecord current, String recordName) {
		try {
			Integer returnVal = Integer.parseInt(current.get(recordName));
			return returnVal;
		} catch (Exception e) {
			return null;
		}
	}

	public static Integer getIntOrNegativeOne(CSVRecord current, String recordName) {
		try {
			Integer returnVal = Integer.parseInt(current.get(recordName));
			return returnVal;
		} catch (Exception e) {
			return new Integer(-1);
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

	public static void insertGamePlayCardFromCSV(CSVRecord current, String defaultSetName, SQLiteConnection db) throws SQLException {

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

		GPC.cardName = name;
		GPC.cardType = type;
		GPC.archetype = archetype;
		GPC.passcode = passcode;
		GPC.wikiID = passcode;
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

		String name = current.get("Name").trim();
		String cardNumber = current.get("Card number").trim();
		String rarity = current.get("Rarity").trim();

		String setName = null;

		try {
			setName = current.get("Set Name").trim();
		} catch (Exception e) {
			setName = defaultSetName;
		}
		
		setName = Util.checkForTranslatedSetName(setName);

		int wikiID = db.getCardIdFromTitle(name);
		// try skill card
		if (wikiID == -1) {
			wikiID = db.getCardIdFromTitle(name + " (Skill Card)");
			if (wikiID != -1) {
				name = name + " (Skill Card)";
			}
		}

		if (wikiID == -1) {
			System.out.println("Unable to find valid passcode for " + cardNumber + ":" + name);
		}

		db.replaceIntoCardSet(cardNumber, rarity, setName, wikiID, null, name);
	}

	public static void writeOwnedCardToCSV(CSVPrinter p, OwnedCard current) throws IOException {
		// p.printRecord("Folder Name","Quantity","Card Name","Set Code","Set
		// Name","Card Number","Condition","Printing","Price Bought","Date
		// Bought","Rarity","Rarity Color Variant", "Rarity Unsure","Passcode");
		// low, mid, market
		p.printRecord(current.folderName, current.quantity, current.cardName, current.setCode, current.setName,
				current.setNumber, current.condition, current.editionPrinting, current.priceBought, current.dateBought,
				current.setRarity, current.colorVariant, current.rarityUnsure, current.id, current.priceLow, current.priceMid, 
				current.priceMarket, current.UUID);

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
