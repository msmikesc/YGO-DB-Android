package ygodb.commonLibrary.bean;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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

	public ArrayList<CardSet> mainSetCardSets;


	public AnalyzeData() {
		setNumber = new HashSet<String>();
		setName = new HashSet<String>();
		setRarities = new HashSet<String>();

		mainSetCardSets = new ArrayList<CardSet>();
		cardPriceAverage = new BigDecimal(0);
		cardPriceAverage = cardPriceAverage.setScale(2, RoundingMode.HALF_UP);

	}

	public String getAveragePrice(){
		if(cardPriceAverage == null){
			return "0.00";
		}

		return cardPriceAverage.toString();
	}

	@Override
	public int compareTo(AnalyzeData o) {

		int compare = 0;
		
		compare = Integer.valueOf(this.quantity).compareTo(Integer.valueOf(o.quantity));

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
		
		if(setName.size() == 0) {
			return "";
		}

		ArrayList<String> results = new ArrayList<String>(setName);

		Collections.sort(results);

		String output = results.get(0);

		for (int i = 1; i < results.size(); i++) {
			output += ", " + results.get(i);
		}

		return output;
	}

	public String getStringOfSetNumbers() {
		
		if(setNumber.size() == 0) {
			return "";
		}
		
		ArrayList<String> results = new ArrayList<String>(setNumber);

		if(results.size() == 1){
			return results.get(0);
		}

		if(results.size() == 0){
			return "None Found";
		}

		Collections.sort(results);

		String output = results.get(0);

		for (int i = 1; i < results.size(); i++) {
			output += ", " + results.get(i);
		}

		return output;
	}

	public String getStringOfMainSetNumbers() {

		if(mainSetCardSets.size() == 0) {
			return "";
		}

		HashSet<String> mainSetNumber = new HashSet<>();

		for(int i = 0; i < mainSetCardSets.size(); i++){
			mainSetNumber.add(mainSetCardSets.get(i).setNumber);
		}

		ArrayList<String> results = new ArrayList<String>(mainSetNumber);

		if(results.size() == 1){
			return results.get(0);
		}

		if(results.size() == 0){
			return "None Found";
		}

		Collections.sort(results);

		String output = results.get(0);

		for (int i = 1; i < results.size(); i++) {
			output += ", " + results.get(i);
		}

		return output;
	}

	public String getStringOfRarities() {
		
		if(setRarities.size() == 0) {
			return "";
		}
		
		HashSet<Rarity> enumList = new HashSet<Rarity>();

		for (String s : setRarities) {
			Rarity rarityValue = Rarity.fromString(s);
			enumList.add(rarityValue);
		}

		ArrayList<Rarity> enumList2 = new ArrayList<Rarity>(enumList);

		if(enumList2.size() == 1){
			return enumList2.get(0).toString();
		}

		if(enumList2.size() == 0){
			return "None Found";
		}

		Collections.sort(enumList2);

		String output = enumList2.get(0).toString();

		for (int i = 1; i < enumList.size(); i++) {
			output += ", " + enumList2.get(i).toString();
		}

		return output;

	}

	public String getStringOfMainRarities() {

		if(mainSetCardSets.size() == 0) {
			return "";
		}

		HashSet<String> mainSetRarities = new HashSet<>();

		for(int i = 0; i < mainSetCardSets.size(); i++){
			mainSetRarities.add(mainSetCardSets.get(i).setRarity);
		}

		HashSet<Rarity> enumList = new HashSet<Rarity>();

		for (String s : mainSetRarities) {
			Rarity rarityValue = Rarity.fromString(s);
			enumList.add(rarityValue);
		}

		ArrayList<Rarity> enumList2 = new ArrayList<Rarity>(enumList);

		if(enumList2.size() == 1){
			return enumList2.get(0).toString();
		}

		if(enumList2.size() == 0){
			return "None Found";
		}

		Collections.sort(enumList2);

		String output = enumList2.get(0).toString();

		for (int i = 1; i < enumList.size(); i++) {
			output += ", " + enumList2.get(i).toString();
		}

		return output;

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
			return new BigDecimal("0.00");
		}
		return lowestPrice;

    }
}
