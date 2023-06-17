package ygodb.commonlibrary.bean;

import org.apache.commons.csv.CSVRecord;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReadCSVRecord {

	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

	private CSVRecord csvRecord;
	private Date readTime;

	public ReadCSVRecord(CSVRecord csvRecord, Date readTime) {
		this.csvRecord = csvRecord;
		this.readTime = readTime;
	}

	public ReadCSVRecord(CSVRecord csvRecord, String readTime) throws ParseException {
		this.csvRecord = csvRecord;

		this.readTime = dateFormat.parse(readTime);
	}

	public String getReadTime() {
		return dateFormat.format(readTime);
	}

	public CSVRecord getCsvRecord() {
		return csvRecord;
	}
}
