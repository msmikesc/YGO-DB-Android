package ygodb.windows.importer;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.csv.CSVPrinter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ygodb.commonlibrary.bean.SetMetaData;
import ygodb.commonlibrary.connection.CsvConnection;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.ApiUtil;
import ygodb.commonlibrary.utility.Util;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.windows.utility.WindowsUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CreateCSVFromYugipedia {

	public static void main(String[] args) throws IOException {
		String page = "Quarter_Century_Bonanza";

		SQLiteConnection db = WindowsUtil.getDBInstance();

		CreateCSVFromYugipedia mainObj = new CreateCSVFromYugipedia();
		mainObj.run(db, page, "yugipedia");
		YGOLogger.info("CSV Process Complete");
	}

	public void run(SQLiteConnection db, String pageName, String csvFile) throws IOException {
		String filename = csvFile+".csv";
		String resourcePath = Const.CSV_EXPORT_FOLDER + filename;

		List<Map<String, String>> rowValues = getMapsFromWikiAPI(db, pageName);
		if (rowValues == null) {
			return;
		}

		CsvConnection csvConnection = new CsvConnection();
		CSVPrinter p = csvConnection.getWikiOutputFile(resourcePath);

		for(Map<String,String> row : rowValues){
			if(row.get(Const.CARD_NAME_CSV) != null && !row.get(Const.CARD_NAME_CSV).isBlank()){
				csvConnection.writeWikiCardToCSV(p, row);
			}
			else{
				YGOLogger.error("ROW WITHOUT NAME FOUND: " + row);
			}
		}
		p.flush();
		p.close();

		YGOLogger.info("CSV written: " + filename);
	}

	private List<Map<String, String>> getMapsFromWikiAPI(SQLiteConnection db, String pageName) {
		String apiUrl = "https://yugipedia.com/api.php?action=parse&page=" + pageName + "&prop=text&format=json";
		String lastWikiLoadFilename = "C:\\Users\\Mike\\AndroidStudioProjects\\YGODB\\log\\lastWikiLoadJSON-"+ pageName;
		JsonNode page = this.getHTMLNode(lastWikiLoadFilename, apiUrl);

		if(page == null){
			YGOLogger.error("Unable to get Wiki Page:" + pageName);
			return null;
		}

		String htmlContent = page.get("parse").get("text").get("*").asText();
		String setName = Util.checkForTranslatedSetName(page.get("parse").get("title").asText().trim());

		// Parse HTML with Jsoup
		Document doc = Jsoup.parse(htmlContent);

		this.saveSetMetaData(db, doc, setName);

		Elements tables = doc.select("table.card-list, table.set-list__main, table.wikitable");

		if (tables.isEmpty()) {
			YGOLogger.error("No tables found");
			return null;
		}

		Set<String> headers = new HashSet<>();
		List<Map<String,String>> rowValues = new ArrayList<>();

		for(Element table: tables) {
			this.addTableToMap(table, headers, rowValues, setName);
		}
		return rowValues;
	}

	private void saveSetMetaData(SQLiteConnection db, Document doc, String pageName){

		try {
			List<SetMetaData> list = db.getAllSetMetaDataFromSetData();
			HashMap<String, SetMetaData> setMetaDataHashMap = new HashMap<>();
			for (SetMetaData s : list) {
				setMetaDataHashMap.put(s.getSetName(), s);
			}

			if (setMetaDataHashMap.get(pageName) == null) {
				//Entry does not yet exist, create it

				SetMetaData setData = this.getSetMetaDataFromPage(doc, pageName);
				db.replaceIntoCardSetMetaData(pageName, setData.getSetPrefix(), setData.getNumOfCards(), setData.getTcgDate());
			}
		} catch (Exception e) {
			YGOLogger.error("Unable to update set data for " + pageName);
			YGOLogger.logException(e);
		}
	}

	private SetMetaData getSetMetaDataFromPage(Document doc, String pageName) {
		// --- Extract sidebar / infobox fields ---
		Element infoBox = doc.selectFirst("table.infobox, table.cardtable");

		String englishPrefix = "";
		String cardCount = "";
		String englishReleaseDate = "";

		if (infoBox != null) {
			Elements rows = infoBox.select("tr");

			for (Element row : rows) {
				Element header = row.selectFirst("th");
				Element value = row.selectFirst("td");
				if (header == null || value == null)
					continue;

				String headerText = header.text().trim();
				String valueText = value.text().trim();

				englishPrefix = this.getEnglishPrefix(headerText, valueText);

				if (headerText.equalsIgnoreCase("Number of cards") ||
						headerText.equalsIgnoreCase("No. of cards")) {
					cardCount = valueText;
				}

				if (headerText.toLowerCase().contains("english (na)")) {
					englishReleaseDate = valueText.trim();
				}
			}
		}

		YGOLogger.info("Wiki Info for " + pageName +
							   " | Prefix: " + englishPrefix +
							   " | Cards: " + cardCount +
							   " | English Release: " + englishReleaseDate);

		int count = -1;

		try{
			count = Integer.parseInt(cardCount);
		} catch (NumberFormatException e) {
			YGOLogger.logException(e);
		}

		return new SetMetaData(pageName, englishPrefix, count, englishReleaseDate);
	}

	private String getEnglishPrefix(String headerText, String valueText) {
		String englishPrefix = "";
		if (headerText.equalsIgnoreCase("Prefix") &&
			valueText.toLowerCase().contains("(en)")) {

			// Example:
			// "YGLD-EN (en) YGLD-FR (fr) YGLD-DE (de)"
			String[] parts = valueText.split("\\s+");

			for (int i = 0; i < parts.length; i++) {
				if (parts[i].equalsIgnoreCase("(en)")) {

					// The prefix is the token immediately before "(en)"
					if (i > 0) {
						englishPrefix = parts[i - 1].trim().split("-")[0];
					}
					break;
				}
			}
		}
		return englishPrefix;
	}

	private JsonNode getHTMLNode(String lastWikiLoadFilename, String apiUrl){
		if(lastWikiLoadFilename != null) {
			File existingFile = new File(lastWikiLoadFilename + "_RAW.txt");

			if (existingFile.exists() && Util.wasModifiedToday(existingFile)) {
				try {
					JsonNode jsonNode = Util.getJsonNode(existingFile);
					YGOLogger.info("Finished reading from Saved File");
					return jsonNode;
				} catch (Exception e) {
					YGOLogger.error("Unable to read saved wiki file");
					YGOLogger.logException(e);
					return null;
				}
			}
		}

		try {
			String inline = ApiUtil.httpGet(apiUrl);
			JsonNode jsonNode = Util.getAndLogJsonNodeFromString(lastWikiLoadFilename, inline);
			YGOLogger.info("Finished reading from API");
			return jsonNode;
		}
		catch (Exception e){
			YGOLogger.error("Exception querying API");
			YGOLogger.logException(e);
			return null;
		}
	}

	private void addTableToMap(Element table,
			Set<String> headers, List<Map<String,String>> rowValues, String setName ) {
		Elements rows = table.select("tr");

		// First row = header
		Elements headerCells = rows.get(0).select("th, td");
		int columnCount = headerCells.size();

		List<String> headerTexts = new ArrayList<>();

		// Get column names
		for (Element headerCell : headerCells) {
			String value = Util.checkForTranslatedYugipediaHeader(headerCell.text().trim());
			headerTexts.add(value);
			headers.add(value);
		}

		// Get data rows
		for (int r = 1; r < rows.size(); r++) {
			Elements cells = rows.get(r).select("td, th");

			Map<String, String> thisRowValues = new HashMap<>();
			thisRowValues.put(Const.SET_NAME_CSV, setName);

			for (int c = 0; c < columnCount; c++) {
				String value = c < cells.size() ? cells.get(c).wholeText().trim() : "";
				thisRowValues.put(headerTexts.get(c), value);
			}
			rowValues.add(thisRowValues);
		}
	}
}
