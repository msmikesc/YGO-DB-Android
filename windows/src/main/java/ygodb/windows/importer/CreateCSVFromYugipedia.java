package ygodb.windows.importer;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.csv.CSVPrinter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ygodb.commonlibrary.connection.CsvConnection;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.ApiUtil;
import ygodb.commonlibrary.utility.Util;
import ygodb.commonlibrary.utility.YGOLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CreateCSVFromYugipedia {

	public static void main(String[] args) throws IOException {
		String page = "Yugi%27s_Legendary_Decks";

		CreateCSVFromYugipedia mainObj = new CreateCSVFromYugipedia();
		mainObj.run(page);
		YGOLogger.info("CSV Process Complete");
	}

	public void run(String pageName) throws IOException {
		String apiUrl = "https://yugipedia.com/api.php?action=parse&page=" + pageName + "&prop=text&format=json";

		String filename = "yugipedia-"+pageName+".csv";
		String resourcePath = Const.CSV_EXPORT_FOLDER + filename;

		String lastWikiLoadFilename = "C:\\Users\\Mike\\AndroidStudioProjects\\YGODB\\log\\lastWikiLoadJSON-"+pageName;

		JsonNode page = this.getHTMLNode(lastWikiLoadFilename, apiUrl);

		if(page == null){
			YGOLogger.error("Unable to get Wiki Page");
			return;
		}

		String htmlContent = page.get("parse").get("text").get("*").asText();

		// Parse HTML with Jsoup
		Document doc = Jsoup.parse(htmlContent);

		Elements tables = doc.select("table.card-list, table.set-list__main, table.wikitable");

		if (tables.isEmpty()) {
			YGOLogger.error("No tables found");
			return;
		}

		Set<String> headers = new HashSet<>();
		List<Map<String,String>> rowValues = new ArrayList<>();

		for(Element table: tables) {
			this.addTableToMap(table, headers, rowValues);
		}

		CsvConnection csvConnection = new CsvConnection();
		CSVPrinter p = csvConnection.getWikiOutputFile(resourcePath);

		for(Map<String,String> row : rowValues){
			if(row.get(Const.CARD_NAME_CSV) != null && !row.get(Const.CARD_NAME_CSV).isBlank()){
				csvConnection.writeWikiCardToCSV(p, row, pageName);
			}
			else{
				YGOLogger.error("ROW WITHOUT NAME FOUND: " + row);
			}
		}
		p.flush();
		p.close();

		YGOLogger.info("CSV written: " + filename);
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
			Set<String> headers, List<Map<String,String>> rowValues ) {
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

			for (int c = 0; c < columnCount; c++) {
				String value = c < cells.size() ? cells.get(c).text().trim() : "";
				thisRowValues.put(headerTexts.get(c), value);
			}
			rowValues.add(thisRowValues);
		}
	}
}
