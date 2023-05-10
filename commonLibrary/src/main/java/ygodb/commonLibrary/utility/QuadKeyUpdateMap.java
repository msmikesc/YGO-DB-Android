package ygodb.commonLibrary.utility;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import ygodb.commonLibrary.connection.CsvConnection;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
        String key = String.join(delimiter, key1, key2, key3, key4);
        List<String> values = map.get(key);
        if (values != null) {
            return values;
        }
        return List.of(key1, key2, key3, key4);
    }

    private void loadMappings(InputStream input) throws IOException {

        CSVParser parser = CsvConnection.getParser(input, StandardCharsets.UTF_16LE);

        Iterator<CSVRecord> it = parser.iterator();

        while (it.hasNext()) {

            CSVRecord current = it.next();

            String name = CsvConnection.getStringOrNull(current,"Card Name Key");
            String cardNumber = CsvConnection.getStringOrNull(current,"Card Number Key");
            String rarity = CsvConnection.getStringOrNull(current,"Rarity Key");
            String setName = CsvConnection.getStringOrNull(current,"Set Name Key");

            String nameValue = CsvConnection.getStringOrNull(current,"Card Name Value");
            String cardNumberValue = CsvConnection.getStringOrNull(current,"Card Number Value");
            String rarityValue = CsvConnection.getStringOrNull(current,"Rarity Value");
            String setNameValue = CsvConnection.getStringOrNull(current,"Set Name Value");

            String key = String.join(delimiter, name, cardNumber, rarity, setName);
            List<String> values = List.of(nameValue, cardNumberValue, rarityValue, setNameValue);
            addMapping(key, values);
        }
        parser.close();
    }


}
