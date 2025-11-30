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
import ygodb.commonlibrary.utility.Util;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.windows.utility.WindowsUtil;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CreateCSVFromYugipedia {

	DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);
	DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	public static void main(String[] args) throws IOException {
		String[] page = {"Phantom Revenge"};

		SQLiteConnection db = WindowsUtil.getDBInstance();

		CreateCSVFromYugipedia mainObj = new CreateCSVFromYugipedia();
		mainObj.run(db, page, "yugipedia");
		YGOLogger.info("CSV Process Complete");
	}

	public void run(SQLiteConnection db, String[] setNames, String csvFile) throws IOException {
		String filename = csvFile+".csv";
		String resourcePath = Const.CSV_EXPORT_FOLDER + filename;

		List<Map<String, String>> multiPageRowValues = new ArrayList<>();

		for(String setName: setNames) {
			try{
				String pageId = this.getPageIdFromSearch(setName);
				List<Map<String, String>> rowValues = getMapsFromWikiAPI(db, pageId, setName);
				multiPageRowValues.addAll(rowValues);
			} catch (Exception e) {
				YGOLogger.error("Unable to load set: " + setName);
				YGOLogger.logException(e);
			}
		}

		CsvConnection csvConnection = new CsvConnection();
		CSVPrinter p = csvConnection.getWikiOutputFile(resourcePath);

		for(Map<String,String> row : multiPageRowValues){
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

	private List<Map<String, String>> getMapsFromWikiAPI(SQLiteConnection db, String pageId, String searchSetName) {
		List<Map<String,String>> rowValues = new ArrayList<>();

		String apiUrl = "https://yugipedia.com/api.php?action=parse&pageid="
				+ pageId + "&prop=text&format=json";
		String lastWikiLoadFilename = "C:\\Users\\Mike\\AndroidStudioProjects\\YGODB\\log\\lastWikiLoadJSON-"+ searchSetName;

		YGOLogger.info("Requesting page: " + apiUrl);
		JsonNode page = Util.getHTMLNodeFromApiOrCachedFile(lastWikiLoadFilename, apiUrl);

		if(page == null){
			YGOLogger.error("Unable to get Wiki Page:" + searchSetName);
			return rowValues;
		}

		String htmlContent = page.get("parse").get("text").get("*").asText();
		String setName = Util.checkForTranslatedSetName(page.get("parse").get("title").asText().trim());

		// Parse HTML with Jsoup
		Document doc = Jsoup.parse(htmlContent);

		this.saveSetMetaData(db, doc, setName);

		Elements tables = doc.select("table.card-list, table.set-list__main, table.wikitable");

		if (tables.isEmpty()) {
			YGOLogger.error("No tables found");
			return rowValues;
		}

		Set<String> headers = new HashSet<>();

		for(Element table: tables) {
			this.addTableToMap(table, headers, rowValues, setName);
		}
		return rowValues;
	}

	private String getPageIdFromSearch(String setName) {
		try {
			setName = setName.trim();
			String encoded = URLEncoder.encode(setName, StandardCharsets.UTF_8.toString());
			String apiUrl = "https://yugipedia.com/api.php?action=query&list=search&srsearch="
					+ encoded + "&format=json";

			String lastWikiSearchFilename = "C:\\Users\\Mike\\AndroidStudioProjects\\YGODB\\log\\lastWikiSearchJSON-"+ setName;

			JsonNode root = Util.getHTMLNodeFromApiOrCachedFile(lastWikiSearchFilename, apiUrl);

			if(root == null){
				YGOLogger.error("JsonNode root was null");
				return null;
			}
			JsonNode searchResults = root.path("query").path("search");
			if (searchResults.isArray() && !searchResults.isEmpty()) {
				return searchResults.get(0).get("pageid").asText();
			} else {
				YGOLogger.error("No Yugipedia result found for: " + setName);
				return null;
			}
		} catch (Exception e) {
			YGOLogger.error("Error searching for Yugipedia page: " + setName);
			YGOLogger.logException(e);
			return null;
		}
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
				if(setData != null) {
					db.replaceIntoCardSetMetaData(pageName, setData.getSetPrefix(), setData.getNumOfCards(), setData.getTcgDate());
				}
			}
		} catch (Exception e) {
			YGOLogger.error("Unable to update set data for " + pageName);
			YGOLogger.logException(e);
		}
	}

	private SetMetaData getSetMetaDataFromPage(Document doc, String pageName) {
		Element infoBox = doc.selectFirst("table.infobox, table.cardtable");
		if (infoBox == null) {
			YGOLogger.error("Unable to find infobox for: " + pageName);
			return null;
		}
		Elements rows = infoBox.select("tr");

		String englishPrefix = getPrefixFromRows(rows);
		String cardCountText = getCardCountFromRows(rows);
		String englishReleaseDate = getReleaseDateFromRows(rows);

		// Format release date
		if (englishReleaseDate != null && !englishReleaseDate.isBlank()) {
			try {
				LocalDate date = LocalDate.parse(englishReleaseDate, inputFormatter);
				englishReleaseDate = date.format(outputFormatter);
			} catch (Exception e) {
				YGOLogger.logException(e);
			}
		}

		// Convert card count
		int cardCount = -1;
		if (cardCountText != null && !cardCountText.isBlank()) {
			try {
				cardCount = Integer.parseInt(cardCountText);
			} catch (NumberFormatException e) {
				YGOLogger.logException(e);
			}
		}

		YGOLogger.info("Wiki Info for " + pageName +
							   " | Prefix: " + englishPrefix +
							   " | Cards: " + cardCount +
							   " | English Release: " + englishReleaseDate);

		return new SetMetaData(pageName, englishPrefix, cardCount, englishReleaseDate);
	}

	private String getPrefixFromRows(Elements rows) {
		for (Element row : rows) {
			Element headerEl = row.selectFirst("th");
			Element valueEl  = row.selectFirst("td");
			if (headerEl == null || valueEl == null)
				continue;

			String header = headerEl.text().trim();
			String value = valueEl.text().trim();

			if (header.equalsIgnoreCase("Prefix") && value.toLowerCase().contains("(en)")){
				// Split by whitespace, walk tokens
				String[] parts = value.split("\\s+");

				for (int i = 0; i < parts.length; i++) {
					if (parts[i].equalsIgnoreCase("(en)") && i > 0) {
						// Prefix is token before "(en)", before dash
						return parts[i - 1].split("-")[0].trim();
					}
				}
			}
		}
		return null;
	}

	private String getCardCountFromRows(Elements rows) {
		for (Element row : rows) {
			Element headerEl = row.selectFirst("th");
			Element valueEl  = row.selectFirst("td");
			if (headerEl == null || valueEl == null)
				continue;

			String headerLower = headerEl.text().trim().toLowerCase();

			if (headerLower.equals("number of cards") || headerLower.equals("no. of cards")) {
				return valueEl.text().trim();
			}
		}
		return null;
	}

	private String getReleaseDateFromRows(Elements rows) {
		for (Element row : rows) {
			Element headerEl = row.selectFirst("th");
			Element valueEl  = row.selectFirst("td");
			if (headerEl == null || valueEl == null)
				continue;

			String headerLower = headerEl.text().trim().toLowerCase();

			if (headerLower.contains("english (na)")) {
				return valueEl.text().trim();
			}
		}
		return null;
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
