package ygodb.commonLibrary.bean;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class AnalyzePrintedOnceData implements Comparable<AnalyzePrintedOnceData> {

	public String gamePlayCardUUID;
	public String cardName;
	public Set<String> setNumber;
	public Set<String> setName;
	public Set<String> setRarities;
	public String cardType;
	public String releaseDate;
	public String archetype;

	public AnalyzePrintedOnceData() {
		setNumber = new HashSet<>();
		setName = new HashSet<>();
		setRarities = new HashSet<>();
	}

	@Override
	public int compareTo(AnalyzePrintedOnceData o) {

		int compare = 0;

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

		Date thisDate = null;
		Date otherDate = null;
		try {
			thisDate = dateFormat.parse(this.releaseDate);
			otherDate = dateFormat.parse(o.releaseDate);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		
		compare = thisDate.compareTo(otherDate);

		if (compare != 0) {
			return compare;
		}

		compare = this.getStringOfRarities().compareTo(o.getStringOfRarities());

		if (compare != 0) {
			return compare;
		}

		compare = this.getStringOfSetNames().compareTo(o.getStringOfSetNames());

		if (compare != 0) {
			return compare;
		}

		return cardName.compareTo(o.cardName);
	}

	public String getStringOfSetNames() {

		ArrayList<String> results = new ArrayList<>(setName);

		Collections.sort(results);

		StringBuilder output = new StringBuilder("(" + results.get(0));

		for (int i = 1; i < results.size(); i++) {
			output.append(", ").append(results.get(i));
		}

		output.append(")");

		return output.toString();
	}

	public String getStringOfSetNumbers() {
		ArrayList<String> results = new ArrayList<>(setNumber);

		Collections.sort(results);

		StringBuilder output = new StringBuilder("(" + results.get(0));

		for (int i = 1; i < results.size(); i++) {
			output.append(", ").append(results.get(i));
		}

		output.append(")");

		return output.toString();
	}

	public String getStringOfRarities() {
		HashSet<Rarity> enumList = new HashSet<>();

		for (String s : setRarities) {
			Rarity rarityValue = Rarity.fromString(s);
			enumList.add(rarityValue);
		}

		ArrayList<Rarity> enumList2 = new ArrayList<>(enumList);

		Collections.sort(enumList2);

		StringBuilder output = new StringBuilder("(" + enumList2.get(0).toString());

		for (int i = 1; i < enumList.size(); i++) {
			output.append(", ").append(enumList2.get(i).toString());
		}

		output.append(")");

		return output.toString();

	}

}
