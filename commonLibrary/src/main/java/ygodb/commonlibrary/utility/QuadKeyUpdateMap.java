package ygodb.commonlibrary.utility;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import ygodb.commonlibrary.connection.CsvConnection;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class QuadKeyUpdateMap {

	private final Map<String, List<String>> map = new HashMap<>();
	private final String delimiter;

	public QuadKeyUpdateMap(InputStream input, String delimiter) throws IOException {
		this.delimiter = delimiter;
		loadMappings(input);
	}

	public void addMapping(String key, List<String> values) {
		map.put(key, values);
	}

	public List<String> getValues(String key1, String key2, String key3, String key4) {
		String key = String.join(delimiter,
								 key1.toLowerCase(Locale.ROOT), key2.toLowerCase(Locale.ROOT),
								 key3.toLowerCase(Locale.ROOT), key4.toLowerCase(Locale.ROOT));
		List<String> values = map.get(key);
		if (values != null) {
			return values;
		}
		return List.of(key1, key2, key3, key4);
	}

	private void loadMappings(InputStream input) throws IOException {

		CsvConnection csvConnection = new CsvConnection();

		CSVParser parser = csvConnection.getParser(input, StandardCharsets.UTF_16LE);

		for (CSVRecord current : parser) {

			String name = csvConnection.getStringOrNull(current, "Card Name Key");
			String cardNumber = csvConnection.getStringOrNull(current, "Card Number Key");
			String rarity = csvConnection.getStringOrNull(current, "Rarity Key");
			String setName = csvConnection.getStringOrNull(current, "Set Name Key");

			String nameValue = csvConnection.getStringOrNull(current, "Card Name Value");
			String cardNumberValue = csvConnection.getStringOrNull(current, "Card Number Value");
			String rarityValue = csvConnection.getStringOrNull(current, "Rarity Value");
			String setNameValue = csvConnection.getStringOrNull(current, "Set Name Value");

			if (nameValue == null || cardNumberValue == null || rarityValue == null || setNameValue == null) {
				YGOLogger.error("missing value in quad csv:" + nameValue + ":" + cardNumberValue + ":" + rarityValue + ":" + setNameValue);
				continue;
			}

			String key = String.join(delimiter,
									 name.toLowerCase(Locale.ROOT), cardNumber.toLowerCase(Locale.ROOT),
									 rarity.toLowerCase(Locale.ROOT), setName.toLowerCase(Locale.ROOT));
			List<String> values = List.of(nameValue, cardNumberValue, rarityValue, setNameValue);
			addMapping(key, values);
		}
		parser.close();
	}


}
