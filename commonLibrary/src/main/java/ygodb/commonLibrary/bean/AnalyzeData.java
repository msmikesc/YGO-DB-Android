package ygodb.commonLibrary.bean;

import ygodb.commonLibrary.constant.Const;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AnalyzeData implements Comparable<AnalyzeData> {

	public String gamePlayCardUUID;
	public int quantity;
	public String cardName;
	public Set<String> setNumber;
	public Set<String> setName;
	public Set<String> setRarities;
	public String cardType;
	public BigDecimal cardPriceAverage;
	public String mainSetName;
	public String mainSetCode;
	public int passcode;
	public List<CardSet> mainSetCardSets;


	public AnalyzeData() {
		setNumber = new HashSet<>();
		setName = new HashSet<>();
		setRarities = new HashSet<>();

		mainSetCardSets = new ArrayList<>();
		cardPriceAverage = new BigDecimal(0);
		cardPriceAverage = cardPriceAverage.setScale(2, RoundingMode.HALF_UP);

	}

	public String getAveragePrice(){
		if(cardPriceAverage == null){
			return Const.ZERO_PRICE_STRING;
		}

		return cardPriceAverage.toString();
	}

	@Override
	public int compareTo(AnalyzeData o) {

		int compare = 0;
		
		compare = Integer.compare(this.quantity, o.quantity);

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
		
		if(setName.isEmpty()) {
			return "";
		}

		ArrayList<String> results = new ArrayList<>(setName);

		Collections.sort(results);

		StringBuilder output = new StringBuilder(results.get(0));

		for (int i = 1; i < results.size(); i++) {
			output.append(", ").append(results.get(i));
		}

		return output.toString();
	}

	public String getStringOfSetNumbers() {
		
		if(setNumber.isEmpty()) {
			return "";
		}
		
		ArrayList<String> results = new ArrayList<>(setNumber);

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

		if(mainSetCardSets.isEmpty()) {
			return "";
		}

		HashSet<String> mainSetNumber = new HashSet<>();

		for (CardSet mainSetCardSet : mainSetCardSets) {
			mainSetNumber.add(mainSetCardSet.setNumber);
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
		
		if(setRarities.isEmpty()) {
			return "";
		}
		
		HashSet<Rarity> enumList = new HashSet<>();

		for (String s : setRarities) {
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

		if(mainSetCardSets.isEmpty()) {
			return "";
		}

		HashSet<String> mainSetRarities = new HashSet<>();

		for (CardSet mainSetCardSet : mainSetCardSets) {
			mainSetRarities.add(mainSetCardSet.setRarity);
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

		for(CardSet current: mainSetCardSets){

			if(current.setPrice == null){
				current.setPrice = "0";
			}

			BigDecimal newPrice = new BigDecimal(current.setPrice);

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
}
