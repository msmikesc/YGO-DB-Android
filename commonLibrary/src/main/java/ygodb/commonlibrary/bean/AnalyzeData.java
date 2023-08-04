package ygodb.commonlibrary.bean;

import ygodb.commonlibrary.constant.Const;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AnalyzeData implements Comparable<AnalyzeData> {

	private String gamePlayCardUUID;
	private int quantity;
	private String cardName;
	private Set<String> setNumber;
	private Set<String> setNames;
	private Set<String> setRarities;
	private String cardType;
	private BigDecimal cardPriceSummary;
	private int passcode;
	private GamePlayCard gamePlayCard;
	private List<CardSet> cardSets;


	public AnalyzeData() {
		setSetNumber(new HashSet<>());
		setSetNames(new HashSet<>());
		setSetRarities(new HashSet<>());

		setCardPriceSummary(new BigDecimal(0));
		setCardPriceSummary(getCardPriceSummary().setScale(2, RoundingMode.HALF_UP));
	}

	public String getDisplaySummaryPrice() {
		if (getCardPriceSummary() == null) {
			return Const.ZERO_PRICE_STRING;
		}

		return getCardPriceSummary().toString();
	}

	@Override
	public int compareTo(AnalyzeData o) {

		int compare = 0;

		compare = Integer.compare(this.getQuantity(), o.getQuantity());

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

		if (getSetNames().isEmpty()) {
			return "";
		}

		ArrayList<String> results = new ArrayList<>(getSetNames());

		Collections.sort(results);

		StringBuilder output = new StringBuilder(results.get(0));

		for (int i = 1; i < results.size(); i++) {
			output.append(", ").append(results.get(i));
		}

		return output.toString();
	}

	public String getStringOfSetNumbers() {

		if (getSetNumber().isEmpty()) {
			return "";
		}

		ArrayList<String> results = new ArrayList<>(getSetNumber());

		if (results.size() == 1) {
			return results.get(0);
		}

		if (results.isEmpty()) {
			return "None Found";
		}

		Collections.sort(results);

		StringBuilder output = new StringBuilder(results.get(0));

		for (int i = 1; i < results.size(); i++) {
			output.append(", ").append(results.get(i));
		}

		return output.toString();
	}

	public String getStringOfRarities() {

		if (getSetRarities().isEmpty()) {
			return "";
		}

		HashSet<Rarity> enumList = new HashSet<>();

		for (String s : getSetRarities()) {
			Rarity rarityValue = Rarity.fromString(s);
			enumList.add(rarityValue);
		}

		ArrayList<Rarity> enumList2 = new ArrayList<>(enumList);

		if (enumList2.size() == 1) {
			return enumList2.get(0).toString();
		}

		if (enumList2.isEmpty()) {
			return "None Found";
		}

		Collections.sort(enumList2);

		StringBuilder output = new StringBuilder(enumList2.get(0).toString());

		for (int i = 1; i < enumList.size(); i++) {
			output.append(", ").append(enumList2.get(i).toString());
		}

		return output.toString();

	}

	public String getGamePlayCardUUID() {
		return gamePlayCardUUID;
	}

	public void setGamePlayCardUUID(String gamePlayCardUUID) {
		this.gamePlayCardUUID = gamePlayCardUUID;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
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

	public Set<String> getSetNames() {
		return setNames;
	}

	public void setSetNames(Set<String> setNames) {
		this.setNames = setNames;
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

	public BigDecimal getCardPriceSummary() {
		return cardPriceSummary;
	}

	public void setCardPriceSummary(BigDecimal cardPriceAverage) {
		this.cardPriceSummary = cardPriceAverage;
	}

	public int getPasscode() {
		return passcode;
	}

	public void setPasscode(int passcode) {
		this.passcode = passcode;
	}

	public GamePlayCard getGamePlayCard() {
		return gamePlayCard;
	}

	public void setGamePlayCard(GamePlayCard gamePlayCard) {
		this.gamePlayCard = gamePlayCard;
	}

	public List<CardSet> getCardSets() {
		return cardSets;
	}

	public void setCardSets(List<CardSet> cardSets) {
		this.cardSets = cardSets;
	}
}
