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
	private Set<String> setName;
	private Set<String> setRarities;
	private String cardType;
	private BigDecimal cardPriceAverage;
	private String mainSetName;
	private String mainSetCode;
	private int passcode;
	private List<CardSet> mainSetCardSets;


	public AnalyzeData() {
		setSetNumber(new HashSet<>());
		setSetName(new HashSet<>());
		setSetRarities(new HashSet<>());

		setMainSetCardSets(new ArrayList<>());
		setCardPriceAverage(new BigDecimal(0));
		setCardPriceAverage(getCardPriceAverage().setScale(2, RoundingMode.HALF_UP));

	}

	public String getAveragePrice(){
		if(getCardPriceAverage() == null){
			return Const.ZERO_PRICE_STRING;
		}

		return getCardPriceAverage().toString();
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
		
		if(getSetName().isEmpty()) {
			return "";
		}

		ArrayList<String> results = new ArrayList<>(getSetName());

		Collections.sort(results);

		StringBuilder output = new StringBuilder(results.get(0));

		for (int i = 1; i < results.size(); i++) {
			output.append(", ").append(results.get(i));
		}

		return output.toString();
	}

	public String getStringOfSetNumbers() {
		
		if(getSetNumber().isEmpty()) {
			return "";
		}
		
		ArrayList<String> results = new ArrayList<>(getSetNumber());

		if(results.size() == 1){
			return results.get(0);
		}

		if(results.isEmpty()){
			return "None Found";
		}

		Collections.sort(results);

		StringBuilder output = new StringBuilder(results.get(0));

		for (int i = 1; i < results.size(); i++) {
			output.append(", ").append(results.get(i));
		}

		return output.toString();
	}

	public String getStringOfMainSetNumbers() {

		if(getMainSetCardSets().isEmpty()) {
			return "";
		}

		HashSet<String> mainSetNumber = new HashSet<>();

		for (CardSet mainSetCardSet : getMainSetCardSets()) {
			mainSetNumber.add(mainSetCardSet.getSetNumber());
		}

		ArrayList<String> results = new ArrayList<>(mainSetNumber);

		if(results.size() == 1){
			return results.get(0);
		}

		if(results.isEmpty()){
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
		
		if(getSetRarities().isEmpty()) {
			return "";
		}
		
		HashSet<Rarity> enumList = new HashSet<>();

		for (String s : getSetRarities()) {
			Rarity rarityValue = Rarity.fromString(s);
			enumList.add(rarityValue);
		}

		ArrayList<Rarity> enumList2 = new ArrayList<>(enumList);

		if(enumList2.size() == 1){
			return enumList2.get(0).toString();
		}

		if(enumList2.isEmpty()){
			return "None Found";
		}

		Collections.sort(enumList2);

		StringBuilder output = new StringBuilder(enumList2.get(0).toString());

		for (int i = 1; i < enumList.size(); i++) {
			output.append(", ").append(enumList2.get(i).toString());
		}

		return output.toString();

	}

	public String getStringOfMainRarities() {

		if(getMainSetCardSets().isEmpty()) {
			return "";
		}

		HashSet<String> mainSetRarities = new HashSet<>();

		for (CardSet mainSetCardSet : getMainSetCardSets()) {
			mainSetRarities.add(mainSetCardSet.getSetRarity());
		}

		HashSet<Rarity> enumList = new HashSet<>();

		for (String s : mainSetRarities) {
			Rarity rarityValue = Rarity.fromString(s);
			enumList.add(rarityValue);
		}

		ArrayList<Rarity> enumList2 = new ArrayList<>(enumList);

		if(enumList2.size() == 1){
			return enumList2.get(0).toString();
		}

		if(enumList2.isEmpty()){
			return "None Found";
		}

		Collections.sort(enumList2);

		StringBuilder output = new StringBuilder(enumList2.get(0).toString());

		for (int i = 1; i < enumList.size(); i++) {
			output.append(", ").append(enumList2.get(i).toString());
		}

		return output.toString();

	}

    public BigDecimal getLowestPriceFromMainSet() {

		BigDecimal lowestPrice = null;

		BigDecimal zero = new BigDecimal(0);

		for(CardSet current: getMainSetCardSets()){

			if(current.getSetPrice() == null){
				current.setSetPrice("0");
			}

			BigDecimal newPrice = new BigDecimal(current.getSetPrice());

			if(newPrice.compareTo(zero) == 0){
				continue;
			}

			if(lowestPrice == null || (lowestPrice.compareTo(newPrice) > 0)) {
				newPrice = newPrice.setScale(2, RoundingMode.HALF_UP);
				lowestPrice = newPrice;
			}
		}

		if(lowestPrice == null){
			return new BigDecimal(Const.ZERO_PRICE_STRING);
		}
		return lowestPrice;

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

	public BigDecimal getCardPriceAverage() {
		return cardPriceAverage;
	}

	public void setCardPriceAverage(BigDecimal cardPriceAverage) {
		this.cardPriceAverage = cardPriceAverage;
	}

	public String getMainSetName() {
		return mainSetName;
	}

	public void setMainSetName(String mainSetName) {
		this.mainSetName = mainSetName;
	}

	public String getMainSetCode() {
		return mainSetCode;
	}

	public void setMainSetCode(String mainSetCode) {
		this.mainSetCode = mainSetCode;
	}

	public int getPasscode() {
		return passcode;
	}

	public void setPasscode(int passcode) {
		this.passcode = passcode;
	}

	public List<CardSet> getMainSetCardSets() {
		return mainSetCardSets;
	}

	public void setMainSetCardSets(List<CardSet> mainSetCardSets) {
		this.mainSetCardSets = mainSetCardSets;
	}
}
