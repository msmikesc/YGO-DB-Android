package ygodb.commonlibrary.utility;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import ygodb.commonlibrary.connection.CsvConnection;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class KeyUpdateMap {

	private final Map<String, String> map = new HashMap<>();

	public KeyUpdateMap(InputStream input) throws IOException {
		loadMappings(input);
	}

	public void addMapping(String key, String values) {
		map.put(key, values);
	}

	public String getValue(String key) {
		String value = map.get(key);
		if (value != null) {
			return value;
		}
		return key;
	}

	private void loadMappings(InputStream input) throws IOException {

		CsvConnection csvConnection = new CsvConnection();

		CSVParser parser = csvConnection.getParser(input, StandardCharsets.UTF_16LE);

		for (CSVRecord current : parser) {

			String key = csvConnection.getStringOrNull(current, "Key");

			String value = csvConnection.getStringOrNull(current, "Value");

			addMapping(key, value);
		}
		parser.close();
	}


}
