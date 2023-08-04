package ygodb.commonlibrary.bean;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class AnalyzePrintedOnceData implements Comparable<AnalyzePrintedOnceData> {

	private String gamePlayCardUUID;
	private String cardName;
	private Set<String> setNumber;
	private Set<String> setName;
	private Set<String> setRarities;
	private String cardType;
	private String releaseDate;
	private String archetype;

	public AnalyzePrintedOnceData() {
		setSetNumber(new HashSet<>());
		setSetName(new HashSet<>());
		setSetRarities(new HashSet<>());
	}

	@Override
	public int compareTo(AnalyzePrintedOnceData o) {

		int compare = 0;

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

		Date thisDate = null;
		Date otherDate = null;
		try {
			thisDate = dateFormat.parse(this.getReleaseDate());
			otherDate = dateFormat.parse(o.getReleaseDate());
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

		return getCardName().compareTo(o.getCardName());
	}

	public String getStringOfSetNames() {

		ArrayList<String> results = new ArrayList<>(getSetName());

		Collections.sort(results);

		StringBuilder output = new StringBuilder("(" + results.get(0));

		for (int i = 1; i < results.size(); i++) {
			output.append(", ").append(results.get(i));
		}

		output.append(")");

		return output.toString();
	}

	public String getStringOfSetNumbers() {
		ArrayList<String> results = new ArrayList<>(getSetNumber());

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

		for (String s : getSetRarities()) {
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

	public String getGamePlayCardUUID() {
		return gamePlayCardUUID;
	}

	public void setGamePlayCardUUID(String gamePlayCardUUID) {
		this.gamePlayCardUUID = gamePlayCardUUID;
	}

	public String getCardName() {
		return cardName;
	}

	public void setCardName(String cardName) {
		this.cardName = cardName;
	}

	public Set<String> getSetNumber() {
		return setNumber;
	}

	public void setSetNumber(Set<String> setNumber) {
		this.setNumber = setNumber;
	}

	public Set<String> getSetName() {
		return setName;
	}

	public void setSetName(Set<String> setName) {
		this.setName = setName;
	}

	public Set<String> getSetRarities() {
		return setRarities;
	}

	public void setSetRarities(Set<String> setRarities) {
		this.setRarities = setRarities;
	}

	public String getCardType() {
		return cardType;
	}

	public void setCardType(String cardType) {
		this.cardType = cardType;
	}

	public String getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(String releaseDate) {
		this.releaseDate = releaseDate;
	}

	public String getArchetype() {
		return archetype;
	}

	public void setArchetype(String archetype) {
		this.archetype = archetype;
	}
}
