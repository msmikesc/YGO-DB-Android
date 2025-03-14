package ygodb.commonlibrary.utility;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import ygodb.commonlibrary.connection.CsvConnection;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class QuadKeyDoesNotExistSet {

	private final Set<String> set = new HashSet<>();
	private final String delimiter;

	public QuadKeyDoesNotExistSet(InputStream input, String delimiter) throws IOException {
		this.delimiter = delimiter;
		loadSet(input);
	}

	public void addMapping(String key) {
		set.add(key);
	}

	public boolean containsKey(String key1, String key2, String key3, String key4) {
		String key = String.join(delimiter,
								 key1.toLowerCase(Locale.ROOT), key2.toLowerCase(Locale.ROOT),
								 key3.toLowerCase(Locale.ROOT), key4.toLowerCase(Locale.ROOT));
		return set.contains(key);
	}

	private void loadSet(InputStream input) throws IOException {

		CsvConnection csvConnection = new CsvConnection();

		CSVParser parser = csvConnection.getParser(input, StandardCharsets.UTF_16LE);

		for (CSVRecord current : parser) {

			String name = csvConnection.getStringOrNull(current, "Card Name Key");
			String cardNumber = csvConnection.getStringOrNull(current, "Card Number Key");
			String rarity = csvConnection.getStringOrNull(current, "Rarity Key");
			String setName = csvConnection.getStringOrNull(current, "Set Name Key");

			String key = String.join(delimiter,
									 name.toLowerCase(Locale.ROOT), cardNumber.toLowerCase(Locale.ROOT),
									 rarity.toLowerCase(Locale.ROOT), setName.toLowerCase(Locale.ROOT));
			addMapping(key);
		}
		parser.close();
	}


}
