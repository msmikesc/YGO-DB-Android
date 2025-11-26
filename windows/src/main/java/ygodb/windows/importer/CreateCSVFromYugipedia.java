package ygodb.windows.importer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVPrinter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ygodb.commonlibrary.connection.CsvConnection;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.Util;
import ygodb.commonlibrary.utility.YGOLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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

	//TODO most of this should be reusable with price functions
	private JsonNode getHTMLNode(String lastWikiLoadFilename, String apiUrl){
		if(lastWikiLoadFilename != null) {
			File existingFile = new File(lastWikiLoadFilename + "_RAW.txt");

			if (existingFile.exists() && Util.wasModifiedToday(existingFile)) {
				try {
					try (BufferedReader reader = new BufferedReader(new FileReader(existingFile))) {
						String line;
						String inline = "";
						while ((line = reader.readLine()) != null) {
							inline += line;
						}

						ObjectMapper objectMapper = new ObjectMapper();
						JsonNode jsonNode = objectMapper.readTree(inline);

						YGOLogger.info("Finished reading from Saved File");
						return jsonNode;
					}
				} catch (Exception e) {
					YGOLogger.error("Unable to read saved wiki file");
					YGOLogger.logException(e);
					return null;
				}
			}
		}

		try {
			String inline = fetch(apiUrl);

			// Parse JSON
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonNode = objectMapper.readTree(inline);

			if (lastWikiLoadFilename != null) {
				try (FileWriter writer = new FileWriter(lastWikiLoadFilename+".txt", false)) {
					writer.write(jsonNode.toPrettyString());
				}
				String rawInput = lastWikiLoadFilename + "_RAW.txt";
				try (FileWriter writer = new FileWriter(rawInput, false)) {
					writer.write(inline);
				}
			}
			YGOLogger.info("Finished reading from API");

			return jsonNode;
		}
		catch (Exception e){
			YGOLogger.error("Exception querying API");
			YGOLogger.logException(e);
			return null;
		}
	}

	//TODO make reusable function?
	private static String fetch(String urlStr) throws IOException {
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestProperty("User-Agent", "YGO DB Importer/1.0");

		try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
			StringBuilder sb = new StringBuilder();
			String line;

			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			return sb.toString();
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
