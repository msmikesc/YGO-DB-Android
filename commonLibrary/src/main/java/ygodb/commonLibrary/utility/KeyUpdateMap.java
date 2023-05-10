package ygodb.commonLibrary.utility;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import ygodb.commonLibrary.connection.CsvConnection;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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

        CSVParser parser = CsvConnection.getParser(input, StandardCharsets.UTF_16LE);

        Iterator<CSVRecord> it = parser.iterator();

        while (it.hasNext()) {

            CSVRecord current = it.next();

            String key = CsvConnection.getStringOrNull(current,"Key");

            String value = CsvConnection.getStringOrNull(current,"Value");

            addMapping(key, value);
        }
        parser.close();
    }


}
